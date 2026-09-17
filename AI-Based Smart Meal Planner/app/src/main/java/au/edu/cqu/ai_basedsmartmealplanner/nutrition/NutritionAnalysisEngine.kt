package au.edu.cqu.ai_basedsmartmealplanner.nutrition

import au.edu.cqu.ai_basedsmartmealplanner.model.FoodItem
import au.edu.cqu.ai_basedsmartmealplanner.model.MealPlan
import au.edu.cqu.ai_basedsmartmealplanner.model.NutritionInfo
import au.edu.cqu.ai_basedsmartmealplanner.model.Recipe

object NutritionAnalysisEngine {

    private val ingredientNutritionTable = mapOf(
        "chicken" to NutritionInfo(calories = 165, protein = 31.0, carbs = 0.0, fats = 3.6),
        "beef" to NutritionInfo(calories = 250, protein = 26.0, carbs = 0.0, fats = 17.0),
        "tofu" to NutritionInfo(calories = 76, protein = 8.0, carbs = 1.9, fats = 4.8),
        "rice" to NutritionInfo(calories = 130, protein = 2.7, carbs = 28.0, fats = 0.3),
        "broccoli" to NutritionInfo(calories = 55, protein = 3.7, carbs = 11.2, fats = 0.6),
        "egg" to NutritionInfo(calories = 78, protein = 6.3, carbs = 0.6, fats = 5.3),
        "spinach" to NutritionInfo(calories = 23, protein = 2.9, carbs = 3.6, fats = 0.4),
        "yogurt" to NutritionInfo(calories = 59, protein = 10.0, carbs = 3.6, fats = 0.4),
        "salmon" to NutritionInfo(calories = 208, protein = 20.0, carbs = 0.0, fats = 13.0)
    )

    fun calculateTotalNutrition(items: List<NutritionInfo>): NutritionInfo {
        return NutritionInfo(
            calories = items.sumOf { it.calories },
            protein = items.sumOf { it.protein },
            carbs = items.sumOf { it.carbs },
            fats = items.sumOf { it.fats }
        )
    }

    fun analyzeRecipe(recipe: Recipe): NutritionInfo {
        var totalCalories = recipe.totalCalories ?: 0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFats = 0.0

        for (ingredient in recipe.ingredients) {
            val lower = ingredient.lowercase()

            val matchedNutrients =
                ingredientNutritionTable.entries
                    .firstOrNull { lower.contains(it.key) }
                    ?.value

            if (matchedNutrients != null) {
                totalProtein += matchedNutrients.protein
                totalCarbs += matchedNutrients.carbs
                totalFats += matchedNutrients.fats

                if (totalCalories == 0) {
                    totalCalories += matchedNutrients.calories
                }
            }
        }

        return NutritionInfo(
            calories = totalCalories,
            protein = totalProtein,
            carbs = totalCarbs,
            fats = totalFats
        )
    }

    fun analyzeDailyPlan(mealPlan: MealPlan): NutritionInfo {
        val nutritionItems = mealPlan.dailyMeals.map { meal ->
            analyzeRecipe(meal)
        }

        return calculateTotalNutrition(nutritionItems)
    }

    fun calculateTotalNutritionFromAfcdIds(
        afcdFoodIds: List<String>,
        dataSource: AfcdNutritionDataSource
    ): NutritionInfo {

        val nutritionItems = afcdFoodIds.mapNotNull { afcdFoodId ->
            dataSource.getNutritionByAfcdId(afcdFoodId)
        }

        return calculateTotalNutrition(nutritionItems)
    }

    fun calculateTotalNutritionFromFoodItems(
        foodItems: List<FoodItem>,
        dataSource: AfcdNutritionDataSource
    ): NutritionInfo {

        val afcdFoodIds = foodItems.mapNotNull { foodItem ->
            foodItem.afcdFoodId
        }

        return calculateTotalNutritionFromAfcdIds(
            afcdFoodIds,
            dataSource
        )
    }
}