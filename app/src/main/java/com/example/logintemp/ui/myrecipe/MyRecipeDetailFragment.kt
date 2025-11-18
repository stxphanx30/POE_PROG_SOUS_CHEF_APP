// app/src/main/java/com/example/logintemp/ui/myrecipe/MyRecipeDetailFragment.kt
package com.example.logintemp.ui.myrecipe

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.logintemp.R
import com.example.logintemp.data.recipe.IngredientEntity
import com.example.logintemp.data.recipe.RecipeEntity
import com.example.logintemp.data.recipe.StepEntity
import com.example.logintemp.data.recipe.RecipeRepository
import com.example.logintemp.databinding.FragmentMyRecipeDetailBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import android.app.AlertDialog
import kotlin.getValue

class MyRecipeDetailFragment : Fragment() {

    private var _binding: FragmentMyRecipeDetailBinding? = null
    private val binding get() = _binding!!

    private val args: MyRecipeDetailFragmentArgs by navArgs()

    // Assumes you initialize RecipeRepository in the fragment (pass DAO from AppDatabase)
    private lateinit var recipeRepo: RecipeRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyRecipeDetailBinding.inflate(inflater, container, false)

        // init repository (replace with your app's way of providing it)
        val db = com.example.logintemp.data.AppDatabase.getInstance(requireContext())
        recipeRepo = com.example.logintemp.data.recipe.RecipeRepository(db.recipeDao())

        // Back button
        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        // Delete
        binding.deleteButton.setOnClickListener { confirmAndDelete() }

        // Load data
        loadRecipe(args.recipeId)

        return binding.root
    }

    private fun loadRecipe(recipeId: Long) {
        // load recipe + ingredients + steps in coroutine
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val recipe: RecipeEntity? = withContext(Dispatchers.IO) { recipeRepo.getById(recipeId) }
                if (recipe == null) {
                    // nothing found -> go back
                    Snackbar.make(requireView(), "Recette introuvable", Snackbar.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                    return@launch
                }

                // Bind main fields on UI thread
                binding.mealName.text = recipe.name
                binding.mealDuration.text = recipe.cookTimeMinutes?.let { "$it min" } ?: "—"

                // Load image (URI or fallback)
                if (!recipe.imageUri.isNullOrBlank()) {
                    try {
                        // attempt to use Glide to load Uri (handles remote & local)
                        Glide.with(requireContext())
                            .load(Uri.parse(recipe.imageUri))
                            .centerCrop()
                            .placeholder(R.drawable.logo)
                            .into(binding.mealImage)
                    } catch (e: Exception) {
                        binding.mealImage.setImageResource(R.drawable.logo)
                    }
                } else {
                    binding.mealImage.setImageResource(R.drawable.logo)
                }

                // ingredients + steps from repo
                val ingredients: List<IngredientEntity> = withContext(Dispatchers.IO) {
                    recipeRepo.getIngredientsFor(recipeId)
                }
                val steps: List<StepEntity> = withContext(Dispatchers.IO) {
                    recipeRepo.getStepsFor(recipeId)
                }

                // ingredient count and display
                val ingCount = ingredients.size
                binding.mealIngredientsCount.text = "Ingredients: $ingCount"

                // build bulleted string for ingredients (• item (amount))
                val sbIng = StringBuilder()
                ingredients.sortedBy { it.order }.forEach { ing ->
                    val amt = if (ing.amount.isNullOrBlank()) "" else " (${ing.amount})"
                    sbIng.append("• ${ing.name}$amt\n")
                }
                binding.ingredientsText.text = sbIng.toString().trimEnd()

                // steps bulleted or numbered
                val sbSteps = StringBuilder()
                steps.sortedBy { it.order }.forEachIndexed { index, step ->
                    // use numbered for steps
                    sbSteps.append("${index + 1}. ${step.text.trim()}\n\n")
                }
                binding.stepsText.text = sbSteps.toString().trimEnd()

            } catch (t: Throwable) {
                t.printStackTrace()
                Snackbar.make(requireView(), "Erreur lors du chargement", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmAndDelete() {
        val recipeId = args.recipeId
        AlertDialog.Builder(requireContext())
            .setTitle("Supprimer la recette")
            .setMessage("Êtes-vous sûr(e) de vouloir supprimer cette recette ? Cette action est irréversible.")
            .setPositiveButton("Supprimer") { dialog, _ ->
                dialog.dismiss()
                performDelete(recipeId)
            }
            .setNegativeButton("Annuler") { d, _ -> d.dismiss() }
            .show()
    }

    private fun performDelete(recipeId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { recipeRepo.deleteById(recipeId) }
                // notify user and go back
                Snackbar.make(requireView(), "Recette supprimée", Snackbar.LENGTH_LONG).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                e.printStackTrace()
                Snackbar.make(requireView(), "Impossible de supprimer", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}