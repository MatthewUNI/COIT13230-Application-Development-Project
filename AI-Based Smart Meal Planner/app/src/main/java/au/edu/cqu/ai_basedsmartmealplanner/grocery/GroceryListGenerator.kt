package au.edu.cqu.ai_basedsmartmealplanner.grocery

import au.edu.cqu.ai_basedsmartmealplanner.model.GroceryItem
import au.edu.cqu.ai_basedsmartmealplanner.model.GroceryList
import au.edu.cqu.ai_basedsmartmealplanner.model.MealPlan
import au.edu.cqu.ai_basedsmartmealplanner.model.Recipe
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    fun generateFromMealPlan(
        mealPlan: MealPlan,
        availableIngredients: List<String> = emptyList(),
        isTodayOnly: Boolean = false
    ): GroceryList {
        val daysToInclude = if (isTodayOnly) {
            val today = SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date())
            mealPlan.dailyMeals.filter { it.day.equals(today, ignoreCase = true) }
        } else {
            mealPlan.dailyMeals
        }

        val rawIngredients = daysToInclude.flatMap { it.ingredients }
        return buildCategorizedList(rawIngredients, availableIngredients)
    }

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

        val missingItemsParsed = rawIngredients
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { raw ->
                // Separates quantities (like "200g") from the base name (like "chicken breast")
                val match = Regex("""^([\d\./\s]*(?:g|kg|ml|cup|cups|tbsp|tsp|slices?|whole|cans?|pieces?|eggs?)?)\s*(.*)""", RegexOption.IGNORE_CASE).find(raw)
                if (match != null && match.groupValues[2].isNotBlank()) {
                    Pair(match.groupValues[1].trim(), match.groupValues[2].trim())
                } else {
                    Pair("", raw)
                }
            }
            .filterNot { (_, name) ->
                inventoryNormalized.any { available -> name.lowercase().contains(available) }
            }

        // Group duplicates by the base name and combine their amounts
        val groupedItems = missingItemsParsed.groupBy { it.second.lowercase() }

        val items = groupedItems.map { (_, pairList) ->
            val actualName = pairList.first().second
            val amounts = pairList.map { it.first }.filter { it.isNotBlank() }
            val combinedAmount = if (amounts.isEmpty()) "" else amounts.joinToString(" + ")

            val finalName = if (combinedAmount.isNotBlank()) "$actualName ($combinedAmount)" else actualName

            GroceryItem(
                name = finalName,
                quantity = 1.0,
                unit = "",
                category = categorizeIngredient(actualName),
                isPurchased = false
            )
        }

        return GroceryList(items = items)
    }

    fun categorizeIngredient(ingredient: String): String {
        val lower = ingredient.lowercase()
        return when {
            produceKeywords.any { lower.contains(it) } -> "Produce"
            proteinKeywords.any { lower.contains(it) } -> "Protein"
            else -> "Pantry"
        }
    }
}