package au.edu.cqu.ai_basedsmartmealplanner

import au.edu.cqu.ai_basedsmartmealplanner.nutrition.AfcdNutritionDataSource
import org.junit.Assert.assertNull
import org.junit.Test

class AfcdNutritionDataSourceTest {

    @Test
    fun getNutritionByAfcdId_unknownIdReturnsNull() {
        val dataSource = AfcdNutritionDataSource()

        val result = dataSource.getNutritionByAfcdId("UNKNOWN")

        assertNull(result)
    }
}