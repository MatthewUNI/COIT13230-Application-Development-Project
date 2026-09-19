package au.edu.cqu.ai_basedsmartmealplanner.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import au.edu.cqu.ai_basedsmartmealplanner.R
import au.edu.cqu.ai_basedsmartmealplanner.ai.RecipeGenerator
import au.edu.cqu.ai_basedsmartmealplanner.profile.UserProfileManager
import kotlinx.coroutines.launch

class RecipesFragment : Fragment(R.layout.fragment_recipes) {

    private var selectedRecipeName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectedRecipeName = arguments?.getString(ARG_RECIPE_NAME)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        UserProfileManager.initialize(requireContext())

        val textNoRecipe =
            view.findViewById<TextView>(R.id.textNoRecipe)

        val recipeContainer =
            view.findViewById<View>(R.id.recipeContainer)

        val textRecipeTitle =
            view.findViewById<TextView>(R.id.textRecipeTitle)

        val textRecipeCalories =
            view.findViewById<TextView>(R.id.textRecipeCalories)

        val textRecipeIngredients =
            view.findViewById<TextView>(R.id.textRecipeIngredients)

        val textRecipeInstructions =
            view.findViewById<TextView>(R.id.textRecipeInstructions)

        val buttonGenerateRecipe =
            view.findViewById<Button>(R.id.buttonGenerateRecipe)

        selectedRecipeName?.let { recipeName ->
            textNoRecipe.text = "Selected meal: $recipeName"
        }

        buttonGenerateRecipe.setOnClickListener {

            val recipeName = selectedRecipeName

            if (recipeName.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Please select a meal from the Home screen first.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val profile = UserProfileManager.getProfile()

            buttonGenerateRecipe.isEnabled = false
            buttonGenerateRecipe.text = "Generating..."

            viewLifecycleOwner.lifecycleScope.launch {

                try {
                    val recipe = RecipeGenerator.requestRecipeDetails(
                        recipeName = recipeName,
                        dietaryRequirements = profile.dietaryRequirements,
                        availableIngredients = profile.availableIngredients
                    )

                    if (recipe != null) {

                        textNoRecipe.visibility = View.GONE
                        recipeContainer.visibility = View.VISIBLE

                        textRecipeTitle.text = recipe.title

                        textRecipeCalories.text =
                            "Calories: ${recipe.totalCalories}"

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

                    } else {

                        Toast.makeText(
                            requireContext(),
                            "Unable to generate recipe.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (e: Exception) {

                    Toast.makeText(
                        requireContext(),
                        "Recipe generation failed: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()

                } finally {

                    buttonGenerateRecipe.isEnabled = true
                    buttonGenerateRecipe.text = "Generate Recipe"
                }
            }
        }
    }

    companion object {

        private const val ARG_RECIPE_NAME = "recipe_name"

        fun newInstance(recipeName: String): RecipesFragment {

            return RecipesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_RECIPE_NAME, recipeName)
                }
            }
        }
    }
}