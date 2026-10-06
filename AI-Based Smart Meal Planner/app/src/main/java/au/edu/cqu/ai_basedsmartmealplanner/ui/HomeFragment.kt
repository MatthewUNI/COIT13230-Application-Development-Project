package au.edu.cqu.ai_basedsmartmealplanner.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import au.edu.cqu.ai_basedsmartmealplanner.R
import au.edu.cqu.ai_basedsmartmealplanner.ai.MealPlannerViewModel
import au.edu.cqu.ai_basedsmartmealplanner.ai.MealUiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var viewModel: MealPlannerViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use the same shared ViewModel as the other screens.
        viewModel =
            ViewModelProvider(requireActivity())[MealPlannerViewModel::class.java]

        val nutritionContainer =
            view.findViewById<View>(R.id.nutritionContainer)

        val mealsContainer =
            view.findViewById<View>(R.id.mealsContainer)

        val textNoNutrition =
            view.findViewById<TextView>(R.id.textNoNutrition)

        val textNoMeals =
            view.findViewById<TextView>(R.id.textNoMeals)

        val textCalories =
            view.findViewById<TextView>(R.id.textCalories)

        val textProtein =
            view.findViewById<TextView>(R.id.textProtein)

        val textCarbs =
            view.findViewById<TextView>(R.id.textCarbs)

        val textFats =
            view.findViewById<TextView>(R.id.textFats)

        val textBreakfast =
            view.findViewById<TextView>(R.id.textBreakfast)

        val textLunch =
            view.findViewById<TextView>(R.id.textLunch)

        val textDinner =
            view.findViewById<TextView>(R.id.textDinner)

        val buttonViewBreakfast =
            view.findViewById<Button>(R.id.buttonViewBreakfast)

        val buttonViewLunch =
            view.findViewById<Button>(R.id.buttonViewLunch)

        val buttonViewDinner =
            view.findViewById<Button>(R.id.buttonViewDinner)

        val buttonToggleBreakfast =
            view.findViewById<Button>(R.id.buttonToggleBreakfast)

        val buttonToggleLunch =
            view.findViewById<Button>(R.id.buttonToggleLunch)

        val buttonToggleDinner =
            view.findViewById<Button>(R.id.buttonToggleDinner)

        val buttonViewMealPlan =
            view.findViewById<Button>(R.id.buttonViewMealPlan)

        val buttonViewGroceryList =
            view.findViewById<Button>(R.id.buttonViewGroceryList)

        /*
         * Observe nutrition calculated by MealPlannerViewModel.
         */
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.nutritionInfo.collect { nutrition ->

                    if (nutrition != null) {

                        nutritionContainer.visibility = View.VISIBLE
                        textNoNutrition.visibility = View.GONE

                        textCalories.text =
                            nutrition.calories.toString()

                        textProtein.text =
                            String.format(
                                Locale.ENGLISH,
                                "%.1f g",
                                nutrition.protein
                            )

                        textCarbs.text =
                            String.format(
                                Locale.ENGLISH,
                                "%.1f g",
                                nutrition.carbs
                            )

                        textFats.text =
                            String.format(
                                Locale.ENGLISH,
                                "%.1f g",
                                nutrition.fats
                            )

                    } else {

                        nutritionContainer.visibility = View.GONE
                        textNoNutrition.visibility = View.VISIBLE
                    }
                }
            }
        }

        /*
         * Observe the current meal plan and display today's
         * Breakfast, Lunch and Dinner.
         */
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collect { state ->

                    if (state is MealUiState.Success) {

                        val meals =
                            state.mealPlan.dailyMeals

                        val today =
                            SimpleDateFormat(
                                "EEEE",
                                Locale.ENGLISH
                            ).format(Date())

                        val breakfast =
                            meals.find { meal ->
                                meal.day.equals(
                                    today,
                                    ignoreCase = true
                                ) &&
                                        meal.mealType.equals(
                                            "Breakfast",
                                            ignoreCase = true
                                        )
                            }

                        val lunch =
                            meals.find { meal ->
                                meal.day.equals(
                                    today,
                                    ignoreCase = true
                                ) &&
                                        meal.mealType.equals(
                                            "Lunch",
                                            ignoreCase = true
                                        )
                            }

                        val dinner =
                            meals.find { meal ->
                                meal.day.equals(
                                    today,
                                    ignoreCase = true
                                ) &&
                                        meal.mealType.equals(
                                            "Dinner",
                                            ignoreCase = true
                                        )
                            }

                        mealsContainer.visibility = View.VISIBLE
                        textNoMeals.visibility = View.GONE

                        textBreakfast.text =
                            breakfast?.title
                                ?: "Meal not available"

                        textLunch.text =
                            lunch?.title
                                ?: "Meal not available"

                        textDinner.text =
                            dinner?.title
                                ?: "Meal not available"

                        // Disable actions if a meal is not available.
                        buttonViewBreakfast.isEnabled =
                            breakfast != null

                        buttonToggleBreakfast.isEnabled =
                            breakfast != null

                        buttonViewLunch.isEnabled =
                            lunch != null

                        buttonToggleLunch.isEnabled =
                            lunch != null

                        buttonViewDinner.isEnabled =
                            dinner != null

                        buttonToggleDinner.isEnabled =
                            dinner != null

                    } else {

                        mealsContainer.visibility = View.GONE
                        textNoMeals.visibility = View.VISIBLE
                    }
                }
            }
        }

        /*
         * Observe which meals are excluded from today's
         * nutrition calculation.
         */
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.excludedMealTypes.collect { excluded ->

                    val breakfastExcluded =
                        excluded.any {
                            it.equals(
                                "Breakfast",
                                ignoreCase = true
                            )
                        }

                    val lunchExcluded =
                        excluded.any {
                            it.equals(
                                "Lunch",
                                ignoreCase = true
                            )
                        }

                    val dinnerExcluded =
                        excluded.any {
                            it.equals(
                                "Dinner",
                                ignoreCase = true
                            )
                        }

                    // Change button text depending on current state.
                    buttonToggleBreakfast.text =
                        if (breakfastExcluded) {
                            "Include"
                        } else {
                            "Exclude"
                        }

                    buttonToggleLunch.text =
                        if (lunchExcluded) {
                            "Include"
                        } else {
                            "Exclude"
                        }

                    buttonToggleDinner.text =
                        if (dinnerExcluded) {
                            "Include"
                        } else {
                            "Exclude"
                        }

                    // Fade excluded meals so their state is obvious.
                    textBreakfast.alpha =
                        if (breakfastExcluded) {
                            0.5f
                        } else {
                            1.0f
                        }

                    textLunch.alpha =
                        if (lunchExcluded) {
                            0.5f
                        } else {
                            1.0f
                        }

                    textDinner.alpha =
                        if (dinnerExcluded) {
                            0.5f
                        } else {
                            1.0f
                        }
                }
            }
        }

        /*
         * Include / exclude meals from today's nutrition.
         */
        buttonToggleBreakfast.setOnClickListener {
            viewModel.toggleMealExcluded(
                "Breakfast"
            )
        }

        buttonToggleLunch.setOnClickListener {
            viewModel.toggleMealExcluded(
                "Lunch"
            )
        }

        buttonToggleDinner.setOnClickListener {
            viewModel.toggleMealExcluded(
                "Dinner"
            )
        }

        /*
         * Open the Recipes screen.
         */
        buttonViewBreakfast.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    RecipesFragment()
                )
                .commit()
        }

        buttonViewLunch.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    RecipesFragment()
                )
                .commit()
        }

        buttonViewDinner.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    RecipesFragment()
                )
                .commit()
        }

        /*
         * Open the full meal plan.
         */
        buttonViewMealPlan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    MealPlanFragment()
                )
                .commit()
        }

        /*
         * Open the grocery list.
         */
        buttonViewGroceryList.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    GroceryListFragment()
                )
                .commit()
        }
    }
}