package com.example.logintemp.ui.home

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.logintemp.R
import com.example.logintemp.api.RetrofitInstance
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.MealDBData.CategoryResponse
import com.example.logintemp.data.MealDBData.Meal
import com.example.logintemp.data.MealDBData.MealResponse
import com.example.logintemp.data.recipe.RecipeEntity
import com.example.logintemp.data.recipe.RecipeRepository
import com.example.logintemp.data.user.UserRepository
import com.example.logintemp.databinding.FragmentHomeBinding
import com.example.logintemp.ui.home.adapters.CategoryAdapter
import com.example.logintemp.ui.home.adapters.FavouriteAdapter
import com.example.logintemp.ui.home.adapters.RecipeSliderAdapter
import com.example.logintemp.util.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Session / Repo (to fetch username + profile image)
    private lateinit var session: SessionManager
    private lateinit var userRepo: UserRepository
    private var currentUserId: Int = -1

    // Repositories & adapters
    private lateinit var recipeRepo: RecipeRepository
    private lateinit var recipeSliderAdapter: RecipeSliderAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var favouriteAdapter: FavouriteAdapter

    // Auto-scroll handler for ViewPager2
    private val sliderHandler = Handler(Looper.getMainLooper())
    private val sliderRunnable = Runnable {
        val currentItem = binding.recipesSlider.currentItem
        val itemCount = recipeSliderAdapter.itemCount
        if (itemCount > 0) {
            binding.recipesSlider.currentItem = (currentItem + 1) % itemCount
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // --- Init session / repo / user
        session = SessionManager(requireContext())
        currentUserId = session.getUserId()
        userRepo = UserRepository(AppDatabase.getInstance(requireContext()).userDao())

        // recipe repository (Room)
        recipeRepo = RecipeRepository(AppDatabase.getInstance(requireContext()).recipeDao())

        // --- Setup header (Hi, {user} + profile image)
        setupHeader()

        // --- Rest of your UI
        setupRecyclerViews()
        setupViewPager()
        observeFavorites()     // observe favorites flow
        fetchRandomMeals()
        fetchCategories()

        // --- Header clicks (replace nav IDs to match your graph)
        binding.imageProfile.setOnClickListener {
            findNavController().navigate(R.id.navigation_profile)
        }

        binding.iconNotifications.setOnClickListener {
            findNavController().navigate(R.id.notificationsFragment)
        }

        binding.btnSearchSmall.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_searchFragment)
        }

        binding.seeAllFavorites.setOnClickListener {

                findNavController().navigate(R.id.navigation_favourites)

        }

        binding.seeAllRecipes.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeToSeeAllRecipes()
            findNavController().navigate(action)
        }
        binding.seeAllCategories.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeToCategories()
            findNavController().navigate(action)
        }


        return root
    }

    /** Header: set greeting + load profile image or fallback initial */
    private fun setupHeader() {
        // Greeting
        val username = session.getUsername() ?: "User"
        binding.textGreeting.text = "Hi, $username"

        // Load profile image or fallback (same idea as ProfileFragment)
        viewLifecycleOwner.lifecycleScope.launch {
            val uriString = userRepo.getProfileImage(currentUserId)
            if (!uriString.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(uriString)
                    // Validate we still have access
                    requireContext().contentResolver.openInputStream(uri)?.close()

                    // Show image, hide initial
                    binding.imageProfile.setImageURI(uri)
                    binding.imageProfile.visibility = View.VISIBLE
                    binding.textProfileInitial.visibility = View.GONE
                } catch (_: Exception) {
                    setFallbackImage(username)
                }
            } else {
                setFallbackImage(username)
            }
        }
    }

    private fun setFallbackImage(username: String) {
        binding.imageProfile.setImageResource(R.drawable.circle_background)
        val initial = username.firstOrNull()?.uppercase() ?: "?"
        binding.textProfileInitial.text = initial
        binding.textProfileInitial.visibility = View.VISIBLE
    }

    private fun setupRecyclerViews() {
        // --------------------------
        //  CATEGORIES RECYCLER
        // --------------------------
        categoryAdapter = CategoryAdapter(listOf()) { category ->
            val action = HomeFragmentDirections.actionHomeToCategory(category.strCategory)
            findNavController().navigate(action)
        }

        binding.categoriesRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }

        // --------------------------
        //  FAVORITES RECYCLER
        // --------------------------
        favouriteAdapter = FavouriteAdapter(
            onFavClick = { recipeEntity ->
                // toggle favorite (handle persistence + undo)
                toggleFavoriteWithUndo(recipeEntity)
            },
            onImageClick = {
                // 👉 When the image is clicked, go to the Favorites Page
                findNavController().navigate(R.id.navigation_favourites)
            }
        )

        binding.favouritesRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = favouriteAdapter
        }
    }


    private fun setupViewPager() {
        recipeSliderAdapter = RecipeSliderAdapter(emptyList()) { selectedMeal ->
            val action = HomeFragmentDirections.actionHomeToRecipeDetail(selectedMeal.idMeal)
            findNavController().navigate(action)
        }
        binding.recipesSlider.adapter = recipeSliderAdapter

        // Auto-scroll every 3 seconds
        binding.recipesSlider.registerOnPageChangeCallback(object :
            androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 3000)
            }
        })
    }

    private fun observeFavorites() {
        // Observe Room Flow of favorite recipes and update adapter
        viewLifecycleOwner.lifecycleScope.launch {
            recipeRepo.getFavoriteRecipesFlow().collect { favs ->
                favouriteAdapter.submitList(favs)
                binding.seeAllFavorites.visibility = if (favs.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    /**
     * Toggle favorite status with optimistic update and show Snackbar for UNDO.
     */
    private fun toggleFavoriteWithUndo(recipe: RecipeEntity) {
        val wasFav = recipe.isFavorite
        val newFav = !wasFav

        // Persist change
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                recipeRepo.toggleFavorite(recipe.id, newFav)
            } catch (t: Throwable) {
                // optionally show toast or log
            }
        }

        // Show styled snackbar (Sous Chef theme text)
        val message = if (newFav) "Sous Chef — Added to favourites" else "Sous Chef — Removed from favourites"
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                // Undo: revert favorite state
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        recipeRepo.toggleFavorite(recipe.id, wasFav)
                    } catch (_: Throwable) { /* ignore */ }
                }
            }.show()
    }

    private fun fetchCategories() {
        RetrofitInstance.api.getCategories().enqueue(object : retrofit2.Callback<CategoryResponse> {
            override fun onResponse(
                call: retrofit2.Call<CategoryResponse>,
                response: retrofit2.Response<CategoryResponse>
            ) {
                response.body()?.categories?.let { categories ->
                    val randomSix = categories.shuffled().take(6)
                    categoryAdapter.updateCategories(randomSix)
                }
            }

            override fun onFailure(call: retrofit2.Call<CategoryResponse>, t: Throwable) {
                // ignore for now
            }
        })
    }

    private fun fetchRandomMeals(count: Int = 10) {
        val meals = mutableListOf<Meal>()
        repeat(count) {
            RetrofitInstance.api.getRandomMeal().enqueue(object : retrofit2.Callback<MealResponse> {
                override fun onResponse(call: retrofit2.Call<MealResponse>, response: retrofit2.Response<MealResponse>) {
                    response.body()?.meals?.firstOrNull()?.let { meal ->
                        meals.add(meal)
                        recipeSliderAdapter.updateMeals(meals)
                    }
                }

                override fun onFailure(call: retrofit2.Call<MealResponse>, t: Throwable) {
                    // ignore
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        sliderHandler.removeCallbacks(sliderRunnable)
    }
}