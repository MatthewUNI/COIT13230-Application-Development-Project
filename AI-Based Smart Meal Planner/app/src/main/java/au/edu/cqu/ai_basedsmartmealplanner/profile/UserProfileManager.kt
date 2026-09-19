package au.edu.cqu.ai_basedsmartmealplanner.profile

import android.content.Context
import au.edu.cqu.ai_basedsmartmealplanner.model.UserProfile
import com.google.gson.Gson

object UserProfileManager {

    private const val PREFS_NAME = "user_profile_prefs"
    private const val KEY_PROFILE = "user_profile"

    private val gson = Gson()

    private var userProfile = UserProfile()

    /**
     * Loads the saved profile from SharedPreferences.
     */
    fun initialize(context: Context) {
        val preferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val profileJson = preferences.getString(KEY_PROFILE, null)

        if (!profileJson.isNullOrBlank()) {
            try {
                userProfile =
                    gson.fromJson(profileJson, UserProfile::class.java)
            } catch (e: Exception) {
                userProfile = UserProfile()
            }
        }
    }

    /**
     * Returns the current active user profile.
     */
    fun getProfile(): UserProfile {
        return userProfile
    }

    fun getUserProfile(): UserProfile = getProfile()

    /**
     * Updates the profile in memory and saves it permanently.
     */
    fun updateProfile(context: Context, profile: UserProfile) {
        userProfile = profile

        val preferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        preferences.edit()
            .putString(KEY_PROFILE, gson.toJson(profile))
            .apply()
    }

    /**
     * Adds an ingredient to the current profile.
     */
    fun addIngredient(ingredient: String) {
        val trimmed = ingredient.trim()

        if (trimmed.isBlank()) return

        if (!userProfile.availableIngredients.any {
                it.equals(trimmed, ignoreCase = true)
            }) {
            userProfile.availableIngredients =
                userProfile.availableIngredients + trimmed
        }
    }

    /**
     * Removes an ingredient from the current profile.
     */
    fun removeIngredient(ingredient: String) {
        val trimmed = ingredient.trim()

        userProfile.availableIngredients =
            userProfile.availableIngredients.filterNot {
                it.equals(trimmed, ignoreCase = true)
            }
    }

    fun addAvailableIngredient(ingredient: String): Boolean {
        val countBefore = userProfile.availableIngredients.size
        addIngredient(ingredient)

        return userProfile.availableIngredients.size > countBefore
    }

    fun removeAvailableIngredient(ingredient: String): Boolean {
        val countBefore = userProfile.availableIngredients.size
        removeIngredient(ingredient)

        return userProfile.availableIngredients.size < countBefore
    }

    fun isProfileValid(profile: UserProfile = userProfile): Boolean {
        return profile.goalType.isNotBlank() &&
                profile.currentWeight > 0 &&
                profile.targetWeight > 0
    }

    fun validateProfile(): Boolean {
        return isProfileValid(userProfile)
    }
}