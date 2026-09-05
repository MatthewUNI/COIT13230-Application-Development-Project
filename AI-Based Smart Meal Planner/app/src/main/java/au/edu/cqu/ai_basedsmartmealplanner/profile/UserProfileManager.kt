package au.edu.cqu.ai_basedsmartmealplanner.profile

import au.edu.cqu.ai_basedsmartmealplanner.model.UserProfile

class UserProfileManager {

    private var userProfile = UserProfile()

    fun getProfile(): UserProfile {
        return userProfile
    }

    fun updateProfile(profile: UserProfile) {
        userProfile = profile
    }

    fun addIngredient(ingredient: String) {
        if (ingredient.isBlank()) return

        if (!userProfile.availableIngredients.contains(ingredient)) {
            userProfile.availableIngredients =
                userProfile.availableIngredients + ingredient
        }
    }

    fun removeIngredient(ingredient: String) {
        userProfile.availableIngredients =
            userProfile.availableIngredients - ingredient
    }

    fun isProfileValid(profile: UserProfile): Boolean {
        return profile.goalType.isNotBlank() &&
                profile.currentWeight > 0 &&
                profile.targetWeight > 0
    }
}