package com.example.logintemp.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.logintemp.R
import com.example.logintemp.api.RetrofitInstance
import com.example.logintemp.data.MealDBData.CategoryResponse
import com.example.logintemp.data.MealDBData.Meal
import com.example.logintemp.data.MealDBData.MealResponse
import com.example.logintemp.databinding.FragmentSearchBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchAdapter: SearchResultsAdapter
    private var allResults: List<Meal> = emptyList()
    private var searchJob: Job? = null
    private var selectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchAdapter = SearchResultsAdapter(emptyList()) { meal ->
            val action = SearchFragmentDirections
                .actionSearchFragmentToRecipeDetail(meal.idMeal)
            findNavController().navigate(action)
        }

        binding.recyclerSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        setupCategoryChipDropdown()
        setupSearchInput()
    }

    /** Use custom orange chip as a dropdown trigger */
    private fun setupCategoryChipDropdown() {
        RetrofitInstance.api.getCategories().enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(
                call: Call<CategoryResponse>,
                response: Response<CategoryResponse>
            ) {
                val categories = response.body()?.categories?.map { it.strCategory } ?: emptyList()
                val items = listOf("All") + categories

                val popup = ListPopupWindow(requireContext()).apply {
                    anchorView = binding.categoryDropdown
                    isModal = true
                    setAdapter(
                        ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            items
                        )
                    )
                    setOnItemClickListener { _, _, position, _ ->
                        val choice = items[position]
                        binding.categoryDropdown.text = choice
                        selectedCategory = if (choice == "All") null else choice
                        applyFilters()
                        dismiss()
                    }
                }

                binding.categoryDropdown.setOnClickListener {
                    if (popup.isShowing) popup.dismiss() else popup.show()
                }
            }

            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                // optional: Toast/log
            }
        })
    }

    private fun setupSearchInput() {
        // IMPORTANT: make sure your included view_search_bar has an EditText with id etSearch.
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim().orEmpty()
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300)
                    if (query.isNotEmpty()) performSearch(query)
                    else {
                        allResults = emptyList()
                        searchAdapter.updateResults(emptyList())
                    }
                }
            }
        })
    }

    private fun performSearch(query: String) {
        RetrofitInstance.api.searchMeals(query).enqueue(object : Callback<MealResponse> {
            override fun onResponse(call: Call<MealResponse>, response: Response<MealResponse>) {
                allResults = response.body()?.meals ?: emptyList()
                applyFilters()
            }
            override fun onFailure(call: Call<MealResponse>, t: Throwable) {
                // optional: Toast/log
            }
        })
    }

    private fun applyFilters() {
        val filtered = if (selectedCategory.isNullOrEmpty()) {
            allResults
        } else {
            allResults.filter { it.strCategory.equals(selectedCategory, ignoreCase = true) }
        }
        searchAdapter.updateResults(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
