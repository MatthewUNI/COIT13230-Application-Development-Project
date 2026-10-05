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

        savedPlansAdapter = SavedPlansAdapter { selectedPlan ->
            viewModel.restoreSavedPlan(selectedPlan)
            Toast.makeText(requireContext(), "Loaded: ${selectedPlan.title}", Toast.LENGTH_SHORT).show()
        }

        rvSavedPlans.layoutManager = LinearLayoutManager(requireContext())
        rvSavedPlans.adapter = savedPlansAdapter

        btnGenerate.setOnClickListener {
            val dialog = android.app.AlertDialog.Builder(requireContext()).create()

            val layout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(64, 64, 64, 32)
            }

            val title = TextView(requireContext()).apply {
                text = "Choose Generation Mode"
                textSize = 20f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, 48)
            }

            val btnPantry = Button(requireContext()).apply {
                text = "PANTRY ONLY\n(Use only what I have, perfect for skipping the store)"
                isAllCaps = false
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(32, 24, 32, 24)
                setOnClickListener {
                    viewModel.generateMealPlan(isPantryOnly = true)
                    dialog.dismiss()
                }
            }

            val spacer = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 32)
            }

            val btnFull = Button(requireContext()).apply {
                text = "FULL MENU\n(Best recipes using pantry as base, add missing items to grocery list)"
                isAllCaps = false
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(32, 24, 32, 24)
                setOnClickListener {
                    viewModel.generateMealPlan(isPantryOnly = false)
                    dialog.dismiss()
                }
            }

            val btnCancel = Button(requireContext(), null, android.R.attr.borderlessButtonStyle).apply {
                text = "CANCEL"
                setOnClickListener { dialog.dismiss() }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.END
                    topMargin = 32
                }
            }

            layout.addView(title)
            layout.addView(btnPantry)
            layout.addView(spacer)
            layout.addView(btnFull)
            layout.addView(btnCancel)

            dialog.setView(layout)
            dialog.show()
        }

        btnSave.setOnClickListener {
            viewModel.saveCurrentMealPlan()
            Toast.makeText(requireContext(), "Meal plan saved to history!", Toast.LENGTH_SHORT).show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.savedPlans.collect { plans ->
                    savedPlansAdapter.submitList(plans)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is MealUiState.Idle -> {
                            tvNoPlan.visibility = View.VISIBLE
                            tvNoPlan.text = "No meal plan generated yet."
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
                            val meals = state.mealPlan.dailyMeals
                            val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

                            days.forEach { day ->
                                val dayMeals = meals.filter { it.day.equals(day, ignoreCase = true) }
                                if (dayMeals.isNotEmpty()) {
                                    val dayView = layoutInflater.inflate(R.layout.item_day_meals, mealContainer, false)

                                    val textDay = dayView.findViewById<TextView>(R.id.textDay)
                                    val textBreakfast = dayView.findViewById<TextView>(R.id.textBreakfast)
                                    val textLunch = dayView.findViewById<TextView>(R.id.textLunch)
                                    val textDinner = dayView.findViewById<TextView>(R.id.textDinner)

                                    val breakfast = dayMeals.find { it.mealType.equals("Breakfast", ignoreCase = true) }
                                    val lunch = dayMeals.find { it.mealType.equals("Lunch", ignoreCase = true) }
                                    val dinner = dayMeals.find { it.mealType.equals("Dinner", ignoreCase = true) }

                                    textDay.text = day
                                    textBreakfast.text = "Breakfast - ${breakfast?.title ?: "Not available"}"
                                    textLunch.text = "Lunch - ${lunch?.title ?: "Not available"}"
                                    textDinner.text = "Dinner - ${dinner?.title ?: "Not available"}"

                                    mealContainer.addView(dayView)
                                }
                            }
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