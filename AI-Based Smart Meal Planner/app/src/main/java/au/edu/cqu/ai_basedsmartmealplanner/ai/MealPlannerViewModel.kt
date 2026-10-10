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
import kotlinx.coroutines.withContext
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

    private val _excludedMealTypes = MutableStateFlow<Set<String>>(emptySet())

    val excludedMealTypes: StateFlow<Set<String>> = _excludedMealTypes.asStateFlow()

    private val _savedPlans = MutableStateFlow<List<SavedMealPlanEntity>>(emptyList())
    val savedPlans: StateFlow<List<SavedMealPlanEntity>> = _savedPlans.asStateFlow()

    private var isGroceryTodayOnly = false
    private val gson = Gson()

    init {
        loadSavedPlans()
    }

    private fun calculateTodayNutrition(mealPlan: MealPlan): NutritionInfo {
        val today = SimpleDateFormat(
            "EEEE",
            Locale.ENGLISH
        ).format(Date())

        val excluded = _excludedMealTypes.value

        val todaysMeals = mealPlan.dailyMeals.filter { meal ->
            meal.day.equals(today, ignoreCase = true) &&
                    excluded.none {
                        it.equals(meal.mealType, ignoreCase = true)
                    }
        }

        val todaysMealPlan = mealPlan.copy(
            dailyMeals = todaysMeals
        )

        return NutritionAnalysisEngine.analyzeDailyPlan(todaysMealPlan)
    }

    fun toggleMealExcluded(mealType: String) {
        val current = _excludedMealTypes.value

        _excludedMealTypes.value =
            if (current.any { it.equals(mealType, ignoreCase = true) }) {
                current.filterNot {
                    it.equals(mealType, ignoreCase = true)
                }.toSet()
            } else {
                current + mealType
            }

        val currentState = _uiState.value

        if (currentState is MealUiState.Success) {
            viewModelScope.launch {
                val nutrition = withContext(Dispatchers.Default) {
                    calculateTodayNutrition(currentState.mealPlan)
                }

                _nutritionInfo.value = nutrition
            }
        }
    }

    private fun resetMealExclusions() {
        _excludedMealTypes.value = emptySet()
    }

    fun saveCurrentMealPlan(planName: String) {
        val currentState = _uiState.value
        if (currentState is MealUiState.Success) {
            viewModelScope.launch(Dispatchers.IO) {
                val entity = SavedMealPlanEntity(
                    title = planName,
                    planJson = gson.toJson(currentState.mealPlan)
                )
                mealPlanDao.insertMealPlan(entity)
                loadSavedPlans()
            }
        }
    }

    fun loadSavedPlans() {
        viewModelScope.launch(Dispatchers.IO) {
            _savedPlans.value =
                mealPlanDao.getAllSavedMealPlans()
        }
    }

    fun deleteSavedPlan(entity: SavedMealPlanEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            mealPlanDao.deleteMealPlan(entity)
            loadSavedPlans()
        }
    }

    fun loadSavedGroceryList() {
        viewModelScope.launch(Dispatchers.IO) {

            val savedItems =
                groceryItemDao.getAll()

            if (savedItems.isNotEmpty()) {

                _groceryList.value = GroceryList(
                    items = savedItems.map {
                        it.toGroceryItem()
                    }
                )

            } else {

                _groceryList.value = null
            }
        }
    }

    fun generateMealPlan(
        isPantryOnly: Boolean = false
    ) {

        val profile =
            profileManager.getProfile()

        generateMealPlan(
            ingredients = profile.availableIngredients,
            dietaryRestrictions = profile.dietaryRequirements,
            isPantryOnly = isPantryOnly
        )
    }

    fun restoreSavedPlan(
        entity: SavedMealPlanEntity
    ) {

        viewModelScope.launch {

            try {

                val restoredPlan =
                    withContext(Dispatchers.Default) {
                        gson.fromJson(
                            entity.planJson,
                            MealPlan::class.java
                        )
                    }

                _uiState.value =
                    MealUiState.Success(restoredPlan)

                resetMealExclusions()

                loadSavedGroceryList()

                // Nutrition analysis can perform many AFCD
                // ingredient comparisons, so keep it off
                // the main/UI thread.
                val nutrition =
                    withContext(Dispatchers.Default) {
                        calculateTodayNutrition(
                            restoredPlan
                        )
                    }

                _nutritionInfo.value =
                    nutrition

            } catch (e: Exception) {

                _uiState.value =
                    MealUiState.Error(
                        "Failed to restore saved plan: ${e.localizedMessage}"
                    )
            }
        }
    }

    fun generateMealPlan(
        ingredients: List<String>,
        dietaryRestrictions: List<String>,
        isPantryOnly: Boolean = false
    ) {

        _uiState.value =
            MealUiState.Loading

        viewModelScope.launch {

            try {

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

                val goalGuidance =
                    when (profile.goalType.lowercase()) {

                        "lose" -> """
                            The user's goal is weight loss.
                            Use sensible meal portions intended to support a moderate calorie deficit.
                            Aim for approximately:
                            - 35% of daily calories from protein
                            - 35% of daily calories from carbohydrates
                            - 30% of daily calories from fat
                        """.trimIndent()

                        "gain" -> """
                            The user's goal is weight gain.
                            Use sensible meal portions intended to support a moderate calorie surplus.
                            Aim for approximately:
                            - 30% of daily calories from protein
                            - 45% of daily calories from carbohydrates
                            - 25% of daily calories from fat
                        """.trimIndent()

                        else -> """
                            The user's goal is weight maintenance.
                            Use sensible meal portions intended to support maintaining their current body weight.
                            Aim for approximately:
                            - 30% of daily calories from protein
                            - 40% of daily calories from carbohydrates
                            - 30% of daily calories from fat
                        """.trimIndent()
                    }

                val pantryGuidance =
                    if (isPantryOnly) {

                        """
                        STRICT PANTRY-ONLY RULE:
                        You MUST ONLY use the ingredients listed in the user's 'Available ingredients'.
                        Do NOT suggest any recipes requiring ingredients outside of this exact list.
                        You may assume the user has basic staples like water, salt, pepper, and cooking oil.
                        """.trimIndent()

                    } else {

                        """
                        INGREDIENT USAGE:
                        Prioritize the user's 'Available ingredients' to reduce waste.
                        You may include recipes that require additional ingredients, which will be added to the user's grocery list.
                        """.trimIndent()
                    }

                val promptText = """
                    You are a nutritional meal planner.
                    Create a complete personalised 7-day meal plan using the following user profile.

                    USER PROFILE:
                    Goal: ${profile.goalType}
                    Current weight: ${profile.currentWeight} kg
                    Target weight: ${profile.targetWeight} kg
                    Dietary requirements: $dietaryText
                    Food preferences: $preferenceText
                    Available ingredients: $ingredientList

                    WEIGHT GOAL:
                    $goalGuidance

                    MACRONUTRIENT GUIDANCE:
                    - Treat the macronutrient percentages above as percentages of total calories, not percentages by weight.
                    - Protein provides approximately 4 calories per gram.
                    - Carbohydrates provide approximately 4 calories per gram.
                    - Fat provides approximately 9 calories per gram.
                    - Adjust ingredient quantities and meal composition so the total daily nutrition is reasonably close to the target macronutrient distribution.
                    - Avoid excessively high fat, carbohydrate, or protein totals that significantly distort the target ratio.
                    - Spread nutrition reasonably across Breakfast, Lunch, and Dinner.
                    - Use realistic portion sizes for one adult.
                    - Avoid excessively large ingredient quantities.
                    - Weight-loss plans should generally contain less total energy than maintenance plans.
                    - Weight-gain plans should generally contain more total energy than maintenance plans.

                    $pantryGuidance

                    Generate exactly 3 meals for each day (Breakfast, Lunch, Dinner) for all 7 days.

                    INGREDIENT UNIT REQUIREMENTS:
                    - Every ingredient MUST include a numeric quantity.
                    - Use grams (g) for ALL solid and semi-solid ingredients.
                    - Use millilitres (ml) ONLY for liquids.
                    - Eggs may be given as a whole number.
                    - Do NOT use cups, tablespoons, teaspoons, ounces, cans, slices, handfuls, or bunches. Convert to grams/ml.
                    - Do NOT use vague quantities such as "to taste".

                    DIETARY REQUIREMENTS:
                    - Strictly follow all listed dietary requirements.

                    FOOD PREFERENCES:
                    - Prefer the user's listed food preferences where practical.

                    Each meal must include the day, meal type, title, ingredients with quantities, cooking instructions, and total calories.
                    Output strictly as a JSON object matching the required schema.
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
                            withContext(Dispatchers.Default) {
                                gson.fromJson(
                                    rawText,
                                    MealPlan::class.java
                                )
                            }

                        _uiState.value =
                            MealUiState.Success(parsedPlan)

                        resetMealExclusions()
                        // Grocery list generation is also
                        // computational work, so do it away
                        // from the main thread.
                        generateGroceryList()

                        // AFCD matching and nutrition analysis
                        // must not run on the UI thread.
                        val nutrition =
                            withContext(Dispatchers.Default) {
                                calculateTodayNutrition(
                                    parsedPlan
                                )
                            }

                        _nutritionInfo.value =
                            nutrition

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

    fun updateGroceryListTimeframe(
        isTodayOnly: Boolean
    ) {

        this.isGroceryTodayOnly =
            isTodayOnly

        generateGroceryList()
    }

    fun generateGroceryList() {

        val currentState =
            _uiState.value

        if (currentState
                    is MealUiState.Success
        ) {

            val currentProfile =
                profileManager.getProfile()

            viewModelScope.launch {

                // Generate list away from UI thread.
                val generatedList =
                    withContext(
                        Dispatchers.Default
                    ) {

                        GroceryListGenerator
                            .generateFromMealPlan(
                                mealPlan =
                                    currentState.mealPlan,

                                availableIngredients =
                                    currentProfile
                                        .availableIngredients,

                                isTodayOnly =
                                    isGroceryTodayOnly
                            )
                    }

                _groceryList.value =
                    generatedList

                saveGroceryList(
                    generatedList
                )
            }
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

                if (
                    item.name ==
                    itemName
                ) {
                    item.copy(
                        isPurchased =
                            isPurchased
                    )
                } else {
                    item
                }
            }

        _groceryList.value =
            currentList.copy(
                items = updatedItems
            )

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