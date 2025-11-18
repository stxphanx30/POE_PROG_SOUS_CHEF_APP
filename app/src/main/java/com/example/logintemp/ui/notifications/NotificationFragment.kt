package com.example.logintemp.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.logintemp.R
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.mealplan3.MealPlanWithRecipe
import com.example.logintemp.databinding.FragmentNotificationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView3.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        loadMealPlanNotifications()
    }

    private fun loadMealPlanNotifications() {
        val db = AppDatabase.getInstance(requireContext())
        val dao = db.mealPlanDao()

        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                dao.getAllPlansWithRecipe()
            }

            displayNotifications(items)
        }
    }

    private fun displayNotifications(list: List<MealPlanWithRecipe>) {
        val container = binding.notificationsContainer
        container.removeAllViews()

        if (list.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "No notifications"
                textSize = 15f
                setTextColor(resources.getColor(R.color.gray_400))
                setPadding(16, 30, 16, 30)
            }
            container.addView(tv)
            return
        }

        list.forEach { item ->
            // inflate TON item layout
            val notifView = layoutInflater.inflate(R.layout.item_notification_template, container, false)

            val tvTitle = notifView.findViewById<TextView>(R.id.notificationTitle)
            val tvMessage = notifView.findViewById<TextView>(R.id.notificationMessage)
            val tvTime = notifView.findViewById<TextView>(R.id.notificationTime)

            tvTitle.text = item.name ?: "Meal"
            tvMessage.text = "Your meal \"${item.name}\" has been created"
            tvTime.text = "just now"

            container.addView(notifView)
        }
    }

    private fun computeTimeText(epochMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - epochMillis
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)

        return when {
            minutes < 1 -> "Just now"
            minutes == 1L -> "1 min ago"
            minutes < 60 -> "$minutes min ago"
            else -> "${minutes / 60} hr ago"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}