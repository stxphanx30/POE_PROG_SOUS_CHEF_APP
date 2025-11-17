package com.example.logintemp.ui.mealpantry

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.logintemp.R
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.pantry.PantryRepository
import com.example.logintemp.databinding.FragmentMealpantryBinding
import com.example.logintemp.util.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MealPantryFragment : Fragment() {

    private var _binding: FragmentMealpantryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MealPantryFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealpantryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())
        val userId = session.getUserId()
        val repo = PantryRepository(AppDatabase.getInstance(requireContext()).pantryDao())
        val factory = MealPantryFragmentViewModelFactory(repo, userId)
        viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[MealPantryFragmentViewModel::class.java]

        // Observe pantry lists
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.haveItems.collectLatest {
                renderBulletedList(binding.tvIngredients1, it.map { item -> item.name })
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.toGetItems.collectLatest {
                renderBulletedList(binding.tvIngredients, it.map { item -> item.name })
            }
        }

        binding.btnplanner.setOnClickListener {
            findNavController().navigate(R.id.navigation_mealplanner)
        }

        binding.iconNotifications.setOnClickListener {
            viewModel.clearCategory("Have")
            viewModel.clearCategory("To Get")
            Toast.makeText(requireContext(), "Cleared all lists", Toast.LENGTH_SHORT).show()
        }

        binding.iconNotifications2.setOnClickListener {
            showAddPantrySheet()
        }
    }

    private fun showAddPantrySheet() {
        val dialog = BottomSheetDialog(requireContext(), R.style.ThemeOverlay_SousChef_DatePicker)
        val sheet = layoutInflater.inflate(R.layout.bottomsheet_add_item, null)
        dialog.setContentView(sheet)

        val etItem = sheet.findViewById<EditText>(R.id.etItem)
        val etCategory = sheet.findViewById<MaterialAutoCompleteTextView>(R.id.etCategory)
        val btnAdd = sheet.findViewById<Button>(R.id.btnAddItem)

        val options = listOf("Have", "To Get")
        etCategory.setAdapter(ArrayAdapter(requireContext(), R.layout.item_category_pill, options))
        etCategory.setOnClickListener { etCategory.showDropDown() }

        btnAdd.setOnClickListener {
            val name = etItem.text.toString().trim()
            val category = etCategory.text.toString().trim()

            if (name.isEmpty() || category.isEmpty()) {
                Toast.makeText(requireContext(), "Please complete all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addItem(name, category)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun renderBulletedList(target: TextView, items: List<String>) {
        if (items.isEmpty()) {
            target.text = ""
            return
        }
        val d = resources.displayMetrics.density
        val gap = (8 * d).toInt()
        val radius = (4 * d).toInt()

        val sb = SpannableStringBuilder()
        items.forEachIndexed { i, text ->
            val start = sb.length
            sb.append(text)
            sb.setSpan(BulletSpan(gap, 0xFF333333.toInt(), radius), start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (i != items.lastIndex) sb.append("\n")
        }
        target.text = sb
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
