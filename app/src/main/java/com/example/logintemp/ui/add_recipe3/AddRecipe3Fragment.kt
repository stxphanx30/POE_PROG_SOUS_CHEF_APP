package com.example.logintemp.ui.add_recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.logintemp.R
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.recipe.RecipeRepository
import com.example.logintemp.databinding.FragmentCreateRecipe3Binding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class AddRecipe3Fragment : Fragment() {
    private val vm: CreateRecipeViewModel by activityViewModels {
        val db = AppDatabase.getInstance(requireContext())
        val repo = RecipeRepository(db.recipeDao())
        CreateRecipeVMFactory(repo)
    }

    private var _binding: FragmentCreateRecipe3Binding? = null
    private val binding get() = _binding!!

    // Simple in-memory steps (add shared ViewModel later if you want to persist across screens)
    private val steps = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateRecipe3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Open bottom sheet to add a step
        binding.imageButton.setOnClickListener { showAddStepSheet() }

        // Cancel goes back
        binding.btncancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Finish: here you can save to DB, then navigate back to My Recipes
        binding.btnnext.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val userId = com.example.logintemp.util.SessionManager(requireContext()).getUserId()

                val id = vm.save(userId)
                if (id > 0L) {
                    vm.clear()
                    findNavController().navigate(R.id.navigation_myrecipe)
                } else {
                    android.widget.Toast.makeText(requireContext(), "Save failed", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }


        renderNumberedList()
    }

    private fun showAddStepSheet() {
        val dialog = BottomSheetDialog(requireContext(), R.style.ThemeOverlay_SousChef_DatePicker)
        val sheet = layoutInflater.inflate(R.layout.bottom_sheet_add_step, null)
        dialog.setContentView(sheet)

        val etStep = sheet.findViewById<EditText>(R.id.etStep)
        val btnAdd = sheet.findViewById<Button>(R.id.btnAddStep)

        btnAdd.setOnClickListener {
            val text = etStep.text?.toString()?.trim().orEmpty()

            // ✅ Validation
            if (text.isEmpty()) {
                etStep.error = getString(R.string.field_required)
                return@setOnClickListener
            }

            vm.steps += text


            renderNumberedList()


            dialog.dismiss()
        }

        dialog.show()
    }


    private fun renderNumberedList() {
        val builder = StringBuilder()
        vm.steps.forEachIndexed { index, step ->
            builder.append("${index + 1}. $step\n")
        }
        binding.tvIngredients.text = builder.toString().trim()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
