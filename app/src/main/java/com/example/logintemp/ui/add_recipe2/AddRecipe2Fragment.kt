package com.example.logintemp.ui.add_recipe

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.logintemp.R
import com.example.logintemp.databinding.FragmentCreateRecipe2Binding
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.recipe.RecipeRepository


class AddRecipe2Fragment : Fragment() {
    private val vm: CreateRecipeViewModel by activityViewModels {
        val db = AppDatabase.getInstance(requireContext())
        val repo = RecipeRepository(db.recipeDao())
        CreateRecipeVMFactory(repo)
    }

    private var _binding: FragmentCreateRecipe2Binding? = null
    private val binding get() = _binding!!

    // On stocke les ingrédients saisis (name, amount)
    private val ingredients = mutableListOf<Pair<String, String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateRecipe2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ouvre le bottom sheet quand on clique sur TON ImageButton (id=imageButton)
        binding.imageButton.setOnClickListener { showAddIngredientSheet() }

        // Boutons du bas (navigation laissée à ton graph)
        binding.btncancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.btnnext.setOnClickListener {

             findNavController().navigate(R.id.navigation_addrecipe3)
        }

        // État initial
        renderBulletedList()
    }

    private fun showAddIngredientSheet() {
        val dialog = BottomSheetDialog(requireContext(), R.style.ThemeOverlay_SousChef_DatePicker)
        val sheet = layoutInflater.inflate(R.layout.bottom_sheet_add_ingredient, null)
        dialog.setContentView(sheet)

        val etName = sheet.findViewById<EditText>(R.id.etIngredient)
        val etAmount = sheet.findViewById<EditText>(R.id.etAmount)
        val btnAdd = sheet.findViewById<Button>(R.id.btnAdd)

        btnAdd.setOnClickListener {
            val name = etName.text?.toString()?.trim().orEmpty()
            val amount = etAmount.text?.toString()?.trim().orEmpty()


            if (name.isEmpty() || amount.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in both fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            vm.ingredients += name to amount


            renderBulletedList()


            dialog.dismiss()
        }

        dialog.show()
    }




    // Affiche la liste dans tvIngredients sous forme de puces (aucun changement d’apparence globale)
    private fun renderBulletedList() {
        val list = vm.ingredients  // use the shared VM state

        if (list.isEmpty()) {
            binding.tvIngredients.text = getString(R.string.no_ingredients_yet)
            return
        }

        val d = resources.displayMetrics.density
        val gap = (8 * d).toInt()     // space between bullet and text
        val radius = (4 * d).toInt()  // bullet size

        val sb = SpannableStringBuilder()
        list.forEachIndexed { i, (rawName, rawAmount) ->
            val name = rawName.orEmpty().trim()
            val amount = rawAmount.orEmpty().trim()

            val line = buildString {
                if (amount.isNotEmpty()) append(amount).append(' ')
                append(name)
            }.ifBlank { "—" }

            val start = sb.length
            sb.append(line)
            sb.setSpan(
                BulletSpan(gap, Color.parseColor("#333333"), radius),
                start,
                sb.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (i != list.lastIndex) sb.append("\n")
        }
        binding.tvIngredients.text = sb
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
