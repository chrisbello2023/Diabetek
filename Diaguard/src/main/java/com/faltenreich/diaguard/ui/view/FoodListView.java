package com.faltenreich.diaguard.ui.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daasuu.cat.CountAnimationTextView;
import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.adapter.FoodEditableAdapter;
import com.faltenreich.diaguard.adapter.SimpleDividerItemDecoration;
import com.faltenreich.diaguard.data.PreferenceHelper;
import com.faltenreich.diaguard.data.entity.Food;
import com.faltenreich.diaguard.data.entity.FoodEaten;
import com.faltenreich.diaguard.data.entity.Meal;
import com.faltenreich.diaguard.data.entity.Measurement;
import com.faltenreich.diaguard.event.Events;
import com.faltenreich.diaguard.event.ui.FoodEatenRemovedEvent;
import com.faltenreich.diaguard.event.ui.FoodEatenUpdatedEvent;
import com.faltenreich.diaguard.event.ui.FoodSelectedEvent;
import com.faltenreich.diaguard.ui.activity.FoodSearchActivity;
import com.faltenreich.diaguard.ui.fragment.FoodSearchFragment;
import com.faltenreich.diaguard.util.Helper;
import com.faltenreich.diaguard.util.NumberUtils;
import com.faltenreich.diaguard.util.StringUtils;
import com.j256.ormlite.dao.ForeignCollection;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Faltenreich on 13.10.2016.
 */

public class FoodListView extends LinearLayout {

    @BindView(R.id.food_list_value_input) EditText valueInput;
    @BindView(R.id.food_list_value_calculated) CountAnimationTextView valueCalculated;
    @BindView(R.id.food_list_value_sign) TextView valueSign;
    @BindView(R.id.food_list_separator) View separator;
    @BindView(R.id.food_list) RecyclerView foodList;

    private FoodEditableAdapter adapter;

    private Meal meal;

    public FoodListView(Context context) {
        super(context);
        init();
    }

    public FoodListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FoodListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Events.register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Events.unregister(this);
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_food_list, this);
        ButterKnife.bind(this);

        meal = new Meal();

        valueInput.setHint(PreferenceHelper.getInstance().getUnitAcronym(Measurement.Category.MEAL));

        adapter = new FoodEditableAdapter(getContext());
        foodList.setLayoutManager(new LinearLayoutManager(getContext()));
        foodList.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
        foodList.setAdapter(adapter);

        valueCalculated.setCountAnimationListener(new CountAnimationTextView.CountAnimationListener() {
            @Override
            public void onAnimationStart(Object animatedValue) {
            }
            @Override
            public void onAnimationEnd(Object animatedValue) {
                float totalCarbohydrates = adapter.getTotalCarbohydrates();
                float totalMeal = PreferenceHelper.getInstance().formatDefaultToCustomUnit(Measurement.Category.MEAL, totalCarbohydrates);
                valueCalculated.setText(Helper.parseFloat(totalMeal));
            }
        });

        update();
    }

    public void setupWithMeal(Meal meal) {
        this.meal = meal;
        valueInput.setText(meal.getValuesForUI()[0]);
        addItems(meal.getFoodEaten());
        update();
    }

    public boolean isValid() {
        boolean isValid = true;

        String input = valueInput.getText().toString().trim();

        if (StringUtils.isBlank(input) && adapter.getTotalCarbohydrates() == 0) {
            valueInput.setError(getContext().getString(R.string.validator_value_empty));
            isValid = false;
        } else {
            if (!StringUtils.isBlank(input)) {
                isValid = PreferenceHelper.isValueValid(valueInput, Measurement.Category.MEAL);
            }
        }
        return isValid;
    }

    public Meal getMeal() {
        if (isValid()) {
            meal.setValues(valueInput.getText().toString().length() > 0 ?
                    PreferenceHelper.getInstance().formatCustomToDefaultUnit(
                            meal.getCategory(),
                            NumberUtils.parseNumber(valueInput.getText().toString())) : 0);
            meal.setFoodEatenCache(adapter.getItems());
            return meal;
        } else {
            return null;
        }
    }

    private void update() {
        boolean hasFood = adapter.getItemCount() > 0;
        separator.setVisibility(hasFood ? VISIBLE : GONE);

        float oldValue = NumberUtils.parseNumber(valueCalculated.getText().toString());
        float newValue = PreferenceHelper.getInstance().formatDefaultToCustomUnit(Measurement.Category.MEAL, adapter.getTotalCarbohydrates());
        boolean hasFoodEaten = newValue > 0;
        valueCalculated.setVisibility(hasFoodEaten ? VISIBLE : GONE);
        valueSign.setVisibility(hasFoodEaten ? VISIBLE : GONE);

        boolean hasChangedSignificantly = Math.abs(newValue - oldValue) > 5;
        if (hasChangedSignificantly) {
            valueCalculated.setInterpolator(new AccelerateInterpolator()).countAnimation((int) oldValue, (int) newValue);
        } else {
            valueCalculated.setText(Helper.parseFloat(newValue));
        }
    }

    public List<FoodEaten> getItems() {
        return adapter.getItems();
    }

    public void addItem(FoodEaten foodEaten) {
        adapter.addItem(foodEaten);
        adapter.notifyItemInserted(this.adapter.getItemCount() - 1);
        update();
    }

    public void addItem(Food food) {
        FoodEaten foodEaten = new FoodEaten();
        foodEaten.setFood(food);
        foodEaten.setMeal(meal);
        addItem(foodEaten);
    }

    public void addItems(ForeignCollection<FoodEaten> foodEatenList) {
        int oldCount = adapter.getItemCount();
        for (FoodEaten foodEaten : foodEatenList) {
            adapter.addItem(foodEaten);
        }
        adapter.notifyItemRangeInserted(oldCount, adapter.getItemCount());
        update();
    }

    public void removeItem(int position) {
        adapter.removeItem(position);
        adapter.notifyItemRemoved(position);
        update();
    }

    public void updateItem(FoodEaten foodEaten, int position) {
        adapter.updateItem(position, foodEaten);
        adapter.notifyItemChanged(position);
        update();
    }

    public float getTotalCarbohydrates() {
        return adapter.getTotalCarbohydrates();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.food_list_button)
    public void searchForFood() {
        Intent intent = new Intent(getContext(), FoodSearchActivity.class);
        intent.putExtra(FoodSearchFragment.EXTRA_MODE, FoodSearchFragment.Mode.SELECT);
        getContext().startActivity(intent);
    }

    @SuppressWarnings("unused")
    public void onEvent(FoodSelectedEvent event) {
        addItem(event.context);
    }

    @SuppressWarnings("unused")
    public void onEvent(FoodEatenUpdatedEvent event) {
        updateItem(event.context, event.position);
    }

    @SuppressWarnings("unused")
    public void onEvent(FoodEatenRemovedEvent event) {
        removeItem(event.position);
    }
}