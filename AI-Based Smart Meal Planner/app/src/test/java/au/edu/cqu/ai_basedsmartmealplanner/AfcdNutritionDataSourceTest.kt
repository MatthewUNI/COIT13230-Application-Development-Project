package au.edu.cqu.ai_basedsmartmealplanner

import au.edu.cqu.ai_basedsmartmealplanner.nutrition.AfcdNutritionDataSource
import org.junit.Assert.assertNull
import org.junit.Test
import au.edu.cqu.ai_basedsmartmealplanner.model.NutritionInfo
import org.junit.Assert.assertEquals

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
            energyKj = 450.0,
            proteinG = 5.0,
            carbohydratesG = 20.0,
            fatG = 3.0,
            fibreG = 4.0,
            sugarsG = 6.0,
            sodiumMg = 80.0
        )

        val dataSource = AfcdNutritionDataSource(
            mapOf("TEST001" to nutritionInfo)
        )

        val result = dataSource.getNutritionByAfcdId("TEST001")

        assertEquals(nutritionInfo, result)
    }
}