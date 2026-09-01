package au.edu.cqu.ai_basedsmartmealplanner.nutrition

import au.edu.cqu.ai_basedsmartmealplanner.model.NutritionInfo



class NutritionAnalysisEngine {

    fun calculateTotalNutrition(items: List<NutritionInfo>): NutritionInfo {
        return NutritionInfo(
            energyKj = items.sumOf { it.energyKj },
            proteinG = items.sumOf { it.proteinG },
            carbohydratesG = items.sumOf { it.carbohydratesG },
            fatG = items.sumOf { it.fatG },
            fibreG = items.sumOf { it.fibreG },
            sugarsG = items.sumOf { it.sugarsG },
            sodiumMg = items.sumOf { it.sodiumMg }
        )
    }
}