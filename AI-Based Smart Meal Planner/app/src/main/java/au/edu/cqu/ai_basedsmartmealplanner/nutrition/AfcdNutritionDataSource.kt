package au.edu.cqu.ai_basedsmartmealplanner.nutrition

import android.content.Context
import au.edu.cqu.ai_basedsmartmealplanner.model.NutritionInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class AfcdFoodRecord(
    val foodKey: String,
    val foodName: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fats: Double
)

class AfcdNutritionDataSource(
    private val nutritionData: Map<String, NutritionInfo> = emptyMap()
) {

    private var foodRecords: List<AfcdFoodRecord> = emptyList()

    fun loadFromAssets(context: Context) {
        val json = context.assets
            .open("afcd_nutrition.json")
            .bufferedReader()
            .use { it.readText() }

        val type = object : TypeToken<List<AfcdFoodRecord>>() {}.type

        foodRecords = Gson().fromJson(json, type)
    }

    fun findFoodByIngredient(ingredient: String): AfcdFoodRecord? {

        val text = ingredient.lowercase()

        // Helper: return an exact verified AFCD record.
        fun byId(foodKey: String): AfcdFoodRecord? {
            return foodRecords.firstOrNull {
                it.foodKey.equals(foodKey, ignoreCase = true)
            }
        }

        // ---------------------------------------------------------
        // VERIFIED PREFERRED AFCD MATCHES
        // More specific phrases must be checked before general ones.
        // ---------------------------------------------------------

        return when {

            // Rice
            "cooked white rice" in text ->
                byId("F007661")

            "cooked brown rice" in text ->
                byId("F007641")

            // Banana
            "frozen banana" in text ->
                byId("F009884")

            "banana" in text ->
                byId("F000262")

            // Sweet potato must come before normal potato
            "sweet potato" in text ->
                byId("F009035")

            // Normal potato
            Regex("""\bpotato\b""").containsMatchIn(text) ->
                byId("F007325")

            // Broccoli - cooked approximation
            "broccoli" in text ->
                byId("F001904")

            // Bread
            ("whole wheat bread" in text ||
                    "wholemeal bread" in text ||
                    "whole grain bread" in text) ->
                byId("F001553")

            // Cheese
            "parmesan" in text ->
                byId("F002478")

            "feta" in text || "fetta" in text ->
                byId("F002452")

            "mozzarella" in text ->
                byId("F002472")

            "cheddar" in text ->
                byId("F002414")

            "shredded cheese" in text ->
                byId("F002414")

            // Eggs
            "hard-boiled egg" in text ||
                    "hard boiled egg" in text ->
                byId("F003721")

            "fried egg" in text ->
                byId("F003718")

            Regex("""\beggs?\b""").containsMatchIn(text) ->
                byId("F003729")

            // Peanut butter must be checked before generic butter
            "peanut butter" in text ->
                byId("F006577")

// Butter
            Regex("""\bbutter\b""").containsMatchIn(text) ->
                byId("F001973")

// Vegetable oil
            "vegetable oil" in text ->
                byId("F006191")

// Tomatoes - specific before general
            "cherry tomato" in text ||
                    "cherry tomatoes" in text ->
                byId("F009190")

            Regex("""\btomatoes?\b""").containsMatchIn(text) ->
                byId("F009193")

// Spinach
            Regex("""\bspinach\b""").containsMatchIn(text) ->
                byId("F008749")

// Carrot
            Regex("""\bcarrots?\b""").containsMatchIn(text) ->
                byId("F002276")

            // Mayonnaise
            ("light mayonnaise" in text ||
                    "low fat mayonnaise" in text ||
                    "low-fat mayonnaise" in text) ->
                byId("F005437")

            Regex("""\bmayonnaise\b""").containsMatchIn(text) ->
                byId("F005441")

            // Pasta
            // AFCD record verified for ordinary cooked white-wheat pasta.
            ("cooked pasta" in text ||
                    "cooked spaghetti" in text) &&
                    "whole wheat" !in text &&
                    "wholemeal" !in text ->
                byId("F006456")

            // Tortillas
            "corn tortilla" in text ->
                byId("F009854")

            // Whole-wheat tortilla was not available in our AFCD search.
            "whole wheat tortilla" in text ||
                    "wholemeal tortilla" in text ->
                null

            Regex("""\btortilla\b""").containsMatchIn(text) ->
                byId("F001669")

            // Onion
            Regex("""\bonions?\b""").containsMatchIn(text) ->
                byId("F006225")

            // Green beans
            "green beans" in text ||
                    "green bean" in text ->
                byId("F000431")

            // ---------------------------------------------------------
            // KNOWN UNSUPPORTED / UNSAFE MATCHES
            // Do not substitute an unrelated AFCD food.
            // ---------------------------------------------------------

            "black beans" in text ||
                    "black bean" in text ->
                null

            // Dataset search only found smoked cod records.
            Regex("""\bcod\b""").containsMatchIn(text) &&
                    "smoked" !in text ->
                null

            // Plain milk did not have a suitable verified AFCD record.
            Regex("""\bmilk\b""").containsMatchIn(text) &&
                    "almond milk" !in text ->
                null

            // Avoid known false matches until specifically mapped.
            "marinara" in text ->
                null

            // ---------------------------------------------------------
            // FALLBACK MATCHER
            // ---------------------------------------------------------

            else -> {

                val stopWords = setOf(
                    "cup", "cups",
                    "tbsp", "tablespoon", "tablespoons",
                    "tsp", "teaspoon", "teaspoons",
                    "oz", "ounce", "ounces",
                    "gram", "grams", "kg", "ml", "litre", "litres",
                    "slice", "slices",
                    "large", "small", "medium",
                    "fresh", "chopped", "diced", "sliced", "shredded",
                    "trimmed", "rinsed", "drained", "peeled", "deveined",
                    "handful", "bunch", "can", "splash",
                    "to", "taste", "and", "or", "of"
                )

                val descriptiveWords = setOf(
                    "cooked", "uncooked",
                    "boiled", "grilled", "baked", "roasted",
                    "raw", "dry", "dried",
                    "white", "brown", "black",
                    "plain", "greek",
                    "canned"
                )

                val ingredientWords = text
                    .replace(Regex("""\d+([./]\d+)?"""), " ")
                    .replace(Regex("""[^a-z\s]"""), " ")
                    .split(Regex("""\s+"""))
                    .filter { word ->
                        word.length > 2 && word !in stopWords
                    }

                if (ingredientWords.isEmpty()) {
                    null
                } else {

                    val mainFoodWords = ingredientWords.filter {
                        it !in descriptiveWords
                    }

                    // Conservative fallback:
                    // Only automatically match ingredients with at least
                    // two meaningful food words.
                    //
                    // Single-word foods should use the verified mappings above.
                    if (mainFoodWords.size < 2) {
                        null
                    } else {

                        val candidates = foodRecords.mapNotNull { record ->

                            val foodName = record.foodName.lowercase()

                            val allMainWordsMatch = mainFoodWords.all { word ->
                                Regex("""\b${Regex.escape(word)}s?\b""")
                                    .containsMatchIn(foodName)
                            }

                            if (!allMainWordsMatch) {
                                return@mapNotNull null
                            }

                            val descriptorMatches = ingredientWords
                                .filter { it in descriptiveWords }
                                .count { word ->
                                    Regex("""\b${Regex.escape(word)}\b""")
                                        .containsMatchIn(foodName)
                                }

                            record to descriptorMatches
                        }

                        candidates
                            .maxByOrNull { (_, score) -> score }
                            ?.first
                    }
                }
            }
        }
    }

    fun estimateIngredientGrams(ingredient: String): Double {
        val text = ingredient.lowercase().trim()

        val quantityMatch = Regex("""(\d+/\d+|\d+(?:\.\d+)?)""").find(text)

        val quantity = quantityMatch?.value?.let { value ->
            if (value.contains("/")) {
                val parts = value.split("/")
                val numerator = parts.getOrNull(0)?.toDoubleOrNull() ?: 1.0
                val denominator = parts.getOrNull(1)?.toDoubleOrNull() ?: 1.0
                numerator / denominator
            } else {
                value.toDoubleOrNull() ?: 1.0
            }
        } ?: 1.0

        return when {

            Regex("""\b(oz|ounce|ounces)\b""").containsMatchIn(text) ->
                quantity * 28.35

            Regex("""\bkg\b""").containsMatchIn(text) ->
                quantity * 1000.0

            Regex("""\d+(?:\.\d+)?\s*g\b|\bgrams?\b""").containsMatchIn(text) ->
                quantity

            Regex("""\b(cup|cups)\b""").containsMatchIn(text) ->
                quantity * 150.0

            Regex("""\b(tbsp|tablespoon|tablespoons)\b""").containsMatchIn(text) ->
                quantity * 15.0

            Regex("""\b(tsp|teaspoon|teaspoons)\b""").containsMatchIn(text) ->
                quantity * 5.0

            Regex("""\b(slice|slices)\b""").containsMatchIn(text) ->
                quantity * 30.0

            "chicken breast" in text ->
                quantity * 150.0

            "egg" in text ->
                quantity * 50.0

            "tortilla" in text ->
                quantity * 50.0

            else ->
                quantity * 100.0
        }
    }
    fun getNutritionByAfcdId(afcdFoodId: String): NutritionInfo? {

        val record = foodRecords.firstOrNull {
            it.foodKey.equals(afcdFoodId, ignoreCase = true)
        }

        if (record != null) {
            return NutritionInfo(
                calories = record.calories,
                protein = record.protein,
                carbs = record.carbs,
                fats = record.fats
            )
        }

        return nutritionData[afcdFoodId]
    }
}