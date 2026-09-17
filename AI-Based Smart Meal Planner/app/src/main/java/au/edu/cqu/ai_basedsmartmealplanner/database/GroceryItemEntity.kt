package au.edu.cqu.ai_basedsmartmealplanner.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_items")
data class GroceryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val groceryItemId: Int = 0,
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,
    val isPurchased: Boolean = false
)