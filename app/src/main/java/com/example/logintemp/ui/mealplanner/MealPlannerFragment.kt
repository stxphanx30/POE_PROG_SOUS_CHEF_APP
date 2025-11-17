package com.example.logintemp.ui.mealplanner

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.logintemp.R
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.mealplan3.MealPlanWithRecipe
import com.example.logintemp.databinding.FragmentMealPlannerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MealPlannerFragment : Fragment() {

    private var _binding: FragmentMealPlannerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealPlannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

binding.iconNotifications.setOnClickListener { findNavController().navigate(R.id.navigation_mealplan3) }


        // Charger et afficher le planning depuis la DB
        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            val data: List<MealPlanWithRecipe> = withContext(Dispatchers.IO) {
                db.mealPlanDao().getAllPlansWithRecipe()
            }

            if (data.isEmpty()) {
                binding.containerDateSections.removeAllViews()
                val msg = TextView(requireContext()).apply {
                    text = "No planned meals yet."
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_400))
                    textSize = 16f
                    setPadding(24, 48, 24, 0)
                }
                binding.containerDateSections.addView(msg)
            } else {
                renderMealPlans(data)
            }
            binding.btnnext.setOnClickListener {
                findNavController().navigate(R.id.navigation_mealpantry)
        }
    }}

    private fun renderMealPlans(list: List<MealPlanWithRecipe>) {
        val container = binding.containerDateSections
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        // Grouper par date
        val grouped = list.groupBy {
            val df = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
            df.format(Date(it.planDateEpoch))
        }.toSortedMap(compareBy { it }) // ordre chronologique

        grouped.forEach { (dateLabel, items) ->
            // Date header
            val title = TextView(requireContext()).apply {
                text = dateLabel
                textSize = 16f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black_900))
                setPadding(8, 24, 8, 8)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            container.addView(title)

            // Section verticale (on ne scrolle pas horizontalement)
            val daySection = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 4, 0, 16)
            }

            // Chaque recette planifiée ce jour-là
            items.forEach { row ->
                val card = inflater.inflate(R.layout.item_recipe, daySection, false)
                val img = card.findViewById<ImageView>(R.id.imgRecipe)
                val tvTitle = card.findViewById<TextView>(R.id.tvTitle)
                val tvCategory = card.findViewById<TextView>(R.id.tvCategory)
                val tvTime = card.findViewById<TextView>(R.id.tvTime)
                val tvIngredients = card.findViewById<TextView>(R.id.tvIngredients)
                val tvCreated = card.findViewById<TextView>(R.id.tvCreated)
                val btnHeart = card.findViewById<ImageButton>(R.id.btnHeart)

                // Remplir les champs
                tvTitle.text = row.name
                tvCategory.text = row.category
                if (row.cookTimeMinutes != null && row.cookTimeMinutes > 0) {
                    tvTime.text = "${row.cookTimeMinutes} min"
                    tvTime.visibility = View.VISIBLE
                } else {
                    tvTime.visibility = View.GONE
                }

                tvIngredients.visibility = View.GONE // optionnel pour le planner
                tvCreated.text = "Planned for $dateLabel"

                // Image
                if (!row.imageUri.isNullOrEmpty()) {
                    try {
                        img.setImageURI(Uri.parse(row.imageUri))
                    } catch (e: Exception) {
                        img.setImageResource(R.drawable.logo)
                    }
                } else {
                    img.setImageResource(R.drawable.logo)
                }

                // Heart button (visuel seulement)
                var liked = false
                btnHeart.setOnClickListener {
                    liked = !liked
                    btnHeart.setImageResource(
                        if (liked) R.drawable.heart_filled_red else R.drawable.heart_filled
                    )
                }

                daySection.addView(card)
            }

            container.addView(daySection)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
