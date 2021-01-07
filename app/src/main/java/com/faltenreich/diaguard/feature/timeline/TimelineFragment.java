package com.faltenreich.diaguard.feature.timeline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.databinding.FragmentTimelineBinding;
import com.faltenreich.diaguard.feature.navigation.ToolbarProperties;
import com.faltenreich.diaguard.feature.preference.data.PreferenceStore;
import com.faltenreich.diaguard.shared.event.data.EntryAddedEvent;
import com.faltenreich.diaguard.shared.event.data.EntryDeletedEvent;
import com.faltenreich.diaguard.shared.event.data.EntryUpdatedEvent;
import com.faltenreich.diaguard.shared.event.file.BackupImportedEvent;
import com.faltenreich.diaguard.shared.event.preference.CategoryPreferenceChangedEvent;
import com.faltenreich.diaguard.shared.event.preference.UnitChangedEvent;
import com.faltenreich.diaguard.shared.view.ViewUtils;
import com.faltenreich.diaguard.shared.view.fragment.DateFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class TimelineFragment extends DateFragment<FragmentTimelineBinding> implements ViewPager.OnPageChangeListener {

    private ViewPager viewPager;
    private TimelinePagerAdapter adapter;
    private int scrollOffset;

    public TimelineFragment() {
        super(R.layout.fragment_timeline);
    }

    @Override
    protected FragmentTimelineBinding createBinding(LayoutInflater layoutInflater) {
        return FragmentTimelineBinding.inflate(layoutInflater);
    }

    @Override
    public ToolbarProperties getToolbarProperties() {
        boolean isLargeTitle = ViewUtils.isLandscape(getActivity()) || ViewUtils.isLargeScreen(getActivity());
        String weekDay = isLargeTitle ?
            getDay().dayOfWeek().getAsText() :
            getDay().dayOfWeek().getAsShortText();
        String date = DateTimeFormat.mediumDate().print(getDay());
        String title = String.format("%s, %s", weekDay, date);
        return new ToolbarProperties.Builder()
            .setTitle(title)
            .setMenu(R.menu.timeline)
            .build();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews();
        initLayout();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_today) {
            goToDay(DateTime.now());
            return true;
        } else if (itemId == R.id.action_style) {
            openDialogForChartStyle();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void bindViews() {
        viewPager = getBinding().viewPager;
    }

    private void initLayout() {
        adapter = new TimelinePagerAdapter(
            getChildFragmentManager(),
            DateTime.now(),
            (view, scrollX, scrollY, oldScrollX, oldScrollY) -> scrollOffset = scrollY
        );
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(adapter.getMiddle(), false);
        viewPager.addOnPageChangeListener(this);
        // Prevent destroying offscreen fragments that occur on fast scrolling
        viewPager.setOffscreenPageLimit(2);
    }

    private void openDialogForChartStyle() {
        if (getContext() != null) {
            TimelineStyle[] styles = TimelineStyle.values();
            String[] titles = new String[styles.length];
            for (int index = 0; index < styles.length; index++) {
                titles[index] = getString(styles[index].getTitleRes());
            }
            TimelineStyle currentStyle = PreferenceStore.getInstance().getTimelineStyle();
            new AlertDialog.Builder(getContext())
                .setTitle(R.string.chart_style)
                .setSingleChoiceItems(titles, currentStyle.getStableId(), null)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {})
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    TimelineStyle style = styles[position];
                    PreferenceStore.getInstance().setTimelineStyle(style);
                    goToDay(getDay());
                })
            .create()
            .show();
        }
    }

    @Override
    protected void goToDay(DateTime day) {
        super.goToDay(day);
        adapter.setDay(day);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position != adapter.getMiddle()) {
            TimelineDayFragment fragment = adapter.getFragment(position);
            DateTime day = fragment.getDay();
            if (day != null) {
                setDay(day);
                updateLabels();
                fragment.scrollTo(scrollOffset);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            int currentItem = viewPager.getCurrentItem();
            int targetItem = adapter.getMiddle();

            adapter.getFragment(currentItem).invalidateLayout();

            if (currentItem != targetItem) {
                switch (currentItem) {
                    case 0:
                        adapter.previousDay();
                        break;
                    case 2:
                        adapter.nextDay();
                        break;
                }
                viewPager.setCurrentItem(targetItem, false);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EntryAddedEvent event) {
        if (isAdded()) {
            goToDay(getDay());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EntryDeletedEvent event) {
        super.onEvent(event);
        if (isAdded()) {
            goToDay(getDay());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EntryUpdatedEvent event) {
        if (isAdded()) {
            goToDay(getDay());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UnitChangedEvent event) {
        if (isAdded()) {
            goToDay(getDay());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BackupImportedEvent event) {
        if (isAdded()) {
            goToDay(getDay());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CategoryPreferenceChangedEvent event) {
        if (isAdded()) {
            adapter.reset();
        }
    }
}
