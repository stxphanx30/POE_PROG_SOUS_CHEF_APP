package com.example.logintemp.ui.recipes

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.logintemp.R
import com.example.logintemp.api.RetrofitInstance
import com.example.logintemp.data.MealDBData.MealResponse
import com.example.logintemp.data.MealDBData.Meal
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.user.UserRepository
import com.example.logintemp.databinding.FragmentSeeAllRecipesBinding
import com.example.logintemp.ui.recipes.adapters.RecipeAdapter
import com.example.logintemp.util.SessionManager
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SeeAllRecipesFragment : Fragment() {

    private var _binding: FragmentSeeAllRecipesBinding? = null
    private val binding get() = _binding!!

    private lateinit var session: SessionManager
    private lateinit var repo: UserRepository
    private lateinit var recipeAdapter: RecipeAdapter
    private var currentUserId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeeAllRecipesBinding.inflate(inflater, container, false)
        val root = binding.root

        session = SessionManager(requireContext())
        currentUserId = session.getUserId()
        repo = UserRepository(AppDatabase.getInstance(requireContext()).userDao())

        binding.arrowback.setOnClickListener {
            findNavController().navigate(R.id.action_seeAllRecipes_to_homeFragment)
        }
        setupRecyclerView()
        fetchRandomMeals(20) // Fetch more for "See All"

        return root
    }





    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(listOf()) { meal ->
            val action = SeeAllRecipesFragmentDirections.actionSeeAllRecipesToRecipeDetail(meal.idMeal)
            findNavController().navigate(action)
        }

        binding.recipesRecycler.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = recipeAdapter
        }
    }

    private fun fetchRandomMeals(count: Int = 10) {
        val meals = mutableListOf<Meal>()
        repeat(count) {
            RetrofitInstance.api.getRandomMeal().enqueue(object : Callback<MealResponse> {
                override fun onResponse(call: Call<MealResponse>, response: Response<MealResponse>) {
                    response.body()?.meals?.firstOrNull()?.let { meal ->
                        meals.add(meal)
                        recipeAdapter.updateMeals(meals)
                    }
                }

                override fun onFailure(call: Call<MealResponse>, t: Throwable) {
                    // TODO: handle failure
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
