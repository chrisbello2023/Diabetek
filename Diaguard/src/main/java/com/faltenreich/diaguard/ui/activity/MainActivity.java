package com.faltenreich.diaguard.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;

import com.faltenreich.diaguard.BuildConfig;
import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.data.PreferenceHelper;
import com.faltenreich.diaguard.ui.fragment.BaseFragment;
import com.faltenreich.diaguard.ui.fragment.CalculatorFragment;
import com.faltenreich.diaguard.ui.fragment.CalculatorMissingFragment;
import com.faltenreich.diaguard.ui.fragment.ChangelogFragment;
import com.faltenreich.diaguard.ui.fragment.ChartFragment;
import com.faltenreich.diaguard.ui.fragment.ExportFragment;
import com.faltenreich.diaguard.ui.fragment.LogFragment;
import com.faltenreich.diaguard.ui.fragment.MainFragment;
import com.faltenreich.diaguard.ui.fragment.StatisticsFragment;
import com.faltenreich.diaguard.ui.view.MainButtonAction;
import com.faltenreich.diaguard.util.SystemUtils;

import butterknife.BindView;

public class MainActivity extends BaseActivity {

    public enum MainFragmentType {
        HOME(com.faltenreich.diaguard.ui.fragment.MainFragment.class, 0),
        TIMELINE(ChartFragment.class, 1),
        LOG(LogFragment.class, 2),
        STATISTICS(StatisticsFragment.class, 5),
        EXPORT(ExportFragment.class, 6);

        public Class<? extends BaseFragment> fragmentClass;
        public int position;

        MainFragmentType(Class<? extends BaseFragment> fragmentClass, int position) {
            this.fragmentClass = fragmentClass;
            this.position = position;
        }

        public static MainFragmentType valueOf(Class<? extends BaseFragment> fragmentClass) {
            for (MainFragmentType mainFragmentType : MainFragmentType.values()) {
                if (mainFragmentType.fragmentClass == fragmentClass) {
                    return mainFragmentType;
                }
            }
            return null;
        }

