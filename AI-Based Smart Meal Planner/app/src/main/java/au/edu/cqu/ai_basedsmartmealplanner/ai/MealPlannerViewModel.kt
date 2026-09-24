package au.edu.cqu.ai_basedsmartmealplanner.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import au.edu.cqu.ai_basedsmartmealplanner.BuildConfig
import au.edu.cqu.ai_basedsmartmealplanner.database.AppDatabase
import au.edu.cqu.ai_basedsmartmealplanner.database.SavedMealPlanEntity
import au.edu.cqu.ai_basedsmartmealplanner.database.toEntity
import au.edu.cqu.ai_basedsmartmealplanner.database.toGroceryItem
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MealPlannerViewModel(
    application: Application
) : AndroidViewModel(application) {

    val profileManager: UserProfileManager = UserProfileManager

    private val db = AppDatabase.getDatabase(application)
    private val mealPlanDao = db.mealPlanDao()
    private val groceryItemDao = db.groceryItemDao()

    private val _uiState = MutableStateFlow<MealUiState>(MealUiState.Idle)
    val uiState: StateFlow<MealUiState> = _uiState.asStateFlow()

    private val _groceryList = MutableStateFlow<GroceryList?>(null)
    val groceryList: StateFlow<GroceryList?> = _groceryList.asStateFlow()

    private val _nutritionInfo = MutableStateFlow<NutritionInfo?>(null)
    val nutritionInfo: StateFlow<NutritionInfo?> = _nutritionInfo.asStateFlow()

    private val _savedPlans =
        MutableStateFlow<List<SavedMealPlanEntity>>(emptyList())
    val savedPlans: StateFlow<List<SavedMealPlanEntity>> =
        _savedPlans.asStateFlow()

    private val gson = Gson()

    init {
        loadSavedPlans()
        loadSavedGroceryList()
    }

    private fun calculateTodayNutrition(
        mealPlan: MealPlan
    ): NutritionInfo {

        val today = SimpleDateFormat(
            "EEEE",
            Locale.ENGLISH
        ).format(Date())

        val todaysMeals = mealPlan.dailyMeals.filter { meal ->
            meal.day.equals(today, ignoreCase = true)
        }

        val todaysMealPlan = mealPlan.copy(
            dailyMeals = todaysMeals
        )

        return NutritionAnalysisEngine.analyzeDailyPlan(
            todaysMealPlan
        )
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
            _savedPlans.value =
                mealPlanDao.getAllSavedMealPlans()
        }
    }

    private fun loadSavedGroceryList() {

        viewModelScope.launch(Dispatchers.IO) {

            val savedItems =
                groceryItemDao.getAll()

            if (savedItems.isNotEmpty()) {

                _groceryList.value =
                    GroceryList(
                        items = savedItems.map {
                            it.toGroceryItem()
                        }
                    )
            }
        }
    }

    fun generateMealPlan() {

        val profile =
            profileManager.getProfile()

        generateMealPlan(
            ingredients = profile.availableIngredients,
            dietaryRestrictions = profile.dietaryRequirements
        )
    }

    /**
     * Deserializes a saved offline plan from Room back into the active UI state
     * and recalculates its groceries and nutrition metrics.
     */
    fun restoreSavedPlan(
        entity: SavedMealPlanEntity
    ) {

        try {

            val restoredPlan =
                gson.fromJson(
                    entity.planJson,
                    MealPlan::class.java
                )

            _uiState.value =
                MealUiState.Success(restoredPlan)

            val currentProfile =
                profileManager.getProfile()

            _groceryList.value =
                GroceryListGenerator.generateFromMealPlan(
                    mealPlan = restoredPlan,
                    availableIngredients =
                        currentProfile.availableIngredients
                )

            _nutritionInfo.value =
                calculateTodayNutrition(restoredPlan)

        } catch (e: Exception) {

            _uiState.value =
                MealUiState.Error(
                    "Failed to restore saved plan: ${e.localizedMessage}"
                )
        }
    }

    fun generateMealPlan(
        ingredients: List<String>
    ) {

        val profile =
            profileManager.getProfile()

        generateMealPlan(
            ingredients,
            profile.dietaryRequirements
        )
    }

    fun generateMealPlan(
        ingredients: List<String>,
        dietaryRestrictions: List<String>
    ) {

        _uiState.value =
            MealUiState.Loading

        viewModelScope.launch {

            try {

                /*
                 * Retrieve the complete profile here so the meal
                 * generation process can use the user's weight goal,
                 * weights and food preferences as well as their
                 * dietary requirements and available ingredients.
                 */
                val profile =
                    profileManager.getProfile()

                val ingredientList =
                    if (ingredients.isNotEmpty()) {
                        ingredients.joinToString(", ")
                    } else {
                        "standard pantry staples"
                    }

                val dietaryText =
                    if (dietaryRestrictions.isNotEmpty()) {
                        dietaryRestrictions.joinToString(", ")
                    } else {
                        "None"
                    }

                val preferenceText =
                    if (profile.foodPreferences.isNotEmpty()) {
                        profile.foodPreferences.joinToString(", ")
                    } else {
                        "No specific food preferences"
                    }

                /*
                 * Give Gemini different portion guidance depending
                 * on the user's selected profile goal.
                 */
                val goalGuidance =
                    when (profile.goalType.lowercase()) {

                        "lose" -> """
                            The user's goal is weight loss.
                            Use sensible meal portions intended to support
                            a moderate calorie deficit.
                        """.trimIndent()

                        "gain" -> """
                            The user's goal is weight gain.
                            Use sensible meal portions intended to support
                            a moderate calorie surplus.
                        """.trimIndent()

                        else -> """
                            The user's goal is weight maintenance.
                            Use sensible meal portions intended to support
                            maintaining their current body weight.
                        """.trimIndent()
                    }

                /*
                 * Ingredient quantities are particularly important.
                 *
                 * Ansh's nutrition component uses these quantities
                 * to estimate ingredient weight and scale AFCD
                 * protein/carbohydrate/fat values.
                 */
                val promptText = """
                    You are a nutritional meal planner.

                    Create a complete personalised 7-day meal plan
                    using the following user profile.

                    USER PROFILE:
                    Goal: ${profile.goalType}
                    Current weight: ${profile.currentWeight} kg
                    Target weight: ${profile.targetWeight} kg
                    Dietary requirements: $dietaryText
                    Food preferences: $preferenceText
                    Available ingredients: $ingredientList

                    WEIGHT GOAL:
                    $goalGuidance

                    Generate exactly 3 meals for each day:
                    Breakfast, Lunch and Dinner.

                    Generate meals for:
                    Monday, Tuesday, Wednesday, Thursday,
                    Friday, Saturday and Sunday.

                    PORTION REQUIREMENTS:
                    - Adjust meal portion sizes according to the user's
                      selected weight goal.
                    - Consider the user's current weight and target weight
                      when determining appropriate portions.
                    - Weight loss should use sensible portions intended
                      to support a calorie deficit.
                    - Weight gain should use sensible portions intended
                      to support a calorie surplus.
                    - Weight maintenance should use sensible portions
                      intended to maintain body weight.
                    - Do not use extreme calorie restriction or
                      excessive portion sizes.
                    
                    INGREDIENT UNIT REQUIREMENTS:
                    - Every ingredient MUST include a numeric quantity.
                    - Use grams (g) for ALL solid and semi-solid ingredients.
                    - This includes meat, fish, vegetables, fruit, rice,
                      pasta, bread, cheese, butter, cooking oils, sauces,
                      mayonnaise and similar ingredients.
                    - Use millilitres (ml) ONLY for liquids where a volume
                      measurement is appropriate, such as milk or other drinks.
                    - Prefer grams (g) for cream, sauces, oils, mayonnaise
                      and other ingredients where AFCD nutrition is measured
                      by weight.
                    - Eggs may be given as a whole number, for example
                      "2 eggs".
                    - Do NOT use cups, tablespoons, teaspoons, ounces,
                      cans, slices, handfuls, bunches, servings, pieces
                      or other measurement types.
                    - Convert any such measurement to grams or
                      millilitres before returning the ingredient.
                    - Do NOT use vague quantities such as "to taste",
                      "as needed", "a splash" or similar descriptions.
                    - Do NOT return an ingredient without a quantity.
                    - Use compact quantity formatting with no space
                      between the number and unit.
                    
                    CORRECT EXAMPLES:
                    - "250g chicken thighs"
                    - "180g dry pasta"
                    - "160g canned tuna in oil"
                    - "70g wholemeal bread"
                    - "100g cherry tomatoes"
                    - "60g mayonnaise"
                    - "20g olive oil"
                    - "200ml milk"
                    - "2 eggs"
                    
                    INCORRECT EXAMPLES:
                    - "2 cans tuna"
                    - "2 slices bread"
                    - "1 cup rice"
                    - "2 tbsp olive oil"
                    - "1 handful spinach"
                    - "1 bunch carrots"
                    - "1 serving pasta"
                    - "butter to taste"

                    DIETARY REQUIREMENTS:
                    - Strictly follow all listed dietary requirements.
                    - Avoid ingredients that conflict with those requirements.

                    FOOD PREFERENCES:
                    - Prefer the user's listed food preferences where practical.
                    - Food preferences do not override dietary requirements.

                    AVAILABLE INGREDIENTS:
                    - Prefer available ingredients where practical.
                    - Other ingredients may be used when necessary to
                      create a complete balanced meal plan.

                    Each meal must include:
                    - the day
                    - the meal type
                    - a meal title
                    - ingredients with quantities
                    - cooking instructions
                    - total calories

                    Output strictly as a JSON object matching
                    the required schema.
                """.trimIndent()

                val requestPayload =
                    mapOf(
                        "contents" to listOf(
                            mapOf(
                                "parts" to listOf(
                                    mapOf(
                                        "text" to promptText
                                    )
                                )
                            )
                        ),

                        "generationConfig" to mapOf(

                            "responseMimeType" to
                                    "application/json",

                            "responseSchema" to mapOf(

                                "type" to "OBJECT",

                                "properties" to mapOf(

                                    "plan_id" to mapOf(
                                        "type" to "STRING"
                                    ),

                                    "daily_meals" to mapOf(

                                        "type" to "ARRAY",

                                        "items" to mapOf(

                                            "type" to "OBJECT",

                                            "properties" to mapOf(

                                                "recipe_id" to mapOf(
                                                    "type" to "STRING"
                                                ),

                                                "title" to mapOf(
                                                    "type" to "STRING"
                                                ),

                                                "day" to mapOf(

                                                    "type" to "STRING",

                                                    "enum" to listOf(
                                                        "Monday",
                                                        "Tuesday",
                                                        "Wednesday",
                                                        "Thursday",
                                                        "Friday",
                                                        "Saturday",
                                                        "Sunday"
                                                    )
                                                ),

                                                "meal_type" to mapOf(

                                                    "type" to "STRING",

                                                    "enum" to listOf(
                                                        "Breakfast",
                                                        "Lunch",
                                                        "Dinner"
                                                    )
                                                ),

                                                "ingredients" to mapOf(

                                                    "type" to "ARRAY",

                                                    "items" to mapOf(
                                                        "type" to "STRING"
                                                    )
                                                ),

                                                "instructions" to mapOf(

                                                    "type" to "ARRAY",

                                                    "items" to mapOf(
                                                        "type" to "STRING"
                                                    )
                                                ),

                                                "total_calories" to mapOf(
                                                    "type" to "INTEGER"
                                                )
                                            ),

                                            "required" to listOf(
                                                "recipe_id",
                                                "title",
                                                "day",
                                                "meal_type",
                                                "ingredients",
                                                "instructions",
                                                "total_calories"
                                            )
                                        )
                                    )
                                ),

                                "required" to listOf(
                                    "plan_id",
                                    "daily_meals"
                                )
                            )
                        )
                    )

                val response =
                    GenAIClient.apiService.generateContent(
                        apiKey =
                            BuildConfig.GEMINI_API_KEY,
                        request =
                            requestPayload
                    )

                if (response.isSuccessful) {

                    val rawText =
                        response.body()
                            ?.candidates
                            ?.firstOrNull()
                            ?.content
                            ?.parts
                            ?.firstOrNull()
                            ?.text

                    if (rawText != null) {

                        val parsedPlan =
                            gson.fromJson(
                                rawText,
                                MealPlan::class.java
                            )

                        _uiState.value =
                            MealUiState.Success(parsedPlan)

                        _groceryList.value =
                            GroceryListGenerator.generateFromMealPlan(
                                mealPlan = parsedPlan,
                                availableIngredients = ingredients
                            )

                        _nutritionInfo.value =
                            calculateTodayNutrition(
                                parsedPlan
                            )

                    } else {

                        _uiState.value =
                            MealUiState.Error(
                                "Received empty response from AI."
                            )
                    }

                } else {

                    val errorMsg =
                        when (response.code()) {

                            400 ->
                                "Invalid AI request."

                            403 ->
                                "Gemini API access denied. Check API key/model permissions."

                            429 ->
                                "Rate limit reached. Please wait a moment."

                            503 ->
                                "AI service temporarily unavailable. Please retry."

                            else ->
                                "API Error: ${response.code()}"
                        }

                    _uiState.value =
                        MealUiState.Error(errorMsg)
                }

            } catch (e: Exception) {

                _uiState.value =
                    MealUiState.Error(
                        e.localizedMessage
                            ?: "Unknown network error"
                    )
            }
        }
    }

    fun addIngredient(
        ingredient: String
    ) {
        profileManager.addIngredient(
            ingredient
        )
    }

    fun removeIngredient(
        ingredient: String
    ) {
        profileManager.removeIngredient(
            ingredient
        )
    }

    /**
     * Generates or refreshes the grocery list based on the active
     * meal plan and the user's current inventory.
     */
    private fun saveGroceryList(
        groceryList: GroceryList
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            groceryItemDao.clearAll()

            groceryList.items.forEach { item ->
                groceryItemDao.insert(
                    item.toEntity()
                )
            }
        }
    }

    fun generateGroceryList() {

        val currentState =
            _uiState.value

        if (
            currentState
                    is MealUiState.Success
        ) {

            val currentProfile =
                profileManager.getProfile()

            val generatedList =
                GroceryListGenerator.generateFromMealPlan(
                    mealPlan =
                        currentState.mealPlan,
                    availableIngredients =
                        currentProfile.availableIngredients
                )

            _groceryList.value =
                generatedList

            saveGroceryList(
                generatedList
            )
        }
    }

    fun updateGroceryItemPurchased(
        itemName: String,
        isPurchased: Boolean
    ) {

        val currentList =
            _groceryList.value
                ?: return

        val updatedItems =
            currentList.items.map { item ->

                if (item.name == itemName) {

                    item.copy(
                        isPurchased =
                            isPurchased
                    )

                } else {

                    item
                }
            }

        val updatedList =
            currentList.copy(
                items = updatedItems
            )

        _groceryList.value =
            updatedList

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            val savedItems =
                groceryItemDao.getAll()

            savedItems
                .filter {
                    it.name.equals(
                        itemName,
                        ignoreCase = true
                    )
                }
                .forEach { entity ->

                    groceryItemDao.update(
                        entity.copy(
                            isPurchased =
                                isPurchased
                        )
                    )
                }
        }
    }
}