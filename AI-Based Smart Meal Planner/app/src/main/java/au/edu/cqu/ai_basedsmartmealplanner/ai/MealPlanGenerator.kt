package au.edu.cqu.ai_basedsmartmealplanner.ai

import au.edu.cqu.ai_basedsmartmealplanner.model.MealPlan
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

object MealPlanGenerator {

    private val gson = Gson()

    /**
     * Builds the prompt using PromptBuilder, calls GenAIClient,
     * and parses the returned JSON into a MealPlan object.
     */
    suspend fun generateWeeklyPlan(
        dietaryRestrictions: List<String>,
        availableIngredients: List<String>
    ): MealPlan? {
        // 1. Build the dynamic prompt using PromptBuilder
        val prompt = PromptBuilder.buildMealPlanPrompt(dietaryRestrictions, availableIngredients)

        // 2. Call the AI service via GenAIClient
        val response = GenAIClient.fetchMealPlanAsync(prompt) ?: return null

        // 3. Extract the text response from the Gemini payload structure
        val rawText = response.candidates?.firstOrNull()
            ?.content?.parts?.firstOrNull()
            ?.text ?: return null

        // 4. Parse JSON into MealPlan data class
        return parseMealPlanJson(rawText)
    }

    /**
     * Cleans up markdown code blocks if present and deserializes JSON into MealPlan.
     */
    fun parseMealPlanJson(jsonResponse: String): MealPlan? {
        return try {
            // Gemini often wraps JSON responses in ```json ... ``` blocks; clean them before parsing
            val cleanedJson = jsonResponse
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            gson.fromJson(cleanedJson, MealPlan::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }
}