package au.edu.cqu.ai_basedsmartmealplanner.model

data class FoodItem(
    val foodItemId: Int,
    val name: String,
    val quantity: Double,
    val unit: String,
    val afcdFoodId: String?
)