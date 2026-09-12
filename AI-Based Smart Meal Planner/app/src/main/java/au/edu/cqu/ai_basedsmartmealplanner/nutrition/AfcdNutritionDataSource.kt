package au.edu.cqu.ai_basedsmartmealplanner.nutrition

import au.edu.cqu.ai_basedsmartmealplanner.model.NutritionInfo

class AfcdNutritionDataSource {

    private val nutritionData = mutableMapOf<String, NutritionInfo>()

    fun getNutritionByAfcdId(afcdFoodId: String): NutritionInfo? {
        return nutritionData[afcdFoodId]
    }
}