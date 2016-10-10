package com.faltenreich.diaguard.data.entity;

import com.j256.ormlite.field.DatabaseField;

import org.joda.time.format.DateTimeFormat;

/**
 * Created by Faltenreich on 11.09.2016.
 */
public class FoodEaten extends BaseEntity {

    public class Column extends BaseEntity.Column {
        public static final String AMOUNT_IN_GRAMS = "amountInGrams";
        public static final String MEAL = "meal";
        public static final String FOOD = "food";
    }

    @DatabaseField(columnName = Column.AMOUNT_IN_GRAMS)
    private float amountInGrams;

    @DatabaseField(columnName = Column.MEAL, foreign = true, foreignAutoRefresh = true)
    private Meal meal;

    @DatabaseField(columnName = Column.FOOD, foreign = true, foreignAutoRefresh = true)
    private Food food;

    public float getAmountInGrams() {
        return amountInGrams;
    }

    public void setAmountInGrams(float amountInGrams) {
        this.amountInGrams = amountInGrams;
    }

    public Meal getMeal() {
        return meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public float getCarbohydrates() {
        return getAmountInGrams() * getFood().getCarbohydrates() / 100;
    }

    @Override
    public String toString() {
        return String.format("%s: %f grams (updated: %s)", food.getName(), amountInGrams, DateTimeFormat.mediumDateTime().print(getUpdatedAt()));
    }
}