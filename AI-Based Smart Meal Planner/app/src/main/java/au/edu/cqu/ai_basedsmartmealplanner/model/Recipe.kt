package au.edu.cqu.ai_basedsmartmealplanner.model

import com.google.gson.annotations.SerializedName
data class Recipe(
    @SerializedName("recipe_id") val recipeId: String,
    @SerializedName("title") val title: String,
    @SerializedName("ingredients") val ingredients: List<String>,
    @SerializedName("instructions") val instructions: List<String>,
    @SerializedName("total_calories") val totalCalories: Int
)
