package com.faltenreich.diaguard.ui.list.item;

import com.faltenreich.diaguard.data.entity.Food;
import com.faltenreich.diaguard.data.entity.FoodEaten;

/**
 * Created by Filip on 10.07.2015.
 */
public class ListItemFood extends ListItem {

    private Food food;
    private FoodEaten foodEaten;

    public ListItemFood(FoodEaten foodEaten) {
        this.food = foodEaten.getFood();
        this.foodEaten = foodEaten;
    }

    public ListItemFood(Food food) {
        this.food = food;
        this.foodEaten = null;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public FoodEaten getFoodEaten() {
        return foodEaten;
    }

    public void setFoodEaten(FoodEaten foodEaten) {
        this.foodEaten = foodEaten;
    }

    @Override
    public boolean equals(Object item) {
        return item != null && item instanceof ListItemFood && food.equals(((ListItemFood) item).getFood());
    }

    @Override
    public String toString() {
        return food.toString();
    }
}