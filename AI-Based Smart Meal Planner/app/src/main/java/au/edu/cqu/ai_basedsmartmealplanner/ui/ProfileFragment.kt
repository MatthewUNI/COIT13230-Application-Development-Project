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

    private val profileManager = UserProfileManager()

    private val availableIngredients = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val radioGroupGoal = view.findViewById<RadioGroup>(R.id.radioGroupGoal)

        val editCurrentWeight =
            view.findViewById<TextInputEditText>(R.id.editCurrentWeight)

        val editTargetWeight =
            view.findViewById<TextInputEditText>(R.id.editTargetWeight)

        val editDietaryRequirements =
            view.findViewById<TextInputEditText>(R.id.editDietaryRequirements)

        val editFoodPreferences =
            view.findViewById<TextInputEditText>(R.id.editFoodPreferences)

        val editIngredient =
            view.findViewById<TextInputEditText>(R.id.editIngredient)

        val buttonToggleIngredients =
            view.findViewById<Button>(R.id.buttonToggleIngredients)

        val buttonSaveProfile =
            view.findViewById<Button>(R.id.buttonSaveProfile)

        val ingredientsContainer =
            view.findViewById<LinearLayout>(R.id.ingredientsContainer)

        val textNoIngredients =
            view.findViewById<TextView>(R.id.textNoIngredients)

        val buttonAddIngredient =
            view.findViewById<Button>(R.id.buttonAddIngredient)

        fun updateIngredientDisplay() {

            ingredientsContainer.removeAllViews()

            if (availableIngredients.isEmpty()) {
                ingredientsContainer.visibility = View.GONE
                textNoIngredients.visibility = View.VISIBLE
                buttonToggleIngredients.text =
                    "Ingredients (${availableIngredients.size}) ▼"
                return
            }

            textNoIngredients.visibility = View.GONE

            availableIngredients.forEach { ingredient ->

                val row = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(8, 8, 8, 8)
                }

                val ingredientText = TextView(requireContext()).apply {
                    text = ingredient
                    textSize = 16f

                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
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
                buttonToggleIngredients.text =
                    "Ingredients (${availableIngredients.size}) ▼"
            } else {
                ingredientsContainer.visibility = View.VISIBLE
                buttonToggleIngredients.text =
                    "Ingredients (${availableIngredients.size}) ▲"
            }
        }

        buttonAddIngredient.setOnClickListener {

            val ingredient = editIngredient.text
                .toString()
                .trim()

            if (ingredient.isNotEmpty() &&
                !availableIngredients.contains(ingredient)
            ) {
                availableIngredients.add(ingredient)

                editIngredient.text?.clear()

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

            val currentWeight =
                editCurrentWeight.text.toString().toDoubleOrNull() ?: 0.0

            val targetWeight =
                editTargetWeight.text.toString().toDoubleOrNull() ?: 0.0

            val dietaryRequirements =
                editDietaryRequirements.text.toString()
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

            val foodPreferences =
                editFoodPreferences.text.toString()
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

            if (profileManager.isProfileValid(profile)) {

                profileManager.updateProfile(profile)

                Toast.makeText(
                    requireContext(),
                    "Profile saved",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    requireContext(),
                    "Please complete all required fields",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        updateIngredientDisplay()
    }
}