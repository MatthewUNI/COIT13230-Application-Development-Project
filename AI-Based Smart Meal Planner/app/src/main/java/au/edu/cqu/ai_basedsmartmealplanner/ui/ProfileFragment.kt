package au.edu.cqu.ai_basedsmartmealplanner.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import au.edu.cqu.ai_basedsmartmealplanner.R
import au.edu.cqu.ai_basedsmartmealplanner.model.UserProfile
import au.edu.cqu.ai_basedsmartmealplanner.profile.UserProfileManager
import com.google.android.material.textfield.TextInputEditText

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val availableIngredients = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        UserProfileManager.initialize(requireContext())

        val radioGroupGoal = view.findViewById<RadioGroup>(R.id.radioGroupGoal)
        val editCurrentWeight = view.findViewById<TextInputEditText>(R.id.editCurrentWeight)
        val editTargetWeight = view.findViewById<TextInputEditText>(R.id.editTargetWeight)
        val editDietaryRequirements = view.findViewById<TextInputEditText>(R.id.editDietaryRequirements)
        val editFoodPreferences = view.findViewById<TextInputEditText>(R.id.editFoodPreferences)
        val editIngredient = view.findViewById<TextInputEditText>(R.id.editIngredient)

        val buttonToggleIngredients = view.findViewById<Button>(R.id.buttonToggleIngredients)
        val buttonRemoveAllIngredients = view.findViewById<Button>(R.id.buttonRemoveAllIngredients)
        val buttonSaveProfile = view.findViewById<Button>(R.id.buttonSaveProfile)
        val buttonAddIngredient = view.findViewById<Button>(R.id.buttonAddIngredient)

        val ingredientsContainer = view.findViewById<LinearLayout>(R.id.ingredientsContainer)
        val textNoIngredients = view.findViewById<TextView>(R.id.textNoIngredients)

        // Helper function to save ingredients to your existing UserProfileManager instantly
        fun saveIngredientsToManager() {
            val currentProfile = UserProfileManager.getProfile()
            val updatedProfile = UserProfile(
                goalType = currentProfile.goalType,
                currentWeight = currentProfile.currentWeight,
                targetWeight = currentProfile.targetWeight,
                dietaryRequirements = currentProfile.dietaryRequirements,
                foodPreferences = currentProfile.foodPreferences,
                availableIngredients = availableIngredients.toList()
            )
            UserProfileManager.updateProfile(requireContext(), updatedProfile)
        }

        fun updateIngredientDisplay() {
            ingredientsContainer.removeAllViews()

            if (availableIngredients.isEmpty()) {
                ingredientsContainer.visibility = View.GONE
                textNoIngredients.visibility = View.VISIBLE
                buttonToggleIngredients.text = "Ingredients (0) ▼"
                buttonRemoveAllIngredients.visibility = View.GONE
                return
            }

            textNoIngredients.visibility = View.GONE

            if (ingredientsContainer.visibility == View.VISIBLE) {
                buttonToggleIngredients.text = "Ingredients (${availableIngredients.size}) ▲"
                buttonRemoveAllIngredients.visibility = View.VISIBLE
            } else {
                buttonToggleIngredients.text = "Ingredients (${availableIngredients.size}) ▼"
                buttonRemoveAllIngredients.visibility = View.GONE
            }

            availableIngredients.forEach { ingredient ->
                val row = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(8, 8, 8, 8)
                }

                val ingredientText = TextView(requireContext()).apply {
                    text = ingredient
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val removeButton = Button(requireContext()).apply {
                    text = "Remove"
                    textSize = 12f
                    minWidth = 0
                    minimumWidth = 0
                    minHeight = 0
                    minimumHeight = 0
                    setPadding(16, 4, 16, 4)

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    setOnClickListener {
                        availableIngredients.remove(ingredient)
                        saveIngredientsToManager() // Save the deletion immediately
                        updateIngredientDisplay()
                    }
                }

                row.addView(ingredientText)
                row.addView(removeButton)

                ingredientsContainer.addView(row)
            }
        }

        buttonToggleIngredients.setOnClickListener {
            if (availableIngredients.isEmpty()) {
                return@setOnClickListener
            }

            if (ingredientsContainer.visibility == View.VISIBLE) {
                ingredientsContainer.visibility = View.GONE
            } else {
                ingredientsContainer.visibility = View.VISIBLE
            }
            updateIngredientDisplay()
        }

        buttonRemoveAllIngredients.setOnClickListener {
            availableIngredients.clear()
            saveIngredientsToManager() // Save the cleared list immediately
            ingredientsContainer.visibility = View.GONE
            updateIngredientDisplay()
        }

        buttonAddIngredient.setOnClickListener {
            val ingredient = editIngredient.text.toString().trim()

            if (ingredient.isNotEmpty() && !availableIngredients.contains(ingredient)) {
                availableIngredients.add(ingredient)
                editIngredient.text?.clear()

                saveIngredientsToManager() // Save the new addition immediately

                ingredientsContainer.visibility = View.VISIBLE
                updateIngredientDisplay()
            }
        }

        buttonSaveProfile.setOnClickListener {
            val goalType = when (radioGroupGoal.checkedRadioButtonId) {
                R.id.radioLose -> "Lose"
                R.id.radioMaintain -> "Maintain"
                R.id.radioGain -> "Gain"
                else -> ""
            }

            val currentWeight = editCurrentWeight.text.toString().toDoubleOrNull() ?: 0.0
            val targetWeight = editTargetWeight.text.toString().toDoubleOrNull() ?: 0.0

            val dietaryRequirements = editDietaryRequirements.text.toString()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val foodPreferences = editFoodPreferences.text.toString()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val profile = UserProfile(
                goalType = goalType,
                currentWeight = currentWeight,
                targetWeight = targetWeight,
                dietaryRequirements = dietaryRequirements,
                foodPreferences = foodPreferences,
                availableIngredients = availableIngredients.toList()
            )

            if (UserProfileManager.isProfileValid(profile)) {
                UserProfileManager.updateProfile(requireContext(), profile)
                Toast.makeText(requireContext(), "Profile saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please complete all required fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Load the current saved profile back into the UI
        val savedProfile = UserProfileManager.getProfile()

        when (savedProfile.goalType) {
            "Lose" -> radioGroupGoal.check(R.id.radioLose)
            "Maintain" -> radioGroupGoal.check(R.id.radioMaintain)
            "Gain" -> radioGroupGoal.check(R.id.radioGain)
        }

        if (savedProfile.currentWeight > 0) {
            editCurrentWeight.setText(savedProfile.currentWeight.toString())
        }

        if (savedProfile.targetWeight > 0) {
            editTargetWeight.setText(savedProfile.targetWeight.toString())
        }

        editDietaryRequirements.setText(savedProfile.dietaryRequirements.joinToString(", "))
        editFoodPreferences.setText(savedProfile.foodPreferences.joinToString(", "))

        availableIngredients.clear()
        availableIngredients.addAll(savedProfile.availableIngredients)

        updateIngredientDisplay()
    }
}