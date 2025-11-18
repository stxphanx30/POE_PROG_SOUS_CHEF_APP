package com.example.logintemp.ui.categories

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.logintemp.R
import com.example.logintemp.api.RetrofitInstance
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.MealDBData.CategoryResponse
import com.example.logintemp.data.user.UserRepository
import com.example.logintemp.databinding.FragmentCategoriesBinding
import com.example.logintemp.ui.home.HomeFragmentDirections
import com.example.logintemp.ui.home.adapters.CategoryAdapter
import com.example.logintemp.util.SessionManager
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private lateinit var session: SessionManager
    private lateinit var repo: UserRepository
    private lateinit var categoryAdapter: CategoryAdapter
    private var currentUserId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        session = SessionManager(requireContext())
        repo = UserRepository(AppDatabase.getInstance(requireContext()).userDao())
        currentUserId = session.getUserId()


        setupRecyclerView()
        fetchCategories()

        binding.arrowback.setOnClickListener {
            findNavController().navigate(R.id.action_categoriesFragment_to_homeFragment)
        }


        return root
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(listOf()) { category ->
            val action = CategoriesFragmentDirections
                .actionCategoriesFragmentToCategoryMealFragment(category.strCategory)

            findNavController().navigate(action)
        }

        binding.categoriesRecycler.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = categoryAdapter
        }
    }




    private fun fetchCategories() {
        RetrofitInstance.api.getCategories().enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(
                call: Call<CategoryResponse>,
                response: Response<CategoryResponse>
            ) {
                response.body()?.categories?.let {
                    categoryAdapter.updateCategories(it)
                }
            }

            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                // TODO: handle failure gracefully (toast/log)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
