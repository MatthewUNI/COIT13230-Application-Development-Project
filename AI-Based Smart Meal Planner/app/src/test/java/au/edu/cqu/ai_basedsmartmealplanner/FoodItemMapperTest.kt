package au.edu.cqu.ai_basedsmartmealplanner

import au.edu.cqu.ai_basedsmartmealplanner.database.toEntity
import au.edu.cqu.ai_basedsmartmealplanner.database.toFoodItem
import au.edu.cqu.ai_basedsmartmealplanner.model.FoodItem
import org.junit.Assert.assertEquals
import org.junit.Test

class FoodItemMapperTest {

    @Test
    fun foodItem_convertsToEntityAndBackCorrectly() {
        val foodItem = FoodItem(
            foodItemId = 1,
            name = "Apple",
            quantity = 2.0,
            unit = "piece",
            afcdFoodId = "TEST001"
        )

        val entity = foodItem.toEntity()
        val result = entity.toFoodItem()

        assertEquals(foodItem, result)
    }
}