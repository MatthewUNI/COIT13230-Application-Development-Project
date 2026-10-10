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

        /*
         * Convert common alternative food names into terminology that is
         * more likely to occur in the Australian AFCD dataset.
         *
         * Gemini may use American terminology such as "ground beef" or
         * "whole milk", while AFCD commonly uses Australian terminology.
         */
        text = text
            .replace("ground beef", "beef mince")
            .replace("ground turkey", "chicken mince")
            .replace("turkey mince", "chicken mince")
            .replace("ground chicken", "chicken mince")
            .replace("ground pork", "pork mince")
            .replace("whole milk", "full cream milk")
            .replace("whole wheat croutons", "wholemeal bread toasted")
            .replace("wholemeal croutons", "wholemeal bread toasted")
            .replace("whole wheat bread", "wholemeal bread")
            .replace("whole wheat", "wholemeal")
            .replace("low-fat cottage cheese", "cottage cheese")
            .replace("low fat cottage cheese", "cottage cheese")
            .replace("flank steak", "beef rump steak")
            .replace("light mayonnaise", "low fat mayonnaise")
            .replace("lite mayonnaise", "low fat mayonnaise")
            .replace("zucchini noodles", "zucchini")
            .replace("zoodles", "zucchini")
            .replace("liquid egg whites", "egg chicken white")
            .replace("egg whites", "egg chicken white")
            .replace("egg white", "egg chicken white")
            .replace("low-fat greek yogurt", "natural yoghurt")
            .replace("low fat greek yogurt", "natural yoghurt")
            .replace("greek yogurt", "natural yoghurt")
            .replace("greek yoghurt", "natural yoghurt")
            .replace("cod fillet", "blue grenadier hoki fillet")
            .replace("pineapple chunks", "pineapple")
            .replace("pineapple pieces", "pineapple")
            .replace("diced pineapple", "pineapple")
            .replace("white fish fillet", "blue grenadier hoki fillet")
            .replace("white fish", "blue grenadier hoki")


        if (foodRecords.isEmpty()) {
            return null
        }

        /*
         * Remove quantities and measurement units from Gemini ingredient
         * strings.
         *
         * Examples:
         * "300g chicken thighs" -> "chicken thighs"
         * "15ml olive oil" -> "olive oil"
         * "2 cans tuna in oil" -> "tuna in oil"
         */
        val cleanedIngredient = text
            .replace(Regex("""\d+\s+\d+\s*/\s*\d+"""), " ")
            .replace(Regex("""\d+\s*/\s*\d+"""), " ")
            .replace(Regex("""\d+(?:\.\d+)?"""), " ")
            .replace(
                Regex(
                    """\b(g|gram|grams|kg|kilogram|kilograms|""" +
                            """ml|millilitre|millilitres|milliliter|milliliters|""" +
                            """l|litre|litres|liter|liters|""" +
                            """cup|cups|tbsp|tablespoon|tablespoons|""" +
                            """tsp|teaspoon|teaspoons|""" +
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

        /*
         * Words which generally do not identify the food itself.
         */
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

        /*
         * Descriptive words help select between AFCD variants but should
         * not normally be required for a food to match.
         */
        val descriptorWords = setOf(
            "raw",
            "cooked",
            "cooking",
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
            "whole",
            "wholemeal",
            "wholegrain",
            "plain",
            "greek",
            "frozen",
            "smoked",
            "lean",
            "extra",
            "low",
            "reduced",
            "skinless",
            "boneless"
        )

        /*
         * These words describe a substantially different form of a food.
         *
         * For example:
         * avocado != avocado oil
         * apple != apple juice
         * milk != milk powder
         *
         * If an AFCD candidate contains one of these transformations but
         * the requested ingredient does not, the candidate is rejected.
         */
        val strictTransformationWords = setOf(
            "oil",
            "juice",
            "powder",
            "flour",
            "starch",
            "dried",
            "dehydrated",
            "cider"
        )

        fun normaliseWord(word: String): String {
            return when {

                /*
                 * Words that naturally end in "s" and should not have the
                 * final character removed.
                 */
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

        /*
         * Main ingredient words determine whether a candidate is actually
         * the requested food.
         */
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
                .replace(Regex("""\s+"""), " ")
                .trim()

            val recordWords = recordText
                .split(Regex("""\s+"""))
                .map { normaliseWord(it) }
                .filter { it.length > 1 }
                .toSet()

            /*
             * Reject transformed foods when the transformation was not
             * requested.
             *
             * Examples:
             *
             * "avocado" + "Oil, avocado"       -> reject
             * "olive oil" + "Oil, olive"       -> allow
             * "apple" + "Apple juice"          -> reject
             * "milk" + "Milk powder"           -> reject
             */
            val hasConflictingTransformation =
                strictTransformationWords.any { transformation ->
                    transformation in recordWords &&
                            transformation !in ingredientWords
                }

            if (hasConflictingTransformation) {
                return@mapNotNull null
            }

            /*
             * At least one actual food word must match.
             */
            val matchedMainWords = mainIngredientWords.count {
                it in recordWords
            }

            if (matchedMainWords == 0) {
                return@mapNotNull null
            }

            var score = 0

            /*
             * Main food words carry the greatest weight.
             */
            score += matchedMainWords * 20

            /*
             * Strongly prefer records matching all requested main food
             * words.
             */
            if (matchedMainWords == mainIngredientWords.size) {
                score += 40
            }

            /*
             * Penalise records matching only part of a multi-word food.
             */
            val missingMainWords =
                mainIngredientWords.size - matchedMainWords

            score -= missingMainWords * 25

            /*
             * Descriptors help choose the most appropriate variant.
             */
            val matchedDescriptors = ingredientDescriptors.count {
                it in recordWords
            }

            score += matchedDescriptors * 8

            /*
             * Prefer an AFCD name containing the complete cleaned phrase.
             */
            if (recordText.contains(cleanedIngredient)) {
                score += 30
            }

            /*
             * Strongly prefer records where the requested food appears
             * near the beginning of the AFCD food name.
             */
            val mainPhrase =
                mainIngredientWords.joinToString(" ")

            val beginningWords = recordText
                .split(Regex("""\s+"""))
                .take(mainIngredientWords.size + 2)
                .map { normaliseWord(it) }

            if (
                recordText.startsWith(mainPhrase) ||
                mainIngredientWords.all { word ->
                    word in beginningWords
                }
            ) {
                score += 50
            }

            /*
             * Penalise composite dishes containing unrelated food words.
             */
            val ingredientWordSet = ingredientWords.toSet()

            val extraRecordWords =
                recordWords.count { word ->
                    word !in ingredientWordSet &&
                            word !in ignoredWords &&
                            word !in descriptorWords
                }

            score -= extraRecordWords * 10

            /*
             * Preparation preferences.
             */
            if (
                "dry" in ingredientDescriptors &&
                ("dry" in recordWords || "dried" in recordWords)
            ) {
                score += 10
            }

            if (
                "canned" in ingredientDescriptors &&
                "canned" in recordWords
            ) {
                score += 10
            }

            if (
                "cooked" in ingredientDescriptors &&
                (
                        "cooked" in recordWords ||
                                "boiled" in recordWords ||
                                "baked" in recordWords ||
                                "grilled" in recordWords
                        )
            ) {
                score += 8
            }

            /*
             * Avoid clearly conflicting preparation types.
             */
            if (
                "raw" in ingredientDescriptors &&
                "raw" !in recordWords &&
                (
                        "cooked" in recordWords ||
                                "boiled" in recordWords ||
                                "fried" in recordWords
                        )
            ) {
                score -= 10
            }

            if (
                "lean" in ingredientDescriptors &&
                "lean" in recordWords
            ) {
                score += 10
            }

            /*
             * Full-cream milk preference.
             *
             * "whole milk" is normalised to "full cream milk" before
             * matching.
             */
            if (
                "full" in mainIngredientWords &&
                "cream" in mainIngredientWords &&
                "full" in recordWords &&
                "cream" in recordWords
            ) {
                score += 15
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

        /*
         * Supports:
         * 200
         * 15.5
         * 1/2
         * 1 1/2
         */
        val quantityMatch = Regex(
            """(\d+(?:\.\d+)?\s+\d+/\d+|\d+/\d+|\d+(?:\.\d+)?)"""
        ).find(text)

        val quantity = quantityMatch?.value?.trim()?.let { value ->

            when {

                /*
                 * Mixed fraction:
                 * "1 1/2" -> 1.5
                 */
                value.contains(" ") && value.contains("/") -> {

                    val parts = value.split(Regex("""\s+"""))

                    val whole =
                        parts[0].toDoubleOrNull() ?: 0.0

                    val fraction =
                        parts[1].split("/")

                    val numerator =
                        fraction.getOrNull(0)
                            ?.toDoubleOrNull() ?: 0.0

                    val denominator =
                        fraction.getOrNull(1)
                            ?.toDoubleOrNull() ?: 1.0

                    whole + (numerator / denominator)
                }

                /*
                 * Fraction:
                 * "1/2" -> 0.5
                 */
                value.contains("/") -> {

                    val parts = value.split("/")

                    val numerator =
                        parts.getOrNull(0)
                            ?.toDoubleOrNull() ?: 1.0

                    val denominator =
                        parts.getOrNull(1)
                            ?.toDoubleOrNull() ?: 1.0

                    numerator / denominator
                }

                /*
                 * Normal number:
                 * "200" or "15.5"
                 */
                else ->
                    value.toDoubleOrNull() ?: 1.0
            }

        } ?: 1.0

        return when {

            /*
             * Ounces
             * 8oz / 8 oz / 8 ounces
             */
            Regex(
                """\d+(?:\.\d+)?\s*oz\b|\bounces?\b"""
            ).containsMatchIn(text) ->
                quantity * 28.35

            /*
             * Kilograms
             * 1kg / 1 kg / 1 kilogram
             */
            Regex(
                """\d+(?:\.\d+)?\s*kg\b|\bkilograms?\b"""
            ).containsMatchIn(text) ->
                quantity * 1000.0

            /*
             * Grams
             * 200g / 200 g / 200 grams
             */
            Regex(
                """\d+(?:\.\d+)?\s*g\b|\bgrams?\b"""
            ).containsMatchIn(text) ->
                quantity

            /*
             * Millilitres
             *
             * For this prototype, 1 ml is approximated as 1 g.
             * This is not density-perfect but prevents liquids from
             * incorrectly falling back to 100 g.
             */
            Regex(
                """\d+(?:\.\d+)?\s*ml\b|\bmillilitres?\b|\bmilliliters?\b"""
            ).containsMatchIn(text) ->
                quantity

            /*
             * Litres
             */
            Regex(
                """\d+(?:\.\d+)?\s*l\b|\blitres?\b|\bliters?\b"""
            ).containsMatchIn(text) ->
                quantity * 1000.0

            /*
             * Cups.
             *
             * Generic approximation because actual gram weight varies
             * substantially by ingredient.
             */
            Regex(
                """\bcups?\b"""
            ).containsMatchIn(text) ->
                quantity * 150.0

            /*
             * Tablespoons.
             */
            Regex(
                """\btbsp\b|\btablespoons?\b"""
            ).containsMatchIn(text) ->
                quantity * 15.0

            /*
             * Teaspoons.
             */
            Regex(
                """\btsp\b|\bteaspoons?\b"""
            ).containsMatchIn(text) ->
                quantity * 5.0

            /*
             * Slices.
             */
            Regex(
                """\bslices?\b"""
            ).containsMatchIn(text) ->
                quantity * 30.0

            /*
             * Countable chicken breasts.
             */
            "chicken breast" in text ->
                quantity * 150.0

            /*
             * Countable eggs.
             */
            Regex(
                """\beggs?\b"""
            ).containsMatchIn(text) ->
                quantity * 50.0

            /*
             * Countable tortillas.
             */
            Regex(
                """\btortillas?\b"""
            ).containsMatchIn(text) ->
                quantity * 50.0

            /*
             * Generic fallback.
             *
             * Keep this for ingredients where Gemini supplies no usable
             * measurement. Debug output should make these cases visible
             * so additional conversions can be added when required.
             */
            else ->
                100.0
        }
    }

    fun getNutritionByAfcdId(
        afcdFoodId: String
    ): NutritionInfo? {

        val record = foodRecords.firstOrNull {
            it.foodKey.equals(
                afcdFoodId,
                ignoreCase = true
            )
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