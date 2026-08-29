package au.edu.cqu.ai_basedsmartmealplanner.model

data class NutritionInfo(
    val energyKj: Double,
    val proteinG: Double,
    val carbohydratesG: Double,
    val fatG: Double,
    val fibreG: Double,
    val sugarsG: Double,
    val sodiumMg: Double
)