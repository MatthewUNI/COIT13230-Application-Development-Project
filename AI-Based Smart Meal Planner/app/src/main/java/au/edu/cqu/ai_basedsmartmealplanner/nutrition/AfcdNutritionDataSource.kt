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

        var text = ingredient.lowercase().trim()

        // Convert common alternative food names into terminology
        // more likely to be used by the Australian AFCD dataset.
        text = text
            .replace("ground beef", "beef mince")
            .replace("ground turkey", "turkey mince")
            .replace("ground chicken", "chicken mince")
            .replace("ground pork", "pork mince")

        if (foodRecords.isEmpty()) {
            return null
        }

        // Remove quantities and measurement units from Gemini's ingredient text.
        // Example:
        // "300g chicken thighs" -> "chicken thighs"
        // "2 cans tuna in oil" -> "tuna in oil"
        // "30ml olive oil" -> "olive oil"
        val cleanedIngredient = text
            .replace(Regex("""\d+\s*/\s*\d+"""), " ")
            .replace(Regex("""\d+(?:\.\d+)?"""), " ")
            .replace(
                Regex(
                    """\b(g|gram|grams|kg|ml|millilitre|millilitres|milliliter|milliliters|""" +
                            """cup|cups|tbsp|tablespoon|tablespoons|tsp|teaspoon|teaspoons|""" +
                            """oz|ounce|ounces|slice|slices|can|cans)\b"""
                ),
                " "
            )
            .replace(Regex("""[^a-z\s-]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        if (cleanedIngredient.isBlank()) {
            return null
        }

        // Words that do not identify the actual food.
        val ignoredWords = setOf(
            "a", "an", "the",
            "of", "and", "or", "with",
            "in", "to", "for",
            "fresh", "chopped", "diced",
            "sliced", "shredded",
            "trimmed", "rinsed", "drained",
            "peeled", "deveined",
            "large", "small", "medium"
        )

        // Preparation/descriptive words are useful for selecting between
        // multiple AFCD records, but should not be required for a match.
        val descriptorWords = setOf(
            "raw",
            "cooked",
            "boiled",
            "grilled",
            "baked",
            "roasted",
            "fried",
            "dry",
            "dried",
            "canned",
            "white",
            "brown",
            "black",
            "wholemeal",
            "wholegrain",
            "whole",
            "plain",
            "greek",
            "frozen",
            "smoked"
        )

        fun normaliseWord(word: String): String {
            return when {
                // Words that naturally end in "s" and should not be changed.
                word in setOf(
                    "asparagus",
                    "hummus",
                    "couscous"
                ) -> word

                // berries -> berry
                word.endsWith("ies") && word.length > 4 ->
                    word.dropLast(3) + "y"

                // tomatoes -> tomato
                word.endsWith("oes") && word.length > 4 ->
                    word.dropLast(2)

                // thighs -> thigh, carrots -> carrot, beans -> bean
                word.endsWith("s") &&
                        !word.endsWith("ss") &&
                        word.length > 3 ->
                    word.dropLast(1)

                else -> word
            }
        }

        val ingredientWords = cleanedIngredient
            .split(Regex("""\s+"""))
            .map { normaliseWord(it) }
            .filter {
                it.length > 1 &&
                        it !in ignoredWords
            }

        if (ingredientWords.isEmpty()) {
            return null
        }

        val mainIngredientWords = ingredientWords.filter {
            it !in descriptorWords
        }

        if (mainIngredientWords.isEmpty()) {
            return null
        }

        val ingredientDescriptors = ingredientWords.filter {
            it in descriptorWords
        }

        data class ScoredFood(
            val record: AfcdFoodRecord,
            val score: Int
        )

        val candidates = foodRecords.mapNotNull { record ->

            val recordText = record.foodName
                .lowercase()
                .replace(Regex("""[^a-z\s-]"""), " ")

            val recordWords = recordText
                .split(Regex("""\s+"""))
                .map { normaliseWord(it) }
                .filter { it.length > 1 }
                .toSet()

            // At least one actual food word must match.
            val matchedMainWords = mainIngredientWords.count {
                it in recordWords
            }

            if (matchedMainWords == 0) {
                return@mapNotNull null
            }

            var score = 0

            // Main food words are the most important.
            score += matchedMainWords * 20

            // Strongly prefer records matching ALL main food words.
            if (matchedMainWords == mainIngredientWords.size) {
                score += 40
            }

            // Penalise records that only match part of a multi-word food.
            val missingMainWords =
                mainIngredientWords.size - matchedMainWords

            score -= missingMainWords * 25

            // Descriptors help choose the most appropriate AFCD variant.
            val matchedDescriptors = ingredientDescriptors.count {
                it in recordWords
            }

            score += matchedDescriptors * 8

            // Prefer AFCD names containing the complete cleaned food phrase.
            if (recordText.contains(cleanedIngredient)) {
                score += 30
            }

            // Strongly prefer records where the requested food appears
            // near the beginning of the AFCD food name.
            val simplifiedRecordText = recordText
                .replace(Regex("""\s+"""), " ")
                .trim()

            val mainPhrase =
                mainIngredientWords.joinToString(" ")

            if (
                simplifiedRecordText.startsWith(mainPhrase) ||
                mainIngredientWords.all { word ->
                    simplifiedRecordText
                        .split(Regex("""\s+"""))
                        .take(mainIngredientWords.size + 2)
                        .map { normaliseWord(it) }
                        .contains(word)
                }
            ) {
                score += 50
            }

            // Penalise composite dishes containing lots of unrelated foods.
            val ingredientWordSet =
                ingredientWords.toSet()

            val extraRecordWords =
                recordWords.count { word ->
                    word !in ingredientWordSet &&
                            word !in ignoredWords &&
                            word !in descriptorWords
                }

            score -= extraRecordWords * 3


            // Some useful preparation preferences.
            if ("dry" in ingredientDescriptors &&
                ("dry" in recordWords || "dried" in recordWords)
            ) {
                score += 10
            }

            if ("canned" in ingredientDescriptors &&
                "canned" in recordWords
            ) {
                score += 10
            }

            if ("cooked" in ingredientDescriptors &&
                ("cooked" in recordWords ||
                        "boiled" in recordWords ||
                        "baked" in recordWords ||
                        "grilled" in recordWords)
            ) {
                score += 8
            }

            // Avoid clearly conflicting preparation types.
            if ("raw" in ingredientDescriptors &&
                "raw" !in recordWords &&
                ("cooked" in recordWords ||
                        "boiled" in recordWords ||
                        "fried" in recordWords)
            ) {
                score -= 10
            }

            ScoredFood(record, score)
        }

        return candidates
            .maxByOrNull { it.score }
            ?.takeIf { it.score >= 20 }
            ?.record
    }

    fun estimateIngredientGrams(ingredient: String): Double {
        val text = ingredient.lowercase().trim()

        val quantityMatch =
            Regex("""(\d+/\d+|\d+(?:\.\d+)?)""").find(text)

        val quantity = quantityMatch?.value?.let { value ->
            if (value.contains("/")) {
                val parts = value.split("/")
                val numerator =
                    parts.getOrNull(0)?.toDoubleOrNull() ?: 1.0
                val denominator =
                    parts.getOrNull(1)?.toDoubleOrNull() ?: 1.0

                numerator / denominator
            } else {
                value.toDoubleOrNull() ?: 1.0
            }
        } ?: 1.0

        return when {

            // Ounces
            Regex("""\b(oz|ounce|ounces)\b""")
                .containsMatchIn(text) ->
                quantity * 28.35

            // Kilograms
            Regex("""\bkg\b""")
                .containsMatchIn(text) ->
                quantity * 1000.0

            // Grams
            Regex("""\d+(?:\.\d+)?\s*g\b|\bgrams?\b""")
                .containsMatchIn(text) ->
                quantity


            // Millilitres
            Regex("""\bml\b|\bmillilitres?\b|\bmilliliters?\b""")
                .containsMatchIn(text) ->
                quantity

            // Cups
            Regex("""\b(cup|cups)\b""")
                .containsMatchIn(text) ->
                quantity * 150.0

            // Tablespoons
            Regex("""\b(tbsp|tablespoon|tablespoons)\b""")
                .containsMatchIn(text) ->
                quantity * 15.0

            // Teaspoons
            Regex("""\b(tsp|teaspoon|teaspoons)\b""")
                .containsMatchIn(text) ->
                quantity * 5.0

            // Slices
            Regex("""\b(slice|slices)\b""")
                .containsMatchIn(text) ->
                quantity * 30.0

            // Countable chicken breast
            "chicken breast" in text ->
                quantity * 150.0

            // Countable eggs
            Regex("""\beggs?\b""")
                .containsMatchIn(text) ->
                quantity * 50.0

            // Countable tortillas
            Regex("""\btortillas?\b""")
                .containsMatchIn(text) ->
                quantity * 50.0

            // Generic fallback
            else ->
                100.0
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