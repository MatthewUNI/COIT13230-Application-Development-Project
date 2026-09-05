package au.edu.cqu.ai_basedsmartmealplanner.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class MealPlan(
    @SerializedName("plan_id") val planId: String,
    @SerializedName("start_date") val startDate: Date,
    @SerializedName("daily_meals") val dailyMeals: List<Recipe>
)
