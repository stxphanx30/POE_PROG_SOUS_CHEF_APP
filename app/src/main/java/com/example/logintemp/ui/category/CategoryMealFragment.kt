package com.example.logintemp.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.logintemp.api.RetrofitInstance
import com.example.logintemp.data.MealDBData.MealResponse
import com.example.logintemp.databinding.FragmentCategoryMealBinding
import com.example.logintemp.ui.category.CategoryMealAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryMealFragment : Fragment() {

    private var _binding: FragmentCategoryMealBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CategoryMealAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryMealBinding.inflate(inflater, container, false)

        adapter = CategoryMealAdapter(emptyList()) { meal ->
            // Navigate to meal detail
            val action = CategoryMealFragmentDirections
                .actionCategoryMealToRecipeDetail(meal.idMeal)
            findNavController().navigate(action)
        }

        binding.categoryRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.categoryRecycler.adapter = adapter

        // Get category passed from HomeFragment
        val category = arguments?.getString("categoryName") ?: ""
        fetchMealsByCategory(category)

        return binding.root
    }

    private fun fetchMealsByCategory(category: String) {
        RetrofitInstance.api.getMealsByCategory(category)
            .enqueue(object : Callback<MealResponse> {
                override fun onResponse(
                    call: Call<MealResponse>,
                    response: Response<MealResponse>
                ) {
                    response.body()?.meals?.let { meals ->
                        adapter.updateMeals(meals)
                    }
                }

                override fun onFailure(call: Call<MealResponse>, t: Throwable) {
                    // Handle error
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
