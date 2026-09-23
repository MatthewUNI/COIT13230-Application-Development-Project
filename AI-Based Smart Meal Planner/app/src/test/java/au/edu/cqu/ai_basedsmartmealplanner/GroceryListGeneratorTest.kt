package au.edu.cqu.ai_basedsmartmealplanner

import au.edu.cqu.ai_basedsmartmealplanner.grocery.GroceryListGenerator
import au.edu.cqu.ai_basedsmartmealplanner.model.Recipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GroceryListGeneratorTest {

    @Test
    fun generateFromRecipe_removesDuplicateIngredients() {
        val recipe = Recipe(
            recipeId = "1",
            title = "Breakfast",
            day = "Monday",
            mealType = "Breakfast",
            ingredients = listOf(
                "Milk",
                "Milk",
                "Banana"
            ),
            instructions = emptyList(),
            totalCalories = 0
        )

        val result = GroceryListGenerator.generateFromRecipe(recipe)

        assertEquals(2, result.items.size)
        assertEquals("Milk", result.items[0].name)
        assertEquals("Banana", result.items[1].name)
        assertFalse(result.items[0].isPurchased)
        assertFalse(result.items[1].isPurchased)
    }

    @Test
    fun generateFromRecipe_excludesAvailableIngredients() {
        val recipe = Recipe(
            recipeId = "2",
            title = "Breakfast",
            day = "Monday",
            mealType = "Breakfast",
            ingredients = listOf(
                "Milk",
                "Banana",
                "Eggs"
            ),
            instructions = emptyList(),
            totalCalories = 0
        )

        val result = GroceryListGenerator.generateFromRecipe(
            recipe = recipe,
            availableIngredients = listOf("Milk")
        )

        assertEquals(2, result.items.size)
        assertEquals("Banana", result.items[0].name)
        assertEquals("Eggs", result.items[1].name)
    }

    @Test
    fun generateFromRecipe_emptyIngredientsReturnsEmptyList() {
        val recipe = Recipe(
            recipeId = "3",
            title = "Empty Recipe",
            day = "Monday",
            mealType = "Breakfast",
            ingredients = emptyList(),
            instructions = emptyList(),
            totalCalories = 0
        )

        val result = GroceryListGenerator.generateFromRecipe(recipe)

        assertEquals(0, result.items.size)
    }

    @Test
    fun categorizeIngredient_assignsCorrectCategories() {
        assertEquals(
            "Produce",
            GroceryListGenerator.categorizeIngredient("Banana")
        )

        assertEquals(
            "Protein",
            GroceryListGenerator.categorizeIngredient("Chicken breast")
        )

        assertEquals(
            "Pantry",
            GroceryListGenerator.categorizeIngredient("Rice")
        )
    }
}