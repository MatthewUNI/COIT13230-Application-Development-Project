package au.edu.cqu.ai_basedsmartmealplanner

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import au.edu.cqu.ai_basedsmartmealplanner.nutrition.AfcdNutritionDataSource

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("au.edu.cqu.ai_basedsmartmealplanner", appContext.packageName)
    }

    @Test
    fun cookingOil_matchesAfcdOilWithFat() {
        val appContext =
            InstrumentationRegistry.getInstrumentation().targetContext

        val dataSource = AfcdNutritionDataSource()
        dataSource.loadFromAssets(appContext)

        val result = dataSource.findFoodByIngredient("20g cooking oil")

        assertNotNull("Cooking oil should match an AFCD food", result)

        println("Matched food: ${result!!.foodName}")
        println("Fat per 100g: ${result.fats}")

        assertTrue(
            "Cooking oil should have a meaningful fat value",
            result.fats > 50.0
        )
    }
}