package au.edu.cqu.ai_basedsmartmealplanner.database

import au.edu.cqu.ai_basedsmartmealplanner.model.GroceryItem

fun GroceryItem.toEntity(): GroceryItemEntity {
    return GroceryItemEntity(
        name = name,
        quantity = quantity,
        unit = unit,
        category = category,
        isPurchased = isPurchased
    )
}

fun GroceryItemEntity.toGroceryItem(): GroceryItem {
    return GroceryItem(
        name = name,
        quantity = quantity,
        unit = unit,
        category = category,
        isPurchased = isPurchased
    )
}