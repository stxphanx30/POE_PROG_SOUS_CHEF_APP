package com.example.logintemp.ui.mealplan2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.logintemp.R
import com.example.logintemp.databinding.FragmentGetmeal2Binding

class MealPlan2Fragment : Fragment() {

    private var _binding: FragmentGetmeal2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGetmeal2Binding.inflate(inflater, container, false)
        return binding.root
    }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
          super.onViewCreated(view, savedInstanceState)


             binding.btnStart.setOnClickListener {
                 findNavController().navigate(R.id.navigation_mealplanner)
             }
    //        // to be added for the skip button
    //        // binding.btnNext.setOnClickListener {
    //        //     findNavController().navigate(R.id.navigation_getmeal2)
    //
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
