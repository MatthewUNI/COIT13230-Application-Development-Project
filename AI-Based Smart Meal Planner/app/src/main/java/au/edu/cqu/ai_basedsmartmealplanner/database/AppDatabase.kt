package au.edu.cqu.ai_basedsmartmealplanner.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FoodItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
}