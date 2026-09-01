package au.edu.cqu.ai_basedsmartmealplanner.ai

sealed interface MealUiState {
    object Idle : MealUiState
    object Loading : MealUiState
    data class Success(val mealPlan: MealPlan) : MealUiState
    data class Error(val message: String) : MealUiState
}