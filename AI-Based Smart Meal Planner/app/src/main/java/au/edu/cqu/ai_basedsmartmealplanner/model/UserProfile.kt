package au.edu.cqu.ai_basedsmartmealplanner.model

data class UserProfile(
    val profileId: Int = 0,
    var goalType: String = "",
    var currentWeight: Double = 0.0,
    var targetWeight: Double = 0.0,
    var dietaryRequirements: List<String> = emptyList(),
    var foodPreferences: List<String> = emptyList(),
    var availableIngredients: List<String> = emptyList()
)
