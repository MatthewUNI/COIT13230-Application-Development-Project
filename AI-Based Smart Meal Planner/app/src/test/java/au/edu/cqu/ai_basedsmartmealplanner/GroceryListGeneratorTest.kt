package au.edu.cqu.ai_basedsmartmealplanner

import au.edu.cqu.ai_basedsmartmealplanner.grocery.GroceryListGenerator
import au.edu.cqu.ai_basedsmartmealplanner.model.GroceryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GroceryListGeneratorTest {

    @Test
    fun generateGroceryList_combinesDuplicateItems() {
        val generator = GroceryListGenerator()

        val items = listOf(
            GroceryItem(
                name = "Milk",
                quantity = 1.0,
                unit = "L",
                category = "Dairy"
            ),
            GroceryItem(
                name = "Milk",
                quantity = 2.0,
                unit = "L",
                category = "Dairy"
            )
        )

        val result = generator.generateGroceryList(items)

        assertEquals(1, result.items.size)
        assertEquals("Milk", result.items[0].name)
        assertEquals(3.0, result.items[0].quantity, 0.001)
        assertEquals("L", result.items[0].unit)
        assertEquals("Dairy", result.items[0].category)
        assertFalse(result.items[0].isPurchased)
    }

    @Test
    fun generateGroceryList_keepsDifferentUnitsSeparate() {
        val generator = GroceryListGenerator()

        val items = listOf(
            GroceryItem(
                name = "Milk",
                quantity = 1.0,
                unit = "L",
                category = "Dairy"
            ),
            GroceryItem(
                name = "Milk",
                quantity = 500.0,
                unit = "mL",
                category = "Dairy"
            )
        )

        val result = generator.generateGroceryList(items)

        assertEquals(2, result.items.size)
    }

    @Test
    fun generateGroceryList_emptyInputReturnsEmptyList() {
        val generator = GroceryListGenerator()

        val result = generator.generateGroceryList(emptyList())

        assertEquals(0, result.items.size)
    }
}