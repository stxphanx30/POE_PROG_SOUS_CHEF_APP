package com.example.logintemp.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.logintemp.api.RetrofitInstance
import com.example.logintemp.data.MealDBData.MealResponse
import com.example.logintemp.data.MealDBData.getIngredient
import com.example.logintemp.data.MealDBData.getMeasure
import com.example.logintemp.databinding.FragmentRecipeDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    private val args: RecipeDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)

        fetchMealDetails(args.mealId)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }



        return binding.root
    }

    private fun fetchMealDetails(mealId: String) {
        RetrofitInstance.api.getMealDetails(mealId).enqueue(object : Callback<MealResponse> {
            override fun onResponse(call: Call<MealResponse>, response: Response<MealResponse>) {
                response.body()?.meals?.firstOrNull()?.let { meal ->
                    binding.mealName.text = meal.strMeal
                    binding.mealIngredientsCount.text = "Ingredients: " +
                            (listOfNotNull(
                                meal.strIngredient1, meal.strIngredient2, meal.strIngredient3,
                                meal.strIngredient4, meal.strIngredient5, meal.strIngredient6,
                                meal.strIngredient7, meal.strIngredient8, meal.strIngredient9,
                                meal.strIngredient10, meal.strIngredient11, meal.strIngredient12,
                                meal.strIngredient13, meal.strIngredient14, meal.strIngredient15
                            ).filter { it.isNotBlank() }.size)

                    binding.mealDuration.text = "16 min"

                    Glide.with(requireContext())
                        .load(meal.strMealThumb)
                        .into(binding.mealImage)

                    // Build ingredients list
                    val ingredientsList = StringBuilder()
                    for (i in 1..20) {
                        val ingredient = meal.getIngredient(i)
                        val measure = meal.getMeasure(i)
                        if (!ingredient.isNullOrBlank()) {
                            ingredientsList.append("• $ingredient ($measure)\n")
                        }
                    }
                    binding.ingredientsText.text = ingredientsList.toString()

                    // Steps
                    binding.stepsText.text = meal.strInstructions
                }
            }

            override fun onFailure(call: Call<MealResponse>, t: Throwable) {
                // handle error
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
