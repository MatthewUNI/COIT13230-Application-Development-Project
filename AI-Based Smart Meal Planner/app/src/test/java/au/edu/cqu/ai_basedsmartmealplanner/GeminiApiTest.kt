package au.edu.cqu.ai_basedsmartmealplanner

import au.edu.cqu.ai_basedsmartmealplanner.ai.GenAIClient
import kotlinx.coroutines.runBlocking
import org.junit.Test

class GeminiApiTest {

    @Test
    fun testGeminiConnection() = runBlocking {
        // Grab the secure API key generated from local.properties
        val apiKey = BuildConfig.GEMINI_API_KEY

        // Define strict prompt instructions for your modular sandbox
        val promptText = """
            You are a strict nutritional meal planner.
            Generate a 1-day vegan meal plan using only: Rice, Beans, Broccoli, Tofu.
            Output strictly as a JSON object with:
            - 'title': String
            - 'ingredients': Array of Strings
            - 'instructions': Array of Strings (each step as an individual item in the array)
        """.trimIndent()

        // Construct the exact JSON payload format Gemini requires
        val requestPayload = mapOf(
            "contents" to listOf(
                mapOf(
                    "parts" to listOf(
                        mapOf("text" to promptText)
                    )
                )
            ),
            "generationConfig" to mapOf(
                "responseMimeType" to "application/json",
                "responseSchema" to mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "title" to mapOf("type" to "STRING"),
                        "ingredients" to mapOf(
                            "type" to "ARRAY",
                            "items" to mapOf("type" to "STRING")
                        ),
                        "instructions" to mapOf(
                            "type" to "ARRAY",
                            "items" to mapOf("type" to "STRING")
                        )
                    ),
                    "required" to listOf("title", "ingredients", "instructions")
                )
            )
        )

        println("Sending request to Gemini API...")

        try {
            val response = GenAIClient.apiService.generateContent(apiKey, requestPayload)

            if (response.isSuccessful) {
                // Dig through the response envelope to get the raw text string
                val cleanJson = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (cleanJson != null) {
                    // Parse the pure JSON into your MealPlan Kotlin object
                    val gson = com.google.gson.Gson()
                    val mealPlan = gson.fromJson(cleanJson, au.edu.cqu.ai_basedsmartmealplanner.ai.MealPlan::class.java)

                    println("Successfully Parsed Kotlin Object!")
                    println("Title: ${mealPlan.title}")
                    println("Ingredients Count: ${mealPlan.ingredients.size}")
                    println("First Step: ${mealPlan.instructions.firstOrNull()}")
                } else {
                    println("API returned empty text.")
                }
            } else {
                println("HTTP Error: ${response.code()} - ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
        println("Network Exception: ${e.message}")
    }
    }
}