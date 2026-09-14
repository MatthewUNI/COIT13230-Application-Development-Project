package au.edu.cqu.ai_basedsmartmealplanner.profile

import au.edu.cqu.ai_basedsmartmealplanner.model.UserProfile

object UserProfileManager {

    private var userProfile = UserProfile()

    /**
     * Returns the current active user profile.
     */
    fun getProfile(): UserProfile {
        return userProfile
    }

    // Alias to support alternative naming conventions
    fun getUserProfile(): UserProfile = getProfile()

    /**
     * Replaces the active profile with updated data[cite: 1].
     */
    fun updateProfile(profile: UserProfile) {
        userProfile = profile
    }

    /**
     * Adds an ingredient to available ingredients, trimming whitespace
     * and ignoring duplicate case entries[cite: 1].
     */
    fun addIngredient(ingredient: String) {
        val trimmed = ingredient.trim()
        if (trimmed.isBlank()) return

        if (!userProfile.availableIngredients.any { it.equals(trimmed, ignoreCase = true) }) {
            userProfile.availableIngredients = userProfile.availableIngredients + trimmed
        }
    }

    /**
     * Removes an ingredient from the on-hand inventory[cite: 1].
     */
    fun removeIngredient(ingredient: String) {
        val trimmed = ingredient.trim()
        userProfile.availableIngredients = userProfile.availableIngredients.filterNot {
            it.equals(trimmed, ignoreCase = true)
        }
    }

    // Aliases returning Boolean feedback for UI status updates
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

    /**
     * Validates that required goal type and weight metrics are present[cite: 1].
     */
    fun isProfileValid(profile: UserProfile = userProfile): Boolean {
        return profile.goalType.isNotBlank() &&
                profile.currentWeight > 0 &&
                profile.targetWeight > 0
    }

    fun validateProfile(): Boolean {
        return isProfileValid(userProfile)
    }
}