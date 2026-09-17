package au.edu.cqu.ai_basedsmartmealplanner.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var mealPlanDao: MealPlanDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        mealPlanDao = database.mealPlanDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveMealPlan() {
        val plan = SavedMealPlanEntity(
            title = "Test High-Protein Plan",
            planJson = """{"title":"High-Protein Plan","dailyMeals":[]}"""
        )

        mealPlanDao.insertMealPlan(plan)
        val savedPlans = mealPlanDao.getAllSavedMealPlans()

        assertEquals(1, savedPlans.size)
        assertEquals("Test High-Protein Plan", savedPlans[0].title)
    }

    @Test
    fun clearAllSavedPlans() {
        val plan = SavedMealPlanEntity(
            title = "Temporary Plan",
            planJson = "{}"
        )

        mealPlanDao.insertMealPlan(plan)
        mealPlanDao.clearAll()
        val savedPlans = mealPlanDao.getAllSavedMealPlans()

        assertTrue(savedPlans.isEmpty())
    }
}