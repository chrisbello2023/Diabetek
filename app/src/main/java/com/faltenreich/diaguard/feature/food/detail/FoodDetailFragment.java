package com.faltenreich.diaguard.feature.food.detail;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.feature.entry.edit.EntryEditActivity;
import com.faltenreich.diaguard.feature.food.FoodActions;
import com.faltenreich.diaguard.feature.food.edit.FoodEditFragment;
import com.faltenreich.diaguard.feature.navigation.Navigation;
import com.faltenreich.diaguard.feature.navigation.ToolbarDescribing;
import com.faltenreich.diaguard.feature.navigation.ToolbarProperties;
import com.faltenreich.diaguard.shared.data.database.dao.FoodDao;
import com.faltenreich.diaguard.shared.data.database.entity.Food;
import com.faltenreich.diaguard.shared.view.fragment.BaseFragment;
import com.google.android.material.tabs.TabLayout;

import butterknife.BindView;

public class FoodDetailFragment extends BaseFragment implements ToolbarDescribing {

    private static final String EXTRA_FOOD_ID = "EXTRA_FOOD_ID";

    public static FoodDetailFragment newInstance(Food food) {
        FoodDetailFragment fragment = new FoodDetailFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(EXTRA_FOOD_ID, food.getId());
        fragment.setArguments(arguments);
        return fragment;
    }

    @BindView(R.id.food_viewpager) ViewPager viewPager;
    @BindView(R.id.food_tablayout) TabLayout tabLayout;

    private Food food;

    public FoodDetailFragment() {
        super(R.layout.fragment_food_detail);
    }

    @Override
    public ToolbarProperties getToolbarProperties() {
        return new ToolbarProperties.Builder()
            .setTitle(food.getName())
            .setMenu(R.menu.food)
            .build();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initLayout();
        update();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        } else if (itemId == R.id.action_delete) {
            FoodActions.deleteFoodIfConfirmed(getContext(), food);
            finish();
            return true;
        } else if (itemId == R.id.action_edit) {
            editFood();
            return true;
        } else if (itemId == R.id.action_eat) {
            eatFood();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        Bundle arguments = requireArguments();
        long foodId = arguments.getLong(EXTRA_FOOD_ID);
        food = FoodDao.getInstance().getById(foodId);
    }

    private void initLayout() {
        viewPager.setAdapter(new FoodDetailViewPagerAdapter(getChildFragmentManager(), getContext(), food));
        tabLayout.setupWithViewPager(viewPager);
    }

    private void update() {
        food = FoodDao.getInstance().getById(food.getId());
        setTitle(food != null ? food.getName() : null);
    }

    private void eatFood() {
        EntryEditActivity.show(getContext(), food);
    }

    private void editFood() {
        openFragment(FoodEditFragment.newInstance(food), Navigation.Operation.REPLACE, true);
    }
}
