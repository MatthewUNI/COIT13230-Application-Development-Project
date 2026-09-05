package au.edu.cqu.ai_basedsmartmealplanner.model

data class GroceryItem(
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,
    val isPurchased: Boolean = false
)