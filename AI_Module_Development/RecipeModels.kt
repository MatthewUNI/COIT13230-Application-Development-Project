 package com.smartmealplanner.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class MealPlan(
    @SerializedName("plan_id") val planId: String,
    @SerializedName("start_date") val startDate: Date,
    @SerializedName("daily_meals") val dailyMeals: List<Recipe>
)

data class Recipe(
    @SerializedName("recipe_id") val recipeId: String,
    @SerializedName("title") val title: String,
    @SerializedName("ingredients") val ingredients: List<String>,
    @SerializedName("instructions") val instructions: List<String>,
    @SerializedName("total_calories") val totalCalories: Int
)