        public static MainFragmentType valueOf(int position) {
            for (MainFragmentType mainFragmentType : MainFragmentType.values()) {
                if (mainFragmentType.position == position) {
                    return mainFragmentType;
                }
            }
            return null;
        }
    }

    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.navigation_drawer) NavigationView drawer;
    @BindView(R.id.fab) FloatingActionButton fab;

    private ActionBarDrawerToggle drawerToggle;

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        initialize();
        showChangelog();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                startActivity(new Intent(MainActivity.this, EntrySearchActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initialize() {
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
        drawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Delay as workaround to smooth transition
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawerLayout.closeDrawers();
                    }
                }, 150);
                return showFragment(menuItem);
            }
        });
        drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerToggle.isDrawerIndicatorEnabled()) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                } else {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    getSupportFragmentManager().popBackStackImmediate();
                }
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                drawerToggle.setDrawerIndicatorEnabled(getSupportFragmentManager().getBackStackEntryCount() == 0);
                invalidateLayout();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = getVisibleFragment();
                if (fragment instanceof MainButtonAction) {
                    ((MainButtonAction) fragment).onMainButtonClick(fab);
                }
            }
        });

        // Setup start fragment
        int startScreen = PreferenceHelper.getInstance().getStartScreen();
        MenuItem menuItem = drawer.getMenu().getItem(startScreen);
        showFragment(menuItem);
    }

    private Fragment getVisibleFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.container);
    }

    private void invalidateLayout() {
        Fragment fragment = getVisibleFragment();
        if (fragment != null && fragment instanceof BaseFragment) {
            BaseFragment baseFragment = (BaseFragment) fragment;
            setTitle(baseFragment.getTitle());
            MainFragmentType mainFragmentType = MainFragmentType.valueOf(baseFragment.getClass());
            select(mainFragmentType);

            fab.setVisibility(fragment instanceof MainButtonAction ? View.VISIBLE : View.GONE);
        }
    }

    public void showFragment(@IdRes int itemId) {
        MenuItem menuItem = drawer.getMenu().findItem(itemId);
        showFragment(menuItem);
    }

    private boolean showFragment(MenuItem menuItem) {
        if (menuItem != null) {
            switch (menuItem.getItemId()) {
                case R.id.nav_home:
                    showFragment(new MainFragment(), menuItem, false);
                    break;
                case R.id.nav_timeline:
                    showFragment(new ChartFragment(), menuItem, false);
                    break;
                case R.id.nav_log:
                    showFragment(new LogFragment(), menuItem, false);
                    break;
                case R.id.nav_calculator:
                    if (BuildConfig.isCalculatorEnabled) {
                        showFragment(new CalculatorFragment(), menuItem, false);
                    } else {
                        explainMissingCalculator();
                    }
                    break;
                case R.id.nav_food_database:
                    startActivity(new Intent(MainActivity.this, FoodSearchActivity.class));
                    break;
                case R.id.nav_statistics:
                    showFragment(new StatisticsFragment(), menuItem, true);
                    break;
                case R.id.nav_export:
                    showFragment(new ExportFragment(), menuItem, true);
                    break;
                case R.id.nav_settings:
                    startActivity(new Intent(MainActivity.this, PreferenceActivity.class));
                    break;
                default:
                    showFragment(new MainFragment(), menuItem, false);
                    break;
            }
        }
        return true;
    }

    public void showFragment(BaseFragment fragment, MenuItem menuItem, boolean addToBackStack) {
        Fragment activeFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        boolean isActive = activeFragment != null && activeFragment.getClass() == fragment.getClass();
        if (!isActive) {
            SystemUtils.hideKeyboard(this);

            select(menuItem);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            String tag = fragment.getClass().getSimpleName();
            getSupportFragmentManager().popBackStackImmediate();
            if (addToBackStack) {
                transaction.add(R.id.container, fragment, tag);
                transaction.addToBackStack(tag);
            } else {
                transaction.replace(R.id.container, fragment, tag);
            }
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();
            setTitle(fragment.getTitle());
        }
    }

    private void explainMissingCalculator() {
        new CalculatorMissingFragment().show(getSupportFragmentManager(), null);
    }

    private void select(MainFragmentType mainFragmentType) {
        if (mainFragmentType != null) {
            int position = mainFragmentType.position;
            if (position < drawer.getMenu().size()) {
                MenuItem menuItem = drawer.getMenu().getItem(position);
                select(menuItem);
            }
        }
    }

    private void select(MenuItem menuItem) {
        if (menuItem != null) {
            // First uncheck all, then check current Fragment
            for (int index = 0; index < drawer.getMenu().size(); index++) {
                drawer.getMenu().getItem(index).setChecked(false);
            }
            menuItem.setChecked(true);
        }
    }

    // Show changelog only for updated versions
    private void showChangelog() {

        int oldVersionCode = PreferenceHelper.getInstance().getVersionCode();
        int currentVersionCode = SystemUtils.getVersionCode(this);
        boolean isUpdate = oldVersionCode > 0 && oldVersionCode < currentVersionCode;

        if (isUpdate) {
            PreferenceHelper.getInstance().setVersionCode(currentVersionCode);

            String[] changelog = PreferenceHelper.getInstance().getChangelog(this);
            boolean hasChangelog = changelog != null && changelog.length > 0;

            if (hasChangelog) {
                ChangelogFragment fragment = new ChangelogFragment();
                String tag = fragment.getClass().getSimpleName();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.addToBackStack(tag);
                fragment.show(fragmentTransaction, tag);
            }

            if (currentVersionCode == 25) {
                explainMissingCalculator();
            }

        } else if (oldVersionCode == 0) {
            PreferenceHelper.getInstance().setVersionCode(currentVersionCode);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
