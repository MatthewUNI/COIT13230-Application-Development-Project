package au.edu.cqu.ai_basedsmartmealplanner

import au.edu.cqu.ai_basedsmartmealplanner.model.NutritionInfo
import au.edu.cqu.ai_basedsmartmealplanner.nutrition.AfcdNutritionDataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AfcdNutritionDataSourceTest {

    @Test
    fun getNutritionByAfcdId_unknownIdReturnsNull() {
        val dataSource = AfcdNutritionDataSource()

        val result = dataSource.getNutritionByAfcdId("UNKNOWN")

        assertNull(result)
    }

    @Test
    fun getNutritionByAfcdId_knownIdReturnsNutritionInfo() {
        val nutritionInfo = NutritionInfo(
            calories = 450,
            protein = 5.0,
            carbs = 20.0,
            fats = 3.0
        )

        val dataSource = AfcdNutritionDataSource(
            mapOf("TEST001" to nutritionInfo)
        )

        val result = dataSource.getNutritionByAfcdId("TEST001")

        assertEquals(nutritionInfo, result)
    }
}