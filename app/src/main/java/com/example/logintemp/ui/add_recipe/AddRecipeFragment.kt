package com.example.logintemp.ui.add_recipe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.logintemp.R
import com.example.logintemp.api.RetrofitInstance
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.MealDBData.CategoryResponse
import com.example.logintemp.data.recipe.RecipeRepository
import com.example.logintemp.databinding.FragmentCreaterecipe1Binding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.net.toUri

class AddRecipeFragment : Fragment() {
    private val vm: CreateRecipeViewModel by activityViewModels {
        val db = AppDatabase.getInstance(requireContext())
        val repo = RecipeRepository(db.recipeDao())
        CreateRecipeVMFactory(repo)
    }

    private var _binding: FragmentCreaterecipe1Binding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: String? = null
    private var categoryAdapter: ArrayAdapter<String>? = null

    // SAF picker for image upload
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        requireContext().contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        binding.fileRow.visibility = View.VISIBLE
        binding.tvFileName.text = getDisplayName(uri)
        selectedImageUri = uri.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreaterecipe1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Prepare empty adapter (we'll fill it after fetch)
        categoryAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_category_pill,
            mutableListOf()
        )
        binding.etCategory.setAdapter(categoryAdapter)
        binding.etCategory.setOnClickListener { binding.etCategory.showDropDown() }

        // Fetch categories from API
        fetchCategories()
        binding.ettime.setOnClickListener {  showCookTimePicker()}
        // Upload File
        binding.btnUpload.setOnClickListener { pickImage.launch(arrayOf("image/*")) }
        binding.btnClearFile.setOnClickListener {
            selectedImageUri = null
            binding.fileRow.visibility = View.GONE
        }

        // Back/Cancel
        binding.btncancel.setOnClickListener { findNavController().navigateUp() }
        binding.imageView.setOnClickListener { findNavController().navigateUp() }

        // Next
        binding.btnnext.setOnClickListener {
            vm.title    = binding.etrecipename.text?.toString()?.trim()
            vm.category = binding.etCategory.text?.toString()?.trim()

            // Optional: if you added a cooking time field (EditText ettime)
            vm.cookTimeMinutes = binding.ettime.text?.toString()?.trim()?.toIntOrNull()

            // If no image selected → use a fallback drawable as resource URI
            vm.imageUri = (selectedImageUri ?: run {
                val resId = R.drawable.sample_biryani // or your own placeholder drawable
                "android.resource://${requireContext().packageName}/$resId".toUri().toString()
            })

            findNavController().navigate(R.id.navigation_addrecipe2)
        }
    }
    private fun showCookTimePicker() {
        // Simple NumberPicker dans un AlertDialog
        val picker = android.widget.NumberPicker(requireContext()).apply {
            minValue = 1
            maxValue = 300   // jusqu’à 5h
            value = vm.cookTimeMinutes ?: 15
            wrapSelectorWheel = false
        }
        val dlg = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cooking time (min)")
            .setView(picker)
            .setPositiveButton("OK") { d, _ ->
                val v = picker.value
                vm.cookTimeMinutes = v
                binding.ettime.setText(v.toString())
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dlg.show()
    }
    private fun fetchCategories() {
        // Optional: disable field while loading
        binding.etCategory.isEnabled = false

        RetrofitInstance.api.getCategories().enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(
                call: Call<CategoryResponse>,
                response: Response<CategoryResponse>
            ) {
                val list = response.body()?.categories?.mapNotNull { it.strCategory } ?: emptyList()
                if (isAdded) {
                    categoryAdapter?.clear()
                    categoryAdapter?.addAll(list)
                    categoryAdapter?.notifyDataSetChanged()
                    binding.etCategory.isEnabled = true
                }
            }

            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                if (isAdded) {
                    binding.etCategory.isEnabled = true
                    Toast.makeText(requireContext(), "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun getDisplayName(uri: Uri): String {
        val cr = requireContext().contentResolver
        cr.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (c.moveToFirst() && idx >= 0) return c.getString(idx)
        }
        return uri.lastPathSegment ?: "file"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
