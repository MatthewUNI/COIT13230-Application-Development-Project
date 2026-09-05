package au.edu.cqu.ai_basedsmartmealplanner

import au.edu.cqu.ai_basedsmartmealplanner.model.NutritionInfo
import au.edu.cqu.ai_basedsmartmealplanner.nutrition.NutritionAnalysisEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class NutritionAnalysisEngineTest {

    @Test
    fun calculateTotalNutrition_addsValuesCorrectly() {
        val engine = NutritionAnalysisEngine()

        val items = listOf(
            NutritionInfo(
                energyKj = 500.0,
                proteinG = 10.0,
                carbohydratesG = 20.0,
                fatG = 5.0,
                fibreG = 3.0,
                sugarsG = 4.0,
                sodiumMg = 100.0
            ),
            NutritionInfo(
                energyKj = 300.0,
                proteinG = 5.0,
                carbohydratesG = 15.0,
                fatG = 2.0,
                fibreG = 2.0,
                sugarsG = 3.0,
                sodiumMg = 50.0
            )
        )

        val result = engine.calculateTotalNutrition(items)

        assertEquals(800.0, result.energyKj, 0.001)
        assertEquals(15.0, result.proteinG, 0.001)
        assertEquals(35.0, result.carbohydratesG, 0.001)
        assertEquals(7.0, result.fatG, 0.001)
        assertEquals(5.0, result.fibreG, 0.001)
        assertEquals(7.0, result.sugarsG, 0.001)
        assertEquals(150.0, result.sodiumMg, 0.001)
    }

}