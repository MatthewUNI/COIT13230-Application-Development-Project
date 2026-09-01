package au.edu.cqu.ai_basedsmartmealplanner.grocery

import au.edu.cqu.ai_basedsmartmealplanner.model.GroceryItem
import au.edu.cqu.ai_basedsmartmealplanner.model.GroceryList

class GroceryListGenerator {

    fun generateGroceryList(items: List<GroceryItem>): GroceryList {

        val combinedItems = items
            .groupBy { "${it.name.lowercase()}|${it.unit.lowercase()}" }
            .map { (_, groupedItems) ->
                val firstItem = groupedItems.first()

                GroceryItem(
                    name = firstItem.name,
                    quantity = groupedItems.sumOf { it.quantity },
                    unit = firstItem.unit,
                    category = firstItem.category,
                    isPurchased = false
                )
            }

        return GroceryList(items = combinedItems)
    }
}