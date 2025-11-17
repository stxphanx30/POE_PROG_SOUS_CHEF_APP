package com.example.logintemp.ui.myrecipe

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.logintemp.R
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.recipe.RecipeRepository
import com.example.logintemp.data.user.UserRepository
import com.example.logintemp.databinding.FragmentMyRecipesBinding
import com.example.logintemp.util.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import java.lang.Exception

class MyRecipeFragment : Fragment() {

    private var _binding: FragmentMyRecipesBinding? = null
    private val binding get() = _binding!!

    private lateinit var session: SessionManager
    private lateinit var userRepo: UserRepository
    private lateinit var recipeRepo: RecipeRepository
    private var currentUserId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyRecipesBinding.inflate(inflater, container, false)

        // init session/repo
        session = SessionManager(requireContext())
        currentUserId = session.getUserId()
        userRepo = UserRepository(AppDatabase.getInstance(requireContext()).userDao())

        val db = AppDatabase.getInstance(requireContext())
        recipeRepo = RecipeRepository(db.recipeDao())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeader()

        // Observe recipes for the current user; the Flow will emit again when DB changes (e.g. favorites toggled)
        viewLifecycleOwner.lifecycleScope.launch {
            recipeRepo.getRecipes(currentUserId).collect { list ->
                renderRecipes(list)
            }
        }

        binding.iconNotifications.setOnClickListener {
            findNavController().navigate(R.id.navigation_addrecipe1)
        }
    }

    private fun renderRecipes(list: List<com.example.logintemp.data.recipe.RecipeWithCounts>) {
        val container = binding.containerRecipes
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        if (list.isEmpty()) return

        list.forEach { row ->
            val itemView = inflater.inflate(R.layout.item_recipe, container, false)

            val img = itemView.findViewById<ImageView>(R.id.imgRecipe)
            val btnHeart = itemView.findViewById<ImageButton>(R.id.btnHeart)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
            val tvCategory = itemView.findViewById<TextView>(R.id.tvCategory)
            val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
            val tvIngredients = itemView.findViewById<TextView>(R.id.tvIngredients)
            val tvCreated = itemView.findViewById<TextView>(R.id.tvCreated)

            val recipe = row.recipe

            // Bind core fields
            tvTitle.text = recipe.name
            tvCategory.text = recipe.category

            // Cook time (show only if > 0)
            val minutes = recipe.cookTimeMinutes
            if (minutes != null && minutes > 0) {
                tvTime.visibility = View.VISIBLE
                tvTime.text = "$minutes min"
            } else {
                tvTime.visibility = View.GONE
            }

            // Ingredients count
            tvIngredients.text = "Ingredients: ${row.ingredientCount}"

            // Created date (readable)
            tvCreated.text = "Created on " + java.text.DateFormat.getDateInstance()
                .format(java.util.Date(recipe.createdAt))

            // Image: URI or fallback drawable
            val uri = recipe.imageUri
            if (!uri.isNullOrEmpty()) {
                try {
                    img.setImageURI(Uri.parse(uri))
                } catch (_: Exception) {
                    img.setImageResource(R.drawable.logo) // fallback
                }
            } else {
                img.setImageResource(R.drawable.logo) // fallback
            }

            // --- Favorite: read initial state from recipe and persist on toggle ---
            // initial liked state comes from DB field (isFavorite)
            var liked = recipe.isFavorite

            // set the correct icon on load
            btnHeart.setImageResource(
                if (liked) R.drawable.heart_filled_red else R.drawable.heart_filled
            )

            // click: toggle and persist
            btnHeart.setOnClickListener {
                // optimistic UI change
                liked = !liked
                btnHeart.setImageResource(
                    if (liked) R.drawable.heart_filled_red else R.drawable.heart_filled
                )

                // persist change in DB
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        recipeRepo.toggleFavorite(recipe.id, liked)

                        // show Snackbar message with undo
                        val msg = if (liked) "Added to favourites" else "Removed from favourites"
                        val viewRoot = requireActivity().findViewById<View>(android.R.id.content)
                        Snackbar.make(viewRoot, "Sous Chef — $msg", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                // undo: flip back in DB
                                viewLifecycleOwner.lifecycleScope.launch {
                                    recipeRepo.toggleFavorite(recipe.id, !liked)
                                }
                            }.show()
                    } catch (e: Exception) {
                        // on error: revert UI and inform user
                        liked = !liked
                        btnHeart.setImageResource(
                            if (liked) R.drawable.heart_filled_red else R.drawable.heart_filled
                        )
                        Toast.makeText(requireContext(), "Could not update favourite", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            container.addView(itemView)
        }
    }

    /** Username-only label + profile image or fallback initial */
    private fun setupHeader() {
        val username = session.getUsername().orEmpty().ifBlank { "User" }
        binding.textGreeting.text = username

        viewLifecycleOwner.lifecycleScope.launch {
            val uriString = userRepo.getProfileImage(currentUserId)
            if (!uriString.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(uriString)
                    requireContext().contentResolver.openInputStream(uri)?.close()

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
        binding.imageProfile.setImageDrawable(null)
        binding.imageProfile.visibility = View.GONE
        binding.textProfileInitial.text =
            username.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "U"
        binding.textProfileInitial.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}