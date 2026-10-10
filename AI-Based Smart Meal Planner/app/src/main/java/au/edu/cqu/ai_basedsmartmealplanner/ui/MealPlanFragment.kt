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

        savedPlansAdapter = SavedPlansAdapter(
            onPlanClicked = { selectedPlan ->
                viewModel.restoreSavedPlan(selectedPlan)
                Toast.makeText(requireContext(), "Loaded: ${selectedPlan.title}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClicked = { planToDelete ->
                viewModel.deleteSavedPlan(planToDelete)
            }
        )

        rvSavedPlans.layoutManager = LinearLayoutManager(requireContext())
        rvSavedPlans.adapter = savedPlansAdapter

        btnGenerate.setOnClickListener {
            val currentIngredientCount = viewModel.profileManager.getProfile().availableIngredients.size

            val dialog = android.app.AlertDialog.Builder(requireContext()).create()

            // Create a custom rounded background for the dialog itself
            val layout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(72, 72, 72, 48)
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.WHITE)
                    cornerRadius = 64f // Rounds the corners of the white dialog box
                }
            }

            val title = TextView(requireContext()).apply {
                text = "Choose Generation Mode"
                textSize = 22f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.parseColor("#1C1B1F"))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(0, 0, 0, 64)
            }

            // MaterialButton gives us the beautiful corner radius and elevation
            val btnPantry = com.google.android.material.button.MaterialButton(requireContext()).apply {
                isAllCaps = false
                cornerRadius = 40 // Perfectly rounded pill shape
                backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#588064"))
                setTextColor(android.graphics.Color.WHITE)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(32, 40, 32, 40)

                // Format text: Bold title, smaller description
                val buttonText = android.text.SpannableString("PANTRY ONLY\nUse only what I have, perfect for skipping the store")
                buttonText.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, 11, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                buttonText.setSpan(android.text.style.RelativeSizeSpan(0.8f), 11, buttonText.length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                text = buttonText

                setOnClickListener {
                    if (currentIngredientCount < 5) {
                        Toast.makeText(requireContext(), "You need at least 5 ingredients in your profile to use Pantry Only mode!", Toast.LENGTH_LONG).show()
                    } else {
                        viewModel.generateMealPlan(isPantryOnly = true)
                        dialog.dismiss()
                    }
                }
            }

            val spacer = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 40)
            }

            val btnFull = com.google.android.material.button.MaterialButton(requireContext()).apply {
                isAllCaps = false
                cornerRadius = 40
                backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#588064"))
                setTextColor(android.graphics.Color.WHITE)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(32, 40, 32, 40)

                val buttonText2 = android.text.SpannableString("FULL MENU\nBest recipes using pantry as base, add missing items to grocery list")
                buttonText2.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, 9, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                buttonText2.setSpan(android.text.style.RelativeSizeSpan(0.8f), 9, buttonText2.length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                text = buttonText2

                setOnClickListener {
                    viewModel.generateMealPlan(isPantryOnly = false)
                    dialog.dismiss()
                }
            }

            val btnCancel = android.widget.Button(requireContext(), null, android.R.attr.borderlessButtonStyle).apply {
                text = "CANCEL"
                setTextColor(android.graphics.Color.parseColor("#588064"))
                setOnClickListener { dialog.dismiss() }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.CENTER
                    topMargin = 32
                }
            }

            layout.addView(title)
            layout.addView(btnPantry)
            layout.addView(spacer)
            layout.addView(btnFull)
            layout.addView(btnCancel)

            dialog.setView(layout)

            // This makes the sharp square corners of the default dialog invisible,
            // letting our beautiful rounded layout shine through.
            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

            dialog.show()
        }

        btnSave.setOnClickListener {
            val input = android.widget.EditText(requireContext()).apply {
                hint = "e.g., Pre-Exam Week Prep"
                setPadding(48, 32, 48, 32)
            }

            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Save Meal Plan")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val title = if (input.text.isNotBlank()) input.text.toString() else "Saved Meal Plan"
                    viewModel.saveCurrentMealPlan(title)
                    Toast.makeText(requireContext(), "Meal plan saved!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
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