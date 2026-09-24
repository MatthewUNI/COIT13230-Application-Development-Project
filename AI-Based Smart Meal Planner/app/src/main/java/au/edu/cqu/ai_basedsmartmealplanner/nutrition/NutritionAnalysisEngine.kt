package au.edu.cqu.ai_basedsmartmealplanner.nutrition

import android.content.Context
import android.util.Log
import au.edu.cqu.ai_basedsmartmealplanner.model.FoodItem
import au.edu.cqu.ai_basedsmartmealplanner.model.MealPlan
import au.edu.cqu.ai_basedsmartmealplanner.model.NutritionInfo
import au.edu.cqu.ai_basedsmartmealplanner.model.Recipe
import kotlin.math.roundToInt

object NutritionAnalysisEngine {

    private lateinit var afcdDataSource: AfcdNutritionDataSource

    fun initialize(context: Context) {
        afcdDataSource = AfcdNutritionDataSource()
        afcdDataSource.loadFromAssets(context.applicationContext)
    }

    fun calculateTotalNutrition(
        items: List<NutritionInfo>
    ): NutritionInfo {

        return NutritionInfo(
            calories = items.sumOf { it.calories },
            protein = items.sumOf { it.protein },
            carbs = items.sumOf { it.carbs },
            fats = items.sumOf { it.fats }
        )
    }

    fun analyzeRecipe(
        recipe: Recipe
    ): NutritionInfo {

        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFats = 0.0

        for (ingredient in recipe.ingredients) {

            val matchedFood =
                if (::afcdDataSource.isInitialized) {
                    afcdDataSource.findFoodByIngredient(
                        ingredient
                    )
                } else {
                    null
                }

            if (matchedFood != null) {

                val estimatedGrams =
                    afcdDataSource.estimateIngredientGrams(
                        ingredient
                    )

                val portionFactor =
                    estimatedGrams / 100.0

                val ingredientProtein =
                    matchedFood.protein * portionFactor

                val ingredientCarbs =
                    matchedFood.carbs * portionFactor

                val ingredientFats =
                    matchedFood.fats * portionFactor

                Log.d(
                    "AFCD_DEBUG",
                    """
                    ------------------------------
                    Ingredient: $ingredient
                    Matched AFCD ID: ${matchedFood.foodKey}
                    Matched AFCD food: ${matchedFood.foodName}
                    Estimated grams: $estimatedGrams
                    Portion factor: $portionFactor

                    AFCD per 100g:
                    Protein: ${matchedFood.protein}
                    Carbs: ${matchedFood.carbs}
                    Fats: ${matchedFood.fats}

                    Ingredient contribution:
                    Protein: $ingredientProtein
                    Carbs: $ingredientCarbs
                    Fats: $ingredientFats
                    ------------------------------
                    """.trimIndent()
                )

                totalProtein += ingredientProtein
                totalCarbs += ingredientCarbs
                totalFats += ingredientFats

            } else {

                Log.d(
                    "AFCD_DEBUG",
                    """
                    ------------------------------
                    NO AFCD MATCH
                    Ingredient: $ingredient
                    ------------------------------
                    """.trimIndent()
                )
            }
        }

        val totalCalories = (
                (totalProtein * 4.0) +
                        (totalCarbs * 4.0) +
                        (totalFats * 9.0)
                ).roundToInt()

        Log.d(
            "AFCD_DEBUG",
            """
            ==============================
            RECIPE TOTAL
            Recipe: ${recipe.title}
            Protein: $totalProtein
            Carbs: $totalCarbs
            Fats: $totalFats
            Calories: $totalCalories
            ==============================
            """.trimIndent()
        )

        return NutritionInfo(
            calories = totalCalories,
            protein = totalProtein,
            carbs = totalCarbs,
            fats = totalFats
        )
    }

    fun analyzeDailyPlan(
        mealPlan: MealPlan
    ): NutritionInfo {

        val nutritionItems =
            mealPlan.dailyMeals.map { meal ->
                analyzeRecipe(meal)
            }

        return calculateTotalNutrition(
            nutritionItems
        )
    }

    fun calculateTotalNutritionFromAfcdIds(
        afcdFoodIds: List<String>,
        dataSource: AfcdNutritionDataSource
    ): NutritionInfo {

        val nutritionItems =
            afcdFoodIds.mapNotNull { afcdFoodId ->
                dataSource.getNutritionByAfcdId(
                    afcdFoodId
                )
            }

        return calculateTotalNutrition(
            nutritionItems
        )
    }

    fun calculateTotalNutritionFromFoodItems(
        foodItems: List<FoodItem>,
        dataSource: AfcdNutritionDataSource
    ): NutritionInfo {

        val afcdFoodIds =
            foodItems.mapNotNull { foodItem ->
                foodItem.afcdFoodId
            }

        return calculateTotalNutritionFromAfcdIds(
            afcdFoodIds,
            dataSource
        )
    }
}