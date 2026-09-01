package au.edu.cqu.ai_basedsmartmealplanner.model

data class UserProfile(
    val profileId: Int,
    var goalType: String,
    var currentWeight: Double,
    var targetWeight: Double,
    var dietaryRequirements: List<String>,
    var foodPreferences: List<String>,
    var availableIngredients: List<String>
)
