package au.edu.cqu.ai_basedsmartmealplanner.ai

// The wrapper classes for Gemini's network response envelope
data class GeminiResponse(val candidates: List<Candidate>? = null)
data class Candidate(val content: Content? = null)
data class Content(val parts: List<Part>? = null)
data class Part(val text: String? = null)
// Your custom app-level meal plan structure
data class MealPlan(
    val title: String,
    val ingredients: List<String>,
    val instructions: List<String>
)