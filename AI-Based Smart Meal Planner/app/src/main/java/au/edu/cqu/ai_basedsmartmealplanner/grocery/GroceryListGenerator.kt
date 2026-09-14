package au.edu.cqu.ai_basedsmartmealplanner.grocery

import au.edu.cqu.ai_basedsmartmealplanner.model.GroceryItem
import au.edu.cqu.ai_basedsmartmealplanner.model.GroceryList
import au.edu.cqu.ai_basedsmartmealplanner.model.MealPlan
import au.edu.cqu.ai_basedsmartmealplanner.model.Recipe

object GroceryListGenerator {

    private val produceKeywords = setOf(
        "apple", "banana", "berry", "berries", "spinach", "kale", "lettuce", "tomato",
        "onion", "garlic", "potato", "carrot", "broccoli", "avocado", "lemon", "lime",
        "pepper", "bell pepper", "cucumber", "zucchini", "mushroom", "cilantro", "parsley"
    )

    private val proteinKeywords = setOf(
        "chicken", "beef", "steak", "pork", "turkey", "fish", "salmon", "tuna",
        "tofu", "egg", "eggs", "shrimp", "tempeh", "beans", "lentils", "chickpeas",
        "greek yoghurt", "yogurt", "protein powder"
    )

    /**
     * Extracts required ingredients across all scheduled meals and maps them
     * into a categorized grocery list[cite: 1].
     */
    fun generateFromMealPlan(
        mealPlan: MealPlan,
        availableIngredients: List<String> = emptyList()
    ): GroceryList {
        val rawIngredients = mealPlan.dailyMeals.flatMap { it.ingredients }
        return buildCategorizedList(rawIngredients, availableIngredients)
    }

    /**
     * Creates a categorized list for an individual recipe[cite: 1].
     */
    fun generateFromRecipe(
        recipe: Recipe,
        availableIngredients: List<String> = emptyList()
    ): GroceryList {
        return buildCategorizedList(recipe.ingredients, availableIngredients)
    }

    private fun buildCategorizedList(
        rawIngredients: List<String>,
        availableIngredients: List<String>
    ): GroceryList {
        val inventoryNormalized = availableIngredients.map { it.trim().lowercase() }.toSet()

        val items = rawIngredients
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .filterNot { ingredient ->
                // Skip items the user already has in their available ingredients[cite: 1]
                inventoryNormalized.any { available -> ingredient.lowercase().contains(available) }
            }
            .map { ingredient ->
                GroceryItem(
                    name = ingredient,
                    category = categorizeIngredient(ingredient),
                    isPurchased = false
                )
            }

        return GroceryList(items = items)
    }

    /**
     * Classifies ingredients into Produce, Protein, or Pantry[cite: 1].
     */
    fun categorizeIngredient(ingredient: String): String {
        val lower = ingredient.lowercase()
        return when {
            produceKeywords.any { lower.contains(it) } -> "Produce"
            proteinKeywords.any { lower.contains(it) } -> "Protein"
            else -> "Pantry"
        }
    }
}