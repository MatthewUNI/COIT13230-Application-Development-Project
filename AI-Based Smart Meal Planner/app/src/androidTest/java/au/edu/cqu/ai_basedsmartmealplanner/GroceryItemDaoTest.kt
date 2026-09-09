package au.edu.cqu.ai_basedsmartmealplanner

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import au.edu.cqu.ai_basedsmartmealplanner.database.AppDatabase
import au.edu.cqu.ai_basedsmartmealplanner.database.GroceryItemDao
import au.edu.cqu.ai_basedsmartmealplanner.database.GroceryItemEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GroceryItemDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var groceryItemDao: GroceryItemDao

    @Before
    fun setupDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        groceryItemDao = database.groceryItemDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertAndReadGroceryItem() = runBlocking {
        val item = GroceryItemEntity(
            name = "Milk",
            quantity = 2.0,
            unit = "L",
            category = "Dairy",
            isPurchased = false
        )

        groceryItemDao.insert(item)

        val savedItems = groceryItemDao.getAll()

        assertEquals(1, savedItems.size)
        assertEquals("Milk", savedItems[0].name)
        assertEquals(2.0, savedItems[0].quantity, 0.001)
        assertEquals("L", savedItems[0].unit)
        assertEquals("Dairy", savedItems[0].category)
        assertEquals(false, savedItems[0].isPurchased)
    }
}