package au.edu.cqu.ai_basedsmartmealplanner

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import au.edu.cqu.ai_basedsmartmealplanner.database.AppDatabase
import au.edu.cqu.ai_basedsmartmealplanner.database.FoodItemDao
import au.edu.cqu.ai_basedsmartmealplanner.database.FoodItemEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoodItemDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var foodItemDao: FoodItemDao

    @Before
    fun setupDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        foodItemDao = database.foodItemDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertAndReadFoodItem() = runBlocking {
        val item = FoodItemEntity(
            foodItemId = 1,
            name = "Banana",
            quantity = 2.0,
            unit = "piece",
            afcdFoodId = "AFCD001"
        )

        foodItemDao.insert(item)

        val savedItems = foodItemDao.getAll()

        assertEquals(1, savedItems.size)
        assertEquals(1, savedItems[0].foodItemId)
        assertEquals("Banana", savedItems[0].name)
        assertEquals(2.0, savedItems[0].quantity, 0.001)
        assertEquals("piece", savedItems[0].unit)
        assertEquals("AFCD001", savedItems[0].afcdFoodId)
    }
}