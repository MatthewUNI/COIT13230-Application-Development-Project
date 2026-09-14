package au.edu.cqu.ai_basedsmartmealplanner.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Entity representing saved meal plans in local storage.
 */
@Entity(tableName = "saved_meal_plans")
data class SavedMealPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "Saved Meal Plan",
    val planJson: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data Access Object (DAO) for reading and writing meal plans.
 */
@Dao
interface MealPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMealPlan(plan: SavedMealPlanEntity): Long

    @Query("SELECT * FROM saved_meal_plans ORDER BY timestamp DESC")
    fun getAllSavedMealPlans(): List<SavedMealPlanEntity>

    @Query("DELETE FROM saved_meal_plans")
    fun clearAll(): Int
}

/**
 * Room Database definition for application data storage[cite: 1].
 */
@Database(entities = [SavedMealPlanEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mealPlanDao(): MealPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_meal_planner_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}