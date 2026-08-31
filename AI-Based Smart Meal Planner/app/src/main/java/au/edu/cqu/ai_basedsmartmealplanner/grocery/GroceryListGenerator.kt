package au.edu.cqu.ai_basedsmartmealplanner.grocery

import au.edu.cqu.ai_basedsmartmealplanner.model.GroceryItem
import au.edu.cqu.ai_basedsmartmealplanner.model.GroceryList

class GroceryListGenerator {

    fun generateGroceryList(items: List<GroceryItem>): GroceryList {
        return GroceryList(items = items)
    }
}