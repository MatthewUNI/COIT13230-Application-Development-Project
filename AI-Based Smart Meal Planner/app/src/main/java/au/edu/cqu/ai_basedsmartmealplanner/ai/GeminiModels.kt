package au.edu.cqu.ai_basedsmartmealplanner.ai

// The wrapper classes for Gemini's network response envelope
data class GeminiResponse(val candidates: List<Candidate>?)
data class Candidate(val content: Content?)
data class Content(val parts: List<Part>?)
data class Part(val text: String?)

// Your custom app-level meal plan structure
data class MealPlan(
    val title: String,
    val ingredients: List<String>,
    val instructions: List<String>
)