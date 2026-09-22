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

        viewModel =
            ViewModelProvider(requireActivity())[MealPlannerViewModel::class.java]

        val btnGenerate =
            view.findViewById<Button>(R.id.buttonGenerateMealPlan)

        val btnSave =
            view.findViewById<Button>(R.id.buttonSavePlan)

        val tvNoPlan =
            view.findViewById<TextView>(R.id.textNoMealPlan)

        val mealContainer =
            view.findViewById<LinearLayout>(R.id.mealPlanContainer)

        val rvSavedPlans =
            view.findViewById<RecyclerView>(R.id.recyclerViewSavedPlans)

        // Set up saved meal plan history
        savedPlansAdapter = SavedPlansAdapter { selectedPlan ->

            viewModel.restoreSavedPlan(selectedPlan)

            Toast.makeText(
                requireContext(),
                "Loaded: ${selectedPlan.title}",
                Toast.LENGTH_SHORT
            ).show()
        }

        rvSavedPlans.layoutManager =
            LinearLayoutManager(requireContext())

        rvSavedPlans.adapter = savedPlansAdapter

        // Generate meal plan
        btnGenerate.setOnClickListener {
            viewModel.generateMealPlan()
        }

        // Save current generated plan
        btnSave.setOnClickListener {

            viewModel.saveCurrentMealPlan()

            Toast.makeText(
                requireContext(),
                "Meal plan saved to history!",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Observe saved plans
        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.savedPlans.collect { plans ->
                    savedPlansAdapter.submitList(plans)
                }
            }
        }

        // Observe meal plan
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
                            tvNoPlan.text =
                                "Generating your personalized meal plan..."

                            mealContainer.visibility = View.GONE
                            btnSave.visibility = View.GONE
                            btnGenerate.isEnabled = false
                        }

                        is MealUiState.Success -> {

                            tvNoPlan.visibility = View.GONE
                            mealContainer.visibility = View.VISIBLE
                            btnSave.visibility = View.VISIBLE
                            btnGenerate.isEnabled = true

                            // Remove previous day views
                            mealContainer.removeAllViews()

                            val meals = state.mealPlan.dailyMeals

                            val days = listOf(
                                "Monday",
                                "Tuesday",
                                "Wednesday",
                                "Thursday",
                                "Friday",
                                "Saturday",
                                "Sunday"
                            )

                            days.forEach { day ->

                                val dayMeals = meals.filter { meal ->
                                    meal.day.equals(
                                        day,
                                        ignoreCase = true
                                    )
                                }

                                if (dayMeals.isNotEmpty()) {

                                    // Inflate the day card from XML
                                    val dayView =
                                        layoutInflater.inflate(
                                            R.layout.item_day_meals,
                                            mealContainer,
                                            false
                                        )

                                    val textDay =
                                        dayView.findViewById<TextView>(
                                            R.id.textDay
                                        )

                                    val textBreakfast =
                                        dayView.findViewById<TextView>(
                                            R.id.textBreakfast
                                        )

                                    val textLunch =
                                        dayView.findViewById<TextView>(
                                            R.id.textLunch
                                        )

                                    val textDinner =
                                        dayView.findViewById<TextView>(
                                            R.id.textDinner
                                        )

                                    // Find each meal type
                                    val breakfast = dayMeals.find { meal ->
                                        meal.mealType.equals(
                                            "Breakfast",
                                            ignoreCase = true
                                        )
                                    }

                                    val lunch = dayMeals.find { meal ->
                                        meal.mealType.equals(
                                            "Lunch",
                                            ignoreCase = true
                                        )
                                    }

                                    val dinner = dayMeals.find { meal ->
                                        meal.mealType.equals(
                                            "Dinner",
                                            ignoreCase = true
                                        )
                                    }

                                    // Display generated meal data
                                    textDay.text = day

                                    textBreakfast.text =
                                        "Breakfast - ${breakfast?.title ?: "Not available"}"

                                    textLunch.text =
                                        "Lunch - ${lunch?.title ?: "Not available"}"

                                    textDinner.text =
                                        "Dinner - ${dinner?.title ?: "Not available"}"

                                    // Add completed day card
                                    mealContainer.addView(dayView)
                                }
                            }
                        }

                        is MealUiState.Error -> {

                            tvNoPlan.visibility = View.VISIBLE
                            tvNoPlan.text =
                                "Error: ${state.message}"

                            mealContainer.visibility = View.GONE
                            btnSave.visibility = View.GONE
                            btnGenerate.isEnabled = true

                            Toast.makeText(
                                requireContext(),
                                state.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }
}