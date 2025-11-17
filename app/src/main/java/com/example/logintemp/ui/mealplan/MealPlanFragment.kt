package com.example.logintemp.ui.mealplan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.logintemp.R
import com.example.logintemp.databinding.FragmentGetmeal1Binding

class MealPlanFragment : Fragment() {

    private var _binding: FragmentGetmeal1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGetmeal1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // When "Next" button is clicked → navigate to MealPlan2 screen
        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.navigation_mealplan2)
        }

        // If needed, you can restore the old navigation below:
        // binding.btnNext.setOnClickListener {
        //     findNavController().navigate(R.id.navigation_getmeal2)
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
