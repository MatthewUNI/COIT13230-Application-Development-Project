package au.edu.cqu.ai_basedsmartmealplanner.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItemEntity(
    @PrimaryKey
    val foodItemId: Int,
    val name: String,
    val quantity: Double,
    val unit: String,
    val afcdFoodId: String?
)