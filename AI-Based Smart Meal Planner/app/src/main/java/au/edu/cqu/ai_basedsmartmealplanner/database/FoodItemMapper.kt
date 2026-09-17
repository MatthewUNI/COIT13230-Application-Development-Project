package au.edu.cqu.ai_basedsmartmealplanner.database

import au.edu.cqu.ai_basedsmartmealplanner.model.FoodItem

fun FoodItem.toEntity(): FoodItemEntity {
    return FoodItemEntity(
        foodItemId = foodItemId,
        name = name,
        quantity = quantity,
        unit = unit,
        afcdFoodId = afcdFoodId
    )
}

fun FoodItemEntity.toFoodItem(): FoodItem {
    return FoodItem(
        foodItemId = foodItemId,
        name = name,
        quantity = quantity,
        unit = unit,
        afcdFoodId = afcdFoodId
    )
}