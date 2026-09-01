package au.edu.cqu.ai_basedsmartmealplanner.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.edu.cqu.ai_basedsmartmealplanner.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealPlannerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MealUiState>(MealUiState.Idle)
    val uiState: StateFlow<MealUiState> = _uiState.asStateFlow()

    private val gson = Gson()

    fun generateMealPlan(ingredients: List<String>) {
        _uiState.value = MealUiState.Loading

        viewModelScope.launch {
            try {
                val ingredientList = ingredients.joinToString(", ")
                val promptText = """
                    You are a strict nutritional meal planner.
                    Generate a 1-day vegan meal plan using only: $ingredientList.
                    Output strictly as a JSON object with:
                    - 'title': String
                    - 'ingredients': Array of Strings
                    - 'instructions': Array of Strings
                """.trimIndent()

                val requestPayload = mapOf(
                    "contents" to listOf(
                        mapOf("parts" to listOf(mapOf("text" to promptText)))
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

                val response = GenAIClient.apiService.generateContent(
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = requestPayload
                )

                if (response.isSuccessful) {
                    val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (rawText != null) {
                        val parsedPlan = gson.fromJson(rawText, MealPlan::class.java)
                        _uiState.value = MealUiState.Success(parsedPlan)
                    } else {
                        _uiState.value = MealUiState.Error("Received empty response from AI.")
                    }
                } else {
                    _uiState.value = MealUiState.Error("API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = MealUiState.Error(e.localizedMessage ?: "Unknown network error")
            }
        }
    }
}