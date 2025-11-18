package com.example.logintemp.ui.mealplan3

import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.logintemp.MainActivity
import com.example.logintemp.R
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.mealplan3.MealPlanEntity
import com.example.logintemp.data.recipe.RecipeEntity
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

    companion object {
        private const val REQ_POST_NOTIF = 1001
        private const val CHANNEL_ID = "souschef_mealplan_channel"
    }

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
                // getAllRecipesForUser should return List<RecipeEntity>
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

            // safe mapping: handle null names defensively
            recipeNames = list.map { it.name ?: "Untitled" }
            recipeIds = list.map { it.id }

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
            val mealPlanId = withContext(Dispatchers.IO) {
                mealDao.insert(
                    MealPlanEntity(
                        recipeId = recipeId,
                        planDateEpoch = dateEpoch
                    )
                )
            }

            // notify user locally that meal plan was created
            sendLocalNotification(
                title = "Meal Planner",
                message = "Your meal '$pickedName' has been created!"
            )

            // navigate back to meal planner list (or wherever)
            findNavController().navigate(R.id.navigation_mealplanner)
        }
    }

    private fun sendLocalNotification(title: String, message: String) {
        val context = requireContext()
        val channelId = CHANNEL_ID

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo) // mets ton icône (ajoute-la si nécessaire)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Permission check Android 13+ (POST_NOTIFICATIONS)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                // demande simple ; callback traité dans onRequestPermissionsResult
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQ_POST_NOTIF)
                return
            }
        }

        // use time-based id to avoid collisions (safe positive int)
        val notifId = (System.currentTimeMillis() and Int.MAX_VALUE.toLong()).toInt()

        NotificationManagerCompat.from(context).notify(notifId, builder.build())
    }

    // Optional: handle user's response to permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_POST_NOTIF) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Notifications enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Notifications permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}