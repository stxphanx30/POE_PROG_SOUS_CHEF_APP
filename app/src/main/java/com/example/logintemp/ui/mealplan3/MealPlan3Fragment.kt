package com.example.logintemp.ui.mealplan3

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.logintemp.R
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.recipe.RecipeEntity
import com.example.logintemp.data.mealplan3.MealPlanEntity
import com.example.logintemp.databinding.FragmentMealPlanBinding
import com.example.logintemp.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MealPlan3Fragment : Fragment() {

    private var _binding: FragmentMealPlanBinding? = null
    private val binding get() = _binding!!

    private lateinit var session: SessionManager

    // mapping pour le picker
    private var recipeNames: List<String> = emptyList()
    private var recipeIds: List<Long> = emptyList()
    private var selectedRecipeId: Long? = null

    private var selectedDateEpoch: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealPlanBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Charger les vraies recettes pour l’utilisateur connecté
        loadRecipesIntoPicker()

        // 2) Ouvrir le DatePicker
        binding.etDate.setOnClickListener { openDatePicker() }

        // 3) Enregistrer
        binding.btnSubmit2.setOnClickListener { saveMealPlan() }

        // Back (flèche)
        binding.imageView.setOnClickListener { findNavController().navigateUp() }

        // Si l’utilisateur choisit un nom dans l’autocomplete → on capture l’id
        binding.etRecipePicker.setOnItemClickListener { _, _, position, _ ->
            selectedRecipeId = recipeIds.getOrNull(position)
        }
        binding.etRecipePicker.setOnClickListener {
            binding.etRecipePicker.showDropDown()
        }
    }

    private fun loadRecipesIntoPicker() {
        val userId = session.getUserId()
        val db = AppDatabase.getInstance(requireContext())
        val recipeDao = db.recipeDao()

        viewLifecycleOwner.lifecycleScope.launch {
            // I/O
            val list: List<RecipeEntity> = withContext(Dispatchers.IO) {
                // Si tu as un DAO dédié, remplace par: recipeDao.getRecipesForUser(userId)
                // Ici on montre la requête "classique" SELECT * WHERE userId
                recipeDao.getAllRecipesForUser(userId)
            }

            if (list.isEmpty()) {
                recipeNames = emptyList()
                recipeIds = emptyList()
                binding.etRecipePicker.setAdapter(
                    ArrayAdapter(requireContext(), R.layout.item_category_pill, emptyList<String>())
                )
                return@launch
            }

            recipeNames = list.map { it.name }
            recipeIds = list.map { it.id.toLong() }

            val adapter = ArrayAdapter(requireContext(), R.layout.item_category_pill, recipeNames)
            binding.etRecipePicker.setAdapter(adapter)
        }
    }

    private fun openDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                // Affichage
                val formatted = "%02d/%02d/%04d".format(day, month + 1, year)
                binding.etDate.setText(formatted)

                // Epoch (00:00 locale)
                val c = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDateEpoch = c.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveMealPlan() {
        val pickedName = binding.etRecipePicker.text?.toString()?.trim().orEmpty()
        val recipeId = selectedRecipeId
        val dateEpoch = selectedDateEpoch

        if (pickedName.isEmpty() || recipeId == null || dateEpoch == null) {
            Toast.makeText(requireContext(), "Select recipe and date", Toast.LENGTH_SHORT).show()
            return
        }

        val db = AppDatabase.getInstance(requireContext())
        val mealDao = db.mealPlanDao()

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                mealDao.insert(
                    MealPlanEntity(
                        recipeId = recipeId,
                        planDateEpoch = dateEpoch
                    )
                )
            }

            findNavController().navigate(R.id.navigation_mealplanner)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
