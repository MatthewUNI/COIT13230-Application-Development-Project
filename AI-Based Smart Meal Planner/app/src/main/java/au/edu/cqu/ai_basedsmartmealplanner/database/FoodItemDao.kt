package au.edu.cqu.ai_basedsmartmealplanner.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FoodItemDao {

    @Insert
    suspend fun insert(foodItem: FoodItemEntity)

    @Query("SELECT * FROM food_items")
    suspend fun getAll(): List<FoodItemEntity>

    @Delete
    suspend fun delete(foodItem: FoodItemEntity)
}