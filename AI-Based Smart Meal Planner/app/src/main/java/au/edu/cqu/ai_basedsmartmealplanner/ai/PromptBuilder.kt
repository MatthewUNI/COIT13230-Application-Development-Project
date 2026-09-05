package au.edu.cqu.ai_basedsmartmealplanner.ai

object PromptBuilder {

    fun buildMealPlanPrompt(dietaryRestrictions: List<String>, availableIngredients: List<String>): String {
        val constraints = dietaryRestrictions.joinToString(", ")
        val inventory = availableIngredients.joinToString(", ")

        return """
            You are a strict nutritional meal planner. 
            Generate a 7-day meal plan strictly adhering to the following dietary restrictions: $constraints.
            Prioritize using the following available ingredients: $inventory.
            Do NOT hallucinate ingredients that violate the dietary restrictions.
            Respond ONLY with a valid JSON object matching the requested schema. No conversational filler.
        """.trimIndent()
    }

    fun buildIngredientSubstitutionPrompt(missingIngredient: String, inventory: List<String>): String {
        return """
            The user needs a substitute for $missingIngredient.
            Review their current available ingredients: ${inventory.joinToString(", ")}.
            If a suitable substitute exists in their inventory, suggest it. 
            If not, suggest the most common, diet-compliant alternative.
            Respond strictly in JSON format.
        """.trimIndent()
    }
}