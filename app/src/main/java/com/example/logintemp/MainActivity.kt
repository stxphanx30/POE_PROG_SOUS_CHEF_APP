package com.example.logintemp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.logintemp.databinding.ActivityMainBinding
import com.example.logintemp.util.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        if (!session.isLoggedIn()) {
            finish() // or redirect to login
            return
        }

        val navView: BottomNavigationView = binding.navView

        // ✅ Correct way in an Activity
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        navView.setupWithNavController(navController)


        // Hide bottom nav on specific destinations
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_profile,
                R.id.notificationsFragment,
                R.id.navigation_mealplan,
                R.id.navigation_addrecipe1,
                R.id.navigation_addrecipe2,
                R.id.navigation_mealplan2,
                R.id.navigation_addrecipe3 -> binding.navView.visibility = View.GONE
                else -> binding.navView.visibility = View.VISIBLE
            }
        }


    }
}