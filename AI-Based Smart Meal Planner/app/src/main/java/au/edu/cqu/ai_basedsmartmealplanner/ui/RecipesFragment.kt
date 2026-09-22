package au.edu.cqu.ai_basedsmartmealplanner.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import au.edu.cqu.ai_basedsmartmealplanner.R
import au.edu.cqu.ai_basedsmartmealplanner.ai.MealPlannerViewModel
import au.edu.cqu.ai_basedsmartmealplanner.ai.MealUiState
import au.edu.cqu.ai_basedsmartmealplanner.nutrition.NutritionAnalysisEngine
import kotlinx.coroutines.launch
import java.util.Locale

class RecipesFragment : Fragment(R.layout.fragment_recipes) {

    private lateinit var viewModel: MealPlannerViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =
            ViewModelProvider(requireActivity())[MealPlannerViewModel::class.java]

        val textNoRecipe =
            view.findViewById<TextView>(R.id.textNoRecipe)

        val recipeListContainer =
            view.findViewById<LinearLayout>(R.id.recipeListContainer)

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collect { state ->

                    when (state) {

                        is MealUiState.Success -> {

                            val recipes = state.mealPlan.dailyMeals

                            if (recipes.isEmpty()) {

                                textNoRecipe.visibility = View.VISIBLE
                                textNoRecipe.text =
                                    "No recipes available. Generate a meal plan first."

                                recipeListContainer.visibility = View.GONE

                            } else {

                                textNoRecipe.visibility = View.GONE
                                recipeListContainer.visibility = View.VISIBLE

                                recipeListContainer.removeAllViews()

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

                                    val dayRecipes = recipes.filter { recipe ->
                                        recipe.day.equals(
                                            day,
                                            ignoreCase = true
                                        )
                                    }

                                    if (dayRecipes.isNotEmpty()) {

                                        // Day heading
                                        val dayHeading =
                                            TextView(requireContext()).apply {

                                                text = day
                                                textSize = 20f

                                                setPadding(
                                                    4,
                                                    20,
                                                    4,
                                                    12
                                                )

                                                setTypeface(
                                                    null,
                                                    android.graphics.Typeface.BOLD
                                                )
                                            }

                                        recipeListContainer.addView(dayHeading)

                                        dayRecipes.forEach { recipe ->

                                            // Inflate dropdown layout
                                            val recipeView =
                                                layoutInflater.inflate(
                                                    R.layout.item_recipe_dropdown,
                                                    recipeListContainer,
                                                    false
                                                )

                                            val textRecipeHeader =
                                                recipeView.findViewById<TextView>(
                                                    R.id.textRecipeHeader
                                                )

                                            val recipeDetailsContainer =
                                                recipeView.findViewById<LinearLayout>(
                                                    R.id.recipeDetailsContainer
                                                )

                                            val textRecipeCalories =
                                                recipeView.findViewById<TextView>(
                                                    R.id.textRecipeCalories
                                                )

                                            val textRecipeMacros =
                                                recipeView.findViewById<TextView>(
                                                    R.id.textRecipeMacros
                                                )

                                            val textRecipeIngredients =
                                                recipeView.findViewById<TextView>(
                                                    R.id.textRecipeIngredients
                                                )

                                            val textRecipeInstructions =
                                                recipeView.findViewById<TextView>(
                                                    R.id.textRecipeInstructions
                                                )

                                            // Calculate nutrition for this recipe
                                            val nutrition =
                                                NutritionAnalysisEngine.analyzeRecipe(
                                                    recipe
                                                )

                                            // Set recipe information
                                            textRecipeHeader.text =
                                                "▼ ${recipe.mealType} - ${recipe.title}"

                                            textRecipeCalories.text =
                                                "Calories: ${nutrition.calories}"

                                            textRecipeMacros.text =
                                                String.format(
                                                    Locale.ENGLISH,
                                                    "Protein: %.1fg | Carbs: %.1fg | Fats: %.1fg",
                                                    nutrition.protein,
                                                    nutrition.carbs,
                                                    nutrition.fats
                                                )

                                            textRecipeIngredients.text =
                                                recipe.ingredients.joinToString("\n") {
                                                    "• $it"
                                                }

                                            textRecipeInstructions.text =
                                                recipe.instructions
                                                    .mapIndexed { index, instruction ->
                                                        "${index + 1}. $instruction"
                                                    }
                                                    .joinToString("\n")

                                            // Expand/collapse recipe
                                            textRecipeHeader.setOnClickListener {

                                                if (
                                                    recipeDetailsContainer.visibility ==
                                                    View.GONE
                                                ) {

                                                    recipeDetailsContainer.visibility =
                                                        View.VISIBLE

                                                    textRecipeHeader.text =
                                                        "▲ ${recipe.mealType} - ${recipe.title}"

                                                } else {

                                                    recipeDetailsContainer.visibility =
                                                        View.GONE

                                                    textRecipeHeader.text =
                                                        "▼ ${recipe.mealType} - ${recipe.title}"
                                                }
                                            }

                                            recipeListContainer.addView(recipeView)
                                        }
                                    }
                                }
                            }
                        }

                        else -> {

                            textNoRecipe.visibility = View.VISIBLE
                            textNoRecipe.text =
                                "Generate a meal plan to view recipes."

                            recipeListContainer.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
}