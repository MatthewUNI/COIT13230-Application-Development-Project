package au.edu.cqu.ai_basedsmartmealplanner.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import au.edu.cqu.ai_basedsmartmealplanner.BuildConfig
import au.edu.cqu.ai_basedsmartmealplanner.database.AppDatabase
import au.edu.cqu.ai_basedsmartmealplanner.database.SavedMealPlanEntity
import au.edu.cqu.ai_basedsmartmealplanner.grocery.GroceryListGenerator
import au.edu.cqu.ai_basedsmartmealplanner.model.GroceryList
import au.edu.cqu.ai_basedsmartmealplanner.model.MealPlan
import au.edu.cqu.ai_basedsmartmealplanner.model.NutritionInfo
import au.edu.cqu.ai_basedsmartmealplanner.nutrition.NutritionAnalysisEngine
import au.edu.cqu.ai_basedsmartmealplanner.profile.UserProfileManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealPlannerViewModel(
    application: Application
) : AndroidViewModel(application) {

    val profileManager: UserProfileManager = UserProfileManager

    private val db = AppDatabase.getDatabase(application)
    private val mealPlanDao = db.mealPlanDao()

    private val _uiState = MutableStateFlow<MealUiState>(MealUiState.Idle)
    val uiState: StateFlow<MealUiState> = _uiState.asStateFlow()

    private val _groceryList = MutableStateFlow<GroceryList?>(null)
    val groceryList: StateFlow<GroceryList?> = _groceryList.asStateFlow()

    private val _nutritionInfo = MutableStateFlow<NutritionInfo?>(null)
    val nutritionInfo: StateFlow<NutritionInfo?> = _nutritionInfo.asStateFlow()

    private val _savedPlans = MutableStateFlow<List<SavedMealPlanEntity>>(emptyList())
    val savedPlans: StateFlow<List<SavedMealPlanEntity>> = _savedPlans.asStateFlow()

    private val gson = Gson()

    init {
        loadSavedPlans()
    }

    /**
     * Persists the current generated meal plan into the local Room database.
     */
    fun saveCurrentMealPlan() {
        val currentState = _uiState.value
        if (currentState is MealUiState.Success) {
            viewModelScope.launch(Dispatchers.IO) {
                val entity = SavedMealPlanEntity(
                    title = "Meal Plan - ${System.currentTimeMillis()}",
                    planJson = gson.toJson(currentState.mealPlan)
                )
                mealPlanDao.insertMealPlan(entity)
                loadSavedPlans()
            }
        }
    }

    /**
     * Retrieves all saved plans from local SQLite storage.
     */
    fun loadSavedPlans() {
        viewModelScope.launch(Dispatchers.IO) {
            _savedPlans.value = mealPlanDao.getAllSavedMealPlans()
        }
    }

    fun generateMealPlan() {
        val profile = profileManager.getProfile()
        generateMealPlan(
            ingredients = profile.availableIngredients,
            dietaryRestrictions = profile.dietaryRequirements
        )
    }

    /**
     * Deserializes a saved offline plan from Room back into the active UI state
     * and recalculates its groceries and nutrition metrics.
     */
    fun restoreSavedPlan(entity: SavedMealPlanEntity) {
        try {
            val restoredPlan = gson.fromJson(entity.planJson, MealPlan::class.java)
            _uiState.value = MealUiState.Success(restoredPlan)

            val currentProfile = profileManager.getProfile()
            _groceryList.value = GroceryListGenerator.generateFromMealPlan(
                mealPlan = restoredPlan,
                availableIngredients = currentProfile.availableIngredients
            )
            _nutritionInfo.value = NutritionAnalysisEngine.analyzeDailyPlan(restoredPlan)
        } catch (e: Exception) {
            _uiState.value = MealUiState.Error("Failed to restore saved plan: ${e.localizedMessage}")
        }
    }

    fun generateMealPlan(ingredients: List<String>) {
        val profile = profileManager.getProfile()
        generateMealPlan(ingredients, profile.dietaryRequirements)
    }

    fun generateMealPlan(ingredients: List<String>, dietaryRestrictions: List<String>) {
        _uiState.value = MealUiState.Loading

        viewModelScope.launch {
            try {
                val ingredientList = if (ingredients.isNotEmpty()) {
                    ingredients.joinToString(", ")
                } else {
                    "standard pantry staples"
                }

                val constraintText = if (dietaryRestrictions.isNotEmpty()) {
                    "adhering strictly to: ${dietaryRestrictions.joinToString(", ")}"
                } else {
                    "balanced nutrition"
                }

                val promptText = """
                    You are a strict nutritional meal planner.
                    Generate a meal plan $constraintText using available ingredients: $ingredientList.
                    Output strictly as a JSON object matching the required schema.
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
                                "dailyMeals" to mapOf(
                                    "type" to "ARRAY",
                                    "items" to mapOf(
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
                            ),
                            "required" to listOf("dailyMeals")
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
                        _groceryList.value = GroceryListGenerator.generateFromMealPlan(
                            mealPlan = parsedPlan,
                            availableIngredients = ingredients
                        )
                        _nutritionInfo.value = NutritionAnalysisEngine.analyzeDailyPlan(parsedPlan)
                    } else {
                        _uiState.value = MealUiState.Error("Received empty response from AI.")
                    }
                } else {


            val errorMsg = when (response.code()) {
                400 -> "Invalid AI request."
                403 -> "Gemini API access denied. Check API key/model permissions."
                429 -> "Rate limit reached. Please wait a moment."
                503 -> "AI service temporarily unavailable. Please retry."
                else -> "API Error: ${response.code()}"
            }

            _uiState.value = MealUiState.Error(errorMsg)
        }
            } catch (e: Exception) {
                _uiState.value = MealUiState.Error(e.localizedMessage ?: "Unknown network error")
            }
        }
    }

    fun addIngredient(ingredient: String) {
        profileManager.addIngredient(ingredient)
    }

    fun removeIngredient(ingredient: String) {
        profileManager.removeIngredient(ingredient)
    }

    /**
     * Generates or refreshes the grocery list based on the active meal plan
     * and the user's current inventory.
     */
    fun generateGroceryList() {
        val currentState = _uiState.value
        if (currentState is MealUiState.Success) {
            val currentProfile = profileManager.getProfile()
            _groceryList.value = GroceryListGenerator.generateFromMealPlan(
                mealPlan = currentState.mealPlan,
                availableIngredients = currentProfile.availableIngredients
            )
        }
    }
}