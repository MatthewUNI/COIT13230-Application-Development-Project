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

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var viewModel: MealPlannerViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use the same shared ViewModel as the Meal Plan and Grocery List screens.
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
         * When a meal plan is generated or restored, the nutrition
         * values are automatically displayed on the Home screen.
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
         * Observe the current meal plan and display the first
         * three meals as Breakfast, Lunch and Dinner.
         */
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collect { state ->

                    if (state is MealUiState.Success) {

                        val meals = state.mealPlan.dailyMeals

                        mealsContainer.visibility = View.VISIBLE
                        textNoMeals.visibility = View.GONE

                        textBreakfast.text =
                            meals.getOrNull(0)?.title ?: "Meal not available"

                        textLunch.text =
                            meals.getOrNull(1)?.title ?: "Meal not available"

                        textDinner.text =
                            meals.getOrNull(2)?.title ?: "Meal not available"

                    } else {

                        mealsContainer.visibility = View.GONE
                        textNoMeals.visibility = View.VISIBLE
                    }
                }
            }
        }

        buttonViewBreakfast.setOnClickListener {

            val state = viewModel.uiState.value

            if (state is MealUiState.Success) {
                state.mealPlan.dailyMeals.getOrNull(0)?.let { meal ->

                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragmentContainer,
                            RecipesFragment.newInstance(meal.title)
                        )
                        .commit()
                }
            }
        }

        buttonViewLunch.setOnClickListener {

            val state = viewModel.uiState.value

            if (state is MealUiState.Success) {
                state.mealPlan.dailyMeals.getOrNull(1)?.let { meal ->

                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragmentContainer,
                            RecipesFragment.newInstance(meal.title)
                        )
                        .commit()
                }
            }
        }

        buttonViewDinner.setOnClickListener {

            val state = viewModel.uiState.value

            if (state is MealUiState.Success) {
                state.mealPlan.dailyMeals.getOrNull(2)?.let { meal ->

                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragmentContainer,
                            RecipesFragment.newInstance(meal.title)
                        )
                        .commit()
                }
            }
        }
        buttonViewMealPlan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MealPlanFragment())
                .commit()
        }

        buttonViewGroceryList.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, GroceryListFragment())
                .commit()
        }
    }
}