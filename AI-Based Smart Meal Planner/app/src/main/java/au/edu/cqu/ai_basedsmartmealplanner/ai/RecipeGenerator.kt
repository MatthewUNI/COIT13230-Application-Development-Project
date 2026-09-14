package au.edu.cqu.ai_basedsmartmealplanner.ai

import au.edu.cqu.ai_basedsmartmealplanner.model.Recipe
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

object RecipeGenerator {

    private val gson = Gson()

    /**
     * Generates detailed instructions, required ingredients, and substitutions
     * based on user constraints and on-hand inventory.
     */
    suspend fun requestRecipeDetails(
        recipeName: String,
        dietaryRequirements: List<String>,
        availableIngredients: List<String>
    ): Recipe? {
        // 1. Build prompt requiring ingredient alternatives if items are missing
        val prompt = PromptBuilder.buildRecipePrompt(
            recipeName = recipeName,
            dietaryRequirements = dietaryRequirements,
            availableIngredients = availableIngredients
        )

        // 2. Fetch response asynchronously via GenAIClient
        val response = GenAIClient.fetchMealPlanAsync(prompt) ?: return null

        val rawText = response.candidates?.firstOrNull()
            ?.content?.parts?.firstOrNull()
            ?.text ?: return null

        return parseRecipeJson(rawText)
    }

    /**
     * Parses the Gemini JSON response into a Recipe data object[cite: 1].
     */
    fun parseRecipeJson(jsonResponse: String): Recipe? {
        return try {
            val cleanedJson = jsonResponse
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            gson.fromJson(cleanedJson, Recipe::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }
}