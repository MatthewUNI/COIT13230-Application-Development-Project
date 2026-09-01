package au.edu.cqu.ai_basedsmartmealplanner.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import au.edu.cqu.ai_basedsmartmealplanner.R

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val hasMealPlan = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nutritionContainer = view.findViewById<View>(R.id.nutritionContainer)
        val mealsContainer = view.findViewById<View>(R.id.mealsContainer)
        val textNoNutrition = view.findViewById<View>(R.id.textNoNutrition)
        val textNoMeals = view.findViewById<View>(R.id.textNoMeals)

        val buttonViewMealPlan =
            view.findViewById<Button>(R.id.buttonViewMealPlan)

        val buttonViewGroceryList =
            view.findViewById<Button>(R.id.buttonViewGroceryList)

        if (hasMealPlan) {
            nutritionContainer.visibility = View.VISIBLE
            mealsContainer.visibility = View.VISIBLE
            textNoNutrition.visibility = View.GONE
            textNoMeals.visibility = View.GONE
        } else {
            nutritionContainer.visibility = View.GONE
            mealsContainer.visibility = View.GONE
            textNoNutrition.visibility = View.VISIBLE
            textNoMeals.visibility = View.VISIBLE
        }

        buttonViewMealPlan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MealPlanFragment())
                .commit()
        }

        buttonViewGroceryList.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, GroceryListFragment())
                .commit()
        }
    }
}