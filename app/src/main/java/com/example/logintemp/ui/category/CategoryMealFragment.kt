// file: app/src/main/java/com/example/logintemp/ui/category/CategoryMealFragment.kt
package com.example.logintemp.ui.category

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.logintemp.R
import com.example.logintemp.api.RetrofitInstance
import com.example.logintemp.data.MealDBData.Meal
import com.example.logintemp.data.MealDBData.MealResponse
import com.example.logintemp.data.MealDBData.getIngredient
import com.example.logintemp.data.MealDBData.getMeasure
import com.example.logintemp.databinding.FragmentCategoryMealBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class CategoryMealFragment : Fragment() {

    private var _binding: FragmentCategoryMealBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CategoryMealAdapter
    private var allMeals: List<Meal> = emptyList()

    private val mealDetailsCache = ConcurrentHashMap<String, Meal>()
    private val ingredientCounts = ConcurrentHashMap<String, Int>()

    // filters
    private var ingredientFilter: String = ""
    private var ingredientCountRange: Pair<Int, Int>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryMealBinding.inflate(inflater, container, false)

        adapter = CategoryMealAdapter(emptyList()) { meal ->
            val action = CategoryMealFragmentDirections.actionCategoryMealToRecipeDetail(meal.idMeal)
            findNavController().navigate(action)
        }

        binding.recyclerSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSearchResults.adapter = adapter

        // Back
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        // Read category argument
        val category = arguments?.getString("categoryName") ?: arguments?.getString("category") ?: ""
        setupHeaderForCategory(category)

        // search/filter
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.filterIngredients.setOnClickListener { showIngredientFilterDialog() }
        binding.filterTime.setOnClickListener { showTimeFilterDialog() }

        fetchMealsByCategory(category)

        return binding.root
    }

    private fun setupHeaderForCategory(category: String) {
        val display = if (category.isBlank()) "All" else category
        binding.titleRight.text = display
        binding.titleLeft.text = display

        // placeholder image, will be replaced after fetch
        binding.headerCategoryImage.setImageResource(R.drawable.logo)
    }

    private fun fetchMealsByCategory(category: String) {
        RetrofitInstance.api.getMealsByCategory(category).enqueue(object : Callback<MealResponse> {
            override fun onResponse(call: Call<MealResponse>, response: Response<MealResponse>) {
                val meals = response.body()?.meals ?: emptyList()
                allMeals = meals
                adapter.updateMeals(meals)

                val firstThumb = meals.firstOrNull()?.strMealThumb
                if (!firstThumb.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(firstThumb)
                        .centerCrop()
                        .into(binding.headerCategoryImage)
                }

                prefetchMealDetails(meals)
            }

            override fun onFailure(call: Call<MealResponse>, t: Throwable) {
                allMeals = emptyList()
                adapter.updateMeals(emptyList())
            }
        })
    }

    private fun prefetchMealDetails(meals: List<Meal>) {
        meals.forEach { meal ->
            val id = meal.idMeal
            if (id.isNullOrEmpty()) return@forEach
            if (mealDetailsCache.containsKey(id)) return@forEach

            RetrofitInstance.api.getMealDetails(id).enqueue(object : Callback<MealResponse> {
                override fun onResponse(call: Call<MealResponse>, response: Response<MealResponse>) {
                    val detail = response.body()?.meals?.firstOrNull()
                    if (detail != null) {
                        mealDetailsCache[id] = detail
                        ingredientCounts[id] = countIngredients(detail)
                        // push counts to adapter
                        adapter.ingredientCounts = ingredientCounts.toMap()
                    }
                }
                override fun onFailure(call: Call<MealResponse>, t: Throwable) { /* ignore */ }
            })
        }
    }

    private fun countIngredients(meal: Meal): Int {
        var c = 0
        for (i in 1..20) {
            val ing = meal.getIngredient(i)
            if (!ing.isNullOrBlank()) c++
        }
        return c
    }

    private fun showIngredientFilterDialog() {
        val edit = EditText(requireContext())
        edit.hint = "e.g. garlic"

        AlertDialog.Builder(requireContext())
            .setTitle("Filter by ingredient")
            .setView(edit)
            .setPositiveButton("Apply") { d, _ ->
                ingredientFilter = edit.text.toString().trim()
                applyAllFiltersAndSearch()
                d.dismiss()
            }
            .setNegativeButton("Clear") { d, _ ->
                ingredientFilter = ""
                applyAllFiltersAndSearch()
                d.dismiss()
            }
            .show()
    }

    private fun showTimeFilterDialog() {
        val options = arrayOf("Any", "≤ 5 ingredients (quick)", "6 - 10", "> 10")
        AlertDialog.Builder(requireContext())
            .setTitle("Filter by complexity")
            .setItems(options) { _, which ->
                ingredientCountRange = when (which) {
                    0 -> null
                    1 -> Pair(0, 5)
                    2 -> Pair(6, 10)
                    3 -> Pair(11, Int.MAX_VALUE)
                    else -> null
                }
                applyAllFiltersAndSearch()
            }
            .show()
    }

    private fun applyAllFiltersAndSearch() {
        val q = binding.etSearch.text?.toString()?.trim().orEmpty()
        filterList(q)
    }

    private fun filterList(query: String) {
        lifecycleScope.launch {
            val q = query.lowercase(Locale.getDefault())
            val ingSub = ingredientFilter.lowercase(Locale.getDefault())
            val range = ingredientCountRange

            val filtered = allMeals.filter { meal ->
                // name/area filter
                val name = (meal.strMeal ?: "").lowercase(Locale.getDefault())
                val area = (meal.strArea ?: "").lowercase(Locale.getDefault())
                if (q.isNotEmpty() && !(name.contains(q) || area.contains(q))) return@filter false

                // ingredient substring filter (requires detail cached)
                if (ingSub.isNotEmpty()) {
                    val detail = mealDetailsCache[meal.idMeal]
                    if (detail == null) return@filter false
                    var found = false
                    for (i in 1..20) {
                        val ing = detail.getIngredient(i)
                        if (!ing.isNullOrBlank() && ing.lowercase(Locale.getDefault()).contains(ingSub)) {
                            found = true
                            break
                        }
                    }
                    if (!found) return@filter false
                }

                // ingredient count range
                if (range != null) {
                    val cnt = ingredientCounts[meal.idMeal] ?: return@filter false
                    val (min, max) = range
                    if (cnt < min || cnt > max) return@filter false
                }

                true
            }

            withContext(Dispatchers.Main) {
                adapter.updateMeals(filtered)
                adapter.ingredientCounts = ingredientCounts.toMap()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}