package au.edu.cqu.ai_basedsmartmealplanner.nutrition

import au.edu.cqu.ai_basedsmartmealplanner.model.NutritionInfo

class AfcdNutritionDataSource(
    private val nutritionData: Map<String, NutritionInfo> = emptyMap()
) {

    fun getNutritionByAfcdId(afcdFoodId: String): NutritionInfo? {
        return nutritionData[afcdFoodId]
    }
}