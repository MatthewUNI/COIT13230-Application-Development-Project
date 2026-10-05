package au.edu.cqu.ai_basedsmartmealplanner.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import au.edu.cqu.ai_basedsmartmealplanner.R
import au.edu.cqu.ai_basedsmartmealplanner.ai.MealPlannerViewModel
import au.edu.cqu.ai_basedsmartmealplanner.ai.MealUiState
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.launch

class GroceryListFragment : Fragment(R.layout.fragment_grocery_list) {

    private lateinit var viewModel: MealPlannerViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MealPlannerViewModel::class.java]

        val tvNoGrocery = view.findViewById<TextView>(R.id.textNoGroceryList)
        val groceryContainer = view.findViewById<LinearLayout>(R.id.groceryListContainer)
        val btnGenerateGrocery = view.findViewById<Button>(R.id.buttonGenerateGroceryList)
        val toggleTimeframe = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleTimeframe)

        if (viewModel.uiState.value !is MealUiState.Success) {
            tvNoGrocery.visibility = View.VISIBLE
            groceryContainer.visibility = View.GONE
            btnGenerateGrocery.visibility = View.VISIBLE
            toggleTimeframe.visibility = View.GONE
        } else {
            toggleTimeframe.visibility = View.VISIBLE
        }

        toggleTimeframe.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnTodayOnly -> viewModel.updateGroceryListTimeframe(isTodayOnly = true)
                    R.id.btnWholeWeek -> viewModel.updateGroceryListTimeframe(isTodayOnly = false)
                }
            }
        }

        btnGenerateGrocery.setOnClickListener {
            val currentState = viewModel.uiState.value
            if (currentState is MealUiState.Success) {
                viewModel.generateGroceryList()
                Toast.makeText(requireContext(), "Grocery list refreshed!", Toast.LENGTH_SHORT).show()
                toggleTimeframe.visibility = View.VISIBLE
            } else {
                Toast.makeText(requireContext(), "Please generate or select a meal plan first.", Toast.LENGTH_SHORT).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.groceryList.collect { groceryList ->
                    if (groceryList == null || groceryList.items.isEmpty()) {
                        tvNoGrocery.visibility = View.VISIBLE
                        groceryContainer.visibility = View.GONE

                        if (viewModel.uiState.value is MealUiState.Success) {
                            tvNoGrocery.text = "You have all ingredients! No shopping needed."
                            btnGenerateGrocery.visibility = View.GONE
                        } else {
                            tvNoGrocery.text = "No grocery list available."
                            btnGenerateGrocery.visibility = View.VISIBLE
                        }
                    } else {
                        tvNoGrocery.visibility = View.GONE
                        groceryContainer.visibility = View.VISIBLE
                        btnGenerateGrocery.visibility = View.GONE
                        groceryContainer.removeAllViews()

                        // 1. Group the items by their assigned category
                        val groupedItems = groceryList.items.groupBy { it.category }

                        // 2. Define the ideal supermarket flow
                        val categoryOrder = listOf("Produce", "Protein", "Pantry")

                        // 3. Loop through the categories and build the UI sections
                        categoryOrder.forEach { categoryName ->
                            val itemsInCategory = groupedItems[categoryName]

                            if (!itemsInCategory.isNullOrEmpty()) {

                                // Create a bold, colored header for the category (e.g., "PRODUCE")
                                val header = TextView(requireContext()).apply {
                                    text = categoryName.uppercase()
                                    textSize = 16f
                                    setTypeface(null, Typeface.BOLD)
                                    setTextColor(Color.parseColor("#588064")) // Uses your primary_green hex
                                    setPadding(0, 32, 0, 8) // Adds spacing above the header
                                }
                                groceryContainer.addView(header)

                                // Add the checkboxes for every item inside this category
                                itemsInCategory.forEach { item ->
                                    val checkBox = CheckBox(requireContext()).apply {
                                        text = item.name
                                        textSize = 16f
                                        isChecked = item.isPurchased
                                        setPadding(16, 12, 16, 12)
                                        setOnCheckedChangeListener { _, isChecked ->
                                            viewModel.updateGroceryItemPurchased(
                                                itemName = item.name,
                                                isPurchased = isChecked
                                            )
                                        }
                                    }
                                    groceryContainer.addView(checkBox)
                                }
                            }
                        }

                        // Fallback: Catch any items that didn't match the standard 3 categories
                        val otherCategories = groupedItems.keys.filterNot { it in categoryOrder }
                        otherCategories.forEach { categoryName ->
                            val itemsInCategory = groupedItems[categoryName]
                            if (!itemsInCategory.isNullOrEmpty()) {
                                val header = TextView(requireContext()).apply {
                                    text = categoryName.uppercase()
                                    textSize = 16f
                                    setTypeface(null, Typeface.BOLD)
                                    setTextColor(Color.parseColor("#588064"))
                                    setPadding(0, 32, 0, 8)
                                }
                                groceryContainer.addView(header)

                                itemsInCategory.forEach { item ->
                                    val checkBox = CheckBox(requireContext()).apply {
                                        text = item.name
                                        textSize = 16f
                                        isChecked = item.isPurchased
                                        setPadding(16, 12, 16, 12)
                                        setOnCheckedChangeListener { _, isChecked ->
                                            viewModel.updateGroceryItemPurchased(
                                                itemName = item.name,
                                                isPurchased = isChecked
                                            )
                                        }
                                    }
                                    groceryContainer.addView(checkBox)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}