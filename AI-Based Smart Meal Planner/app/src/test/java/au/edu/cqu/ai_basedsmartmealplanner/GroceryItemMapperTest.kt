package au.edu.cqu.ai_basedsmartmealplanner

import au.edu.cqu.ai_basedsmartmealplanner.database.toEntity
import au.edu.cqu.ai_basedsmartmealplanner.database.toGroceryItem
import au.edu.cqu.ai_basedsmartmealplanner.model.GroceryItem
import org.junit.Assert.assertEquals
import org.junit.Test

class GroceryItemMapperTest {

    @Test
    fun groceryItem_convertsToEntityAndBackCorrectly() {
        val groceryItem = GroceryItem(
            name = "Milk",
            quantity = 2.0,
            unit = "L",
            category = "Dairy",
            isPurchased = true
        )

        val entity = groceryItem.toEntity()
        val result = entity.toGroceryItem()

        assertEquals(groceryItem, result)
    }
}