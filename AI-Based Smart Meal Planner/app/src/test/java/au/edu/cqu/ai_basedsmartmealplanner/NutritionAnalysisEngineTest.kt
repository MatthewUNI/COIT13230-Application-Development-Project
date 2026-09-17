package au.edu.cqu.ai_basedsmartmealplanner

import au.edu.cqu.ai_basedsmartmealplanner.model.FoodItem
import au.edu.cqu.ai_basedsmartmealplanner.model.NutritionInfo
import au.edu.cqu.ai_basedsmartmealplanner.nutrition.AfcdNutritionDataSource
import au.edu.cqu.ai_basedsmartmealplanner.nutrition.NutritionAnalysisEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class NutritionAnalysisEngineTest {

    @Test
    fun calculateTotalNutrition_addsValuesCorrectly() {
        val engine = NutritionAnalysisEngine

        val items = listOf(
            NutritionInfo(
                calories = 500,
                protein = 10.0,
                carbs = 20.0,
                fats = 5.0
            ),
            NutritionInfo(
                calories = 300,
                protein = 5.0,
                carbs = 15.0,
                fats = 2.0
            )
        )

        val result = engine.calculateTotalNutrition(items)

        assertEquals(800, result.calories)
        assertEquals(15.0, result.protein, 0.001)
        assertEquals(35.0, result.carbs, 0.001)
        assertEquals(7.0, result.fats, 0.001)
    }

    @Test
    fun calculateTotalNutrition_emptyListReturnsZeros() {
        val engine = NutritionAnalysisEngine

        val result = engine.calculateTotalNutrition(emptyList())

        assertEquals(0, result.calories)
        assertEquals(0.0, result.protein, 0.001)
        assertEquals(0.0, result.carbs, 0.001)
        assertEquals(0.0, result.fats, 0.001)
    }

    @Test
    fun calculateTotalNutritionFromAfcdIds_totalsKnownIds() {
        val engine = NutritionAnalysisEngine

        val firstNutrition = NutritionInfo(
            calories = 400,
            protein = 8.0,
            carbs = 18.0,
            fats = 4.0
        )

        val secondNutrition = NutritionInfo(
            calories = 250,
            protein = 6.0,
            carbs = 12.0,
            fats = 3.0
        )

        val dataSource = AfcdNutritionDataSource(
            mapOf(
                "TEST001" to firstNutrition,
                "TEST002" to secondNutrition
            )
        )

        val result = engine.calculateTotalNutritionFromAfcdIds(
            listOf("TEST001", "UNKNOWN", "TEST002"),
            dataSource
        )

        assertEquals(650, result.calories)
        assertEquals(14.0, result.protein, 0.001)
        assertEquals(30.0, result.carbs, 0.001)
        assertEquals(7.0, result.fats, 0.001)
    }

    @Test
    fun calculateTotalNutritionFromFoodItems_usesAfcdIdsFromFoodItems() {
        val engine = NutritionAnalysisEngine

        val nutritionInfo = NutritionInfo(
            calories = 300,
            protein = 7.0,
            carbs = 15.0,
            fats = 2.0
        )

        val dataSource = AfcdNutritionDataSource(
            mapOf("TEST001" to nutritionInfo)
        )

        val foodItems = listOf(
            FoodItem(
                foodItemId = 1,
                name = "Test Food",
                quantity = 1.0,
                unit = "serve",
                afcdFoodId = "TEST001"
            ),
            FoodItem(
                foodItemId = 2,
                name = "No AFCD Food",
                quantity = 1.0,
                unit = "serve",
                afcdFoodId = null
            )
        )

        val result = engine.calculateTotalNutritionFromFoodItems(
            foodItems,
            dataSource
        )

        assertEquals(300, result.calories)
        assertEquals(7.0, result.protein, 0.001)
        assertEquals(15.0, result.carbs, 0.001)
        assertEquals(2.0, result.fats, 0.001)
    }
}