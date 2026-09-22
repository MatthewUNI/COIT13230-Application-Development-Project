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

                        textCalories.text = nutrition.calories.toString()
                        textProtein.text =
                            String.format("%.1f g", nutrition.protein)
                        textCarbs.text =
                            String.format("%.1f g", nutrition.carbs)
                        textFats.text =
                            String.format("%.1f g", nutrition.fats)

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

                        val meals = state.mealPlan.dailyMeals

                        val today = SimpleDateFormat(
                            "EEEE",
                            Locale.ENGLISH
                        ).format(Date())

                        val breakfast = meals.find { meal ->
                            meal.day.equals(today, ignoreCase = true) &&
                                    meal.mealType.equals(
                                        "Breakfast",
                                        ignoreCase = true
                                    )
                        }

                        val lunch = meals.find { meal ->
                            meal.day.equals(today, ignoreCase = true) &&
                                    meal.mealType.equals(
                                        "Lunch",
                                        ignoreCase = true
                                    )
                        }

                        val dinner = meals.find { meal ->
                            meal.day.equals(today, ignoreCase = true) &&
                                    meal.mealType.equals(
                                        "Dinner",
                                        ignoreCase = true
                                    )
                        }

                        mealsContainer.visibility = View.VISIBLE
                        textNoMeals.visibility = View.GONE

                        textBreakfast.text =
                            breakfast?.title ?: "Meal not available"

                        textLunch.text =
                            lunch?.title ?: "Meal not available"

                        textDinner.text =
                            dinner?.title ?: "Meal not available"

                    } else {

                        mealsContainer.visibility = View.GONE
                        textNoMeals.visibility = View.VISIBLE
                    }
                }
            }
        }

        /*
         * Open the Recipes screen.
         * RecipesFragment now reads recipes directly from the
         * shared MealPlannerViewModel.
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

        buttonViewMealPlan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    MealPlanFragment()
                )
                .commit()
        }

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