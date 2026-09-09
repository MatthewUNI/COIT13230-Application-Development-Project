package au.edu.cqu.ai_basedsmartmealplanner.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import au.edu.cqu.ai_basedsmartmealplanner.R
import au.edu.cqu.ai_basedsmartmealplanner.ai.MealPlannerViewModel
import au.edu.cqu.ai_basedsmartmealplanner.ai.MealUiState
import au.edu.cqu.ai_basedsmartmealplanner.profile.UserProfileManager
import kotlinx.coroutines.launch

class MealPlanFragment : Fragment(R.layout.fragment_meal_plan) {

    private val viewModel: MealPlannerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonGenerateMealPlan =
            view.findViewById<Button>(R.id.buttonGenerateMealPlan)

        val textNoMealPlan =
            view.findViewById<TextView>(R.id.textNoMealPlan)

        val mealPlanContainer =
            view.findViewById<LinearLayout>(R.id.mealPlanContainer)

        val textMealPlanTitle =
            view.findViewById<TextView>(R.id.textMealPlanTitle)

        val textMealPlanIngredients =
            view.findViewById<TextView>(R.id.textMealPlanIngredients)

        val textMealPlanInstructions =
            view.findViewById<TextView>(R.id.textMealPlanInstructions)


        buttonGenerateMealPlan.setOnClickListener {

            val profile = UserProfileManager.getProfile()

            viewModel.generateMealPlan(
                profile.availableIngredients
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collect { state ->

                    when (state) {

                        is MealUiState.Idle -> {
                            // Initial screen
                        }

                        is MealUiState.Loading -> {
                            Toast.makeText(
                                requireContext(),
                                "Generating meal plan...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is MealUiState.Success -> {
                            val mealPlan = state.mealPlan

                            textNoMealPlan.visibility = View.GONE
                            mealPlanContainer.visibility = View.VISIBLE

                            textMealPlanTitle.text =
                                mealPlan.title

                            textMealPlanIngredients.text =
                                "Ingredients\n" +
                                        mealPlan.ingredients.joinToString("\n") {
                                            "• $it"
                                        }

                            textMealPlanInstructions.text =
                                "Instructions\n" +
                                        mealPlan.instructions
                                            .mapIndexed { index, instruction ->
                                                "${index + 1}. $instruction"
                                            }
                                            .joinToString("\n")

                            Toast.makeText(
                                requireContext(),
                                "Meal plan generated",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is MealUiState.Error -> {
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