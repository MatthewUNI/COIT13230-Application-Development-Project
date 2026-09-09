package au.edu.cqu.ai_basedsmartmealplanner.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface GroceryItemDao {

    @Insert
    suspend fun insert(groceryItem: GroceryItemEntity)

    @Query("SELECT * FROM grocery_items")
    suspend fun getAll(): List<GroceryItemEntity>

    @Update
    suspend fun update(groceryItem: GroceryItemEntity)

    @Delete
    suspend fun delete(groceryItem: GroceryItemEntity)
}