package au.edu.cqu.ai_basedsmartmealplanner.ui

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
import kotlinx.coroutines.launch

class GroceryListFragment : Fragment(R.layout.fragment_grocery_list) {

    private lateinit var viewModel: MealPlannerViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Share the same activity-scoped ViewModel across all fragments
        viewModel = ViewModelProvider(requireActivity())[MealPlannerViewModel::class.java]

        val tvNoGrocery = view.findViewById<TextView>(R.id.textNoGroceryList)
        val groceryContainer = view.findViewById<LinearLayout>(R.id.groceryListContainer)
        val btnGenerateGrocery = view.findViewById<Button>(R.id.buttonGenerateGroceryList)

        // Generate or refresh the grocery list on demand
        btnGenerateGrocery.setOnClickListener {
            val currentState = viewModel.uiState.value
            if (currentState is MealUiState.Success) {
                viewModel.generateGroceryList()
                Toast.makeText(requireContext(), "Grocery list refreshed!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please generate or select a meal plan first.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Observe reactive StateFlow updates from MealPlannerViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.groceryList.collect { groceryList ->
                    if (groceryList == null || groceryList.items.isEmpty()) {
                        tvNoGrocery.visibility = View.VISIBLE
                        groceryContainer.visibility = View.GONE
                    } else {
                        tvNoGrocery.visibility = View.GONE
                        groceryContainer.visibility = View.VISIBLE
                        groceryContainer.removeAllViews()

                        groceryList.items.forEach { item ->
                            val checkBox = CheckBox(requireContext()).apply {
                                text = "${item.name} (${item.quantity})"
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