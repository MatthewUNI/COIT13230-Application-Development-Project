package au.edu.cqu.ai_basedsmartmealplanner.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.cqu.ai_basedsmartmealplanner.R
import au.edu.cqu.ai_basedsmartmealplanner.adapter.SavedPlansAdapter
import au.edu.cqu.ai_basedsmartmealplanner.ai.MealPlannerViewModel
import au.edu.cqu.ai_basedsmartmealplanner.ai.MealUiState
import kotlinx.coroutines.launch

class MealPlanFragment : Fragment(R.layout.fragment_meal_plan) {

    private lateinit var viewModel: MealPlannerViewModel
    private lateinit var savedPlansAdapter: SavedPlansAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MealPlannerViewModel::class.java]

        val btnGenerate = view.findViewById<Button>(R.id.buttonGenerateMealPlan)
        val btnSave = view.findViewById<Button>(R.id.buttonSavePlan)
        val tvNoPlan = view.findViewById<TextView>(R.id.textNoMealPlan)
        val mealContainer = view.findViewById<LinearLayout>(R.id.mealPlanContainer)
        val rvSavedPlans = view.findViewById<RecyclerView>(R.id.recyclerViewSavedPlans)

        // 1. Setup Saved Plans List
        savedPlansAdapter = SavedPlansAdapter { selectedPlan ->
            viewModel.restoreSavedPlan(selectedPlan)
            Toast.makeText(requireContext(), "Loaded: ${selectedPlan.title}", Toast.LENGTH_SHORT).show()
        }
        rvSavedPlans.layoutManager = LinearLayoutManager(requireContext())
        rvSavedPlans.adapter = savedPlansAdapter

        // 2. Wire Buttons
        btnGenerate.setOnClickListener {
            viewModel.generateMealPlan()
        }

        btnSave.setOnClickListener {
            viewModel.saveCurrentMealPlan()
            Toast.makeText(requireContext(), "Meal plan saved to history!", Toast.LENGTH_SHORT).show()
        }

        // 3. Observe Room Database Saved Plans
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.savedPlans.collect { plans ->
                    savedPlansAdapter.submitList(plans)
                }
            }
        }

        // 4. Observe Generation State
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is MealUiState.Idle -> {
                            tvNoPlan.visibility = View.VISIBLE
                            mealContainer.visibility = View.GONE
                            btnSave.visibility = View.GONE
                            btnGenerate.isEnabled = true
                        }
                        is MealUiState.Loading -> {
                            tvNoPlan.visibility = View.VISIBLE
                            tvNoPlan.text = "Generating your personalized meal plan..."
                            mealContainer.visibility = View.GONE
                            btnSave.visibility = View.GONE
                            btnGenerate.isEnabled = false
                        }
                        is MealUiState.Success -> {
                            tvNoPlan.visibility = View.GONE
                            mealContainer.visibility = View.VISIBLE
                            btnSave.visibility = View.VISIBLE
                            btnGenerate.isEnabled = true

                            mealContainer.removeAllViews()

                            val meals = state.mealPlan.dailyMeals ?: emptyList()

                            val planSummary = buildString {
                                appendLine("Personalized Meal Plan (${meals.size} meals):")
                                meals.forEachIndexed { index, meal ->
                                    appendLine("${index + 1}. ${meal.title}")
                                }
                            }

                            val summaryTextView = TextView(requireContext()).apply {
                                text = planSummary
                                textSize = 15f
                                setPadding(8, 8, 8, 8)
                            }
                            mealContainer.addView(summaryTextView)
                        }
                        is MealUiState.Error -> {
                            tvNoPlan.visibility = View.VISIBLE
                            tvNoPlan.text = "Error: ${state.message}"
                            mealContainer.visibility = View.GONE
                            btnSave.visibility = View.GONE
                            btnGenerate.isEnabled = true
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}