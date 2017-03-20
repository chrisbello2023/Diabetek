package com.faltenreich.diaguard.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.faltenreich.diaguard.DiaguardApplication;
import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.data.entity.Measurement;
import com.faltenreich.diaguard.ui.view.preferences.CategoryPreference;
import com.faltenreich.diaguard.util.Helper;
import com.faltenreich.diaguard.util.NumberUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by Filip on 04.11.13.
 */

/**
 * Parameters are of Default Value
 * Return Values are of Custom Value
 */
public class PreferenceHelper {

    private static final String TAG = PreferenceHelper.class.getSimpleName();
    private static final String INPUT_QUERIES_SEPARATOR = ";";

    private class Keys {
        static final String CATEGORY_PINNED = "categoryPinned%s";
        static final String ALARM_START_IN_MILLIS = "alarmStartInMillis";
        static final String INTERVAL_FACTOR = "intervalFactor";
        static final String INTERVAL_FACTOR_FOR_HOUR = "intervalFactor%d";
        final static String FACTOR_DEPRECATED = "factor_";
        static final String INTERVAL_CORRECTION = "intervalCorrection";
        static final String INTERVAL_CORRECTION_FOR_HOUR = "intervalCorrection%d";
        final static String CORRECTION_DEPRECATED = "correction_value";
        final static String INPUT_QUERIES = "inputQueries";
        final static String DID_IMPORT_COMMON_FOOD_FOR_LANGUAGE = "didImportCommonFoodForLanguage";
        final static String CHART_STYLE = "chart_style";
    }

    public enum ChartStyle {
        POINT,
        LINE
    }

    private static PreferenceHelper instance;

    public static PreferenceHelper getInstance() {
        if (instance == null) {
            instance = new PreferenceHelper();
        }
        return instance;
    }

    private SharedPreferences sharedPreferences;

    private PreferenceHelper() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(DiaguardApplication.getContext());
    }

    // GENERAL

    public void migrate() {
        migrateFactors();
        migrateCorrection();
    }

    boolean didImportCommonFood(Locale locale) {
        return sharedPreferences.getBoolean(Keys.DID_IMPORT_COMMON_FOOD_FOR_LANGUAGE + locale.getLanguage(), false);
    }

    void  setDidImportCommonFood(Locale locale, boolean didImport) {
        sharedPreferences.edit().putBoolean(Keys.DID_IMPORT_COMMON_FOOD_FOR_LANGUAGE + locale.getLanguage(), didImport).apply();
    }

    public long getAlarmStartInMillis() {
        return sharedPreferences.getLong(Keys.ALARM_START_IN_MILLIS, -1);
    }

    public void setAlarmStartInMillis(long alarmStartInMillis) {
        sharedPreferences.edit().putLong(Keys.ALARM_START_IN_MILLIS, alarmStartInMillis).apply();
    }

    public int getStartScreen() {
        String startScreen = sharedPreferences.getString("startscreen", "0");
        return Integer.parseInt(startScreen);
    }

    public boolean isSoundAllowed() {
        return sharedPreferences.getBoolean("sound", true);
    }

    public boolean isVibrationAllowed() {
        return sharedPreferences.getBoolean("vibration", true);
    }

    public ChartStyle getChartStyle() {
        String preference = sharedPreferences.getString(Keys.CHART_STYLE, null);
        if (!TextUtils.isEmpty(preference)) {
            try {
                int chartStyle = Integer.valueOf(preference);
                ChartStyle[] chartStyles = ChartStyle.values();
                return chartStyle >= 0 && chartStyle < chartStyles.length ? chartStyles[chartStyle] : ChartStyle.POINT;
            } catch (NumberFormatException exception) {
                Log.e(TAG, exception.getMessage());
            }
        } else {
            Log.e(TAG, "Failed to find shared preference for key: " + Keys.CHART_STYLE);
        }
        return ChartStyle.POINT;
    }

    public int[] getExtrema(Measurement.Category category) {
        int resourceIdExtrema = DiaguardApplication.getContext().getResources().getIdentifier(category.name().toLowerCase() +
                "_extrema", "array", DiaguardApplication.getContext().getPackageName());

        if(resourceIdExtrema == 0) {
            throw new Resources.NotFoundException("Resource \"category_extrema\" not found: IntArray with event value extrema");
        }

        return DiaguardApplication.getContext().getResources().getIntArray(resourceIdExtrema);
    }

    public boolean validateEventValue(Measurement.Category category, float value) {
        int[] extrema = getExtrema(category);

        if(extrema.length != 2)
            throw new IllegalStateException("IntArray with event value extrema has to contain two values");

        return value > extrema[0] && value < extrema[1];
    }

    public static boolean isValueValid(TextView textView, Measurement.Category category) {
        boolean isValid = true;
        textView.setError(null);
        try {
            float value = PreferenceHelper.getInstance().formatCustomToDefaultUnit(category, NumberUtils.parseNumber(textView.getText().toString()));
            if (!PreferenceHelper.getInstance().validateEventValue(category, value)) {
                textView.setError(textView.getContext().getString(R.string.validator_value_unrealistic));
                isValid = false;
            }
        } catch (NumberFormatException exception) {
            textView.setError(textView.getContext().getString(R.string.validator_value_number));
            isValid = false;
        }
        return isValid;
    }

    public void setExportNotes(boolean exportNotes) {
        sharedPreferences.edit().putBoolean("export_notes", exportNotes).apply();
    }

    public boolean exportNotes() {
        return sharedPreferences.getBoolean("export_notes", true);
    }

    public void addInputQuery(String query) {
        String inputQueries = getInputQueriesString();
        if (inputQueries.length() > 0) {
            inputQueries = inputQueries + INPUT_QUERIES_SEPARATOR;
        }
        sharedPreferences.edit().putString(Keys.INPUT_QUERIES, inputQueries + query).apply();
    }

    private String getInputQueriesString() {
        return sharedPreferences.getString(Keys.INPUT_QUERIES, "");
    }

    public ArrayList<String> getInputQueries() {
        ArrayList<String> inputQueries = new ArrayList<>();
        for (String inputQuery : getInputQueriesString().split(INPUT_QUERIES_SEPARATOR)) {
            if (!TextUtils.isEmpty(inputQuery)) {
                inputQueries.removeAll(Collections.singleton(inputQuery));
                inputQueries.add(0, inputQuery);
            }
        }
        return inputQueries;
    }

    // BLOOD SUGAR

    public String getValueForKey(String key) {
        return sharedPreferences.getString(key, null);
    }

    public float getTargetValue() {
        return NumberUtils.parseNumber(sharedPreferences.getString("target",
                DiaguardApplication.getContext().getString(R.string.pref_therapy_targets_target_default)));
    }

    public boolean limitsAreHighlighted() {
        return sharedPreferences.getBoolean("targets_highlight", true);
    }

    public float getLimitHyperglycemia() {
        return NumberUtils.parseNumber(sharedPreferences.getString("hyperclycemia",
                DiaguardApplication.getContext().getString(R.string.pref_therapy_targets_hyperclycemia_default)));
    }

    public float getLimitHypoglycemia() {
        return NumberUtils.parseNumber(sharedPreferences.getString("hypoclycemia",
                DiaguardApplication.getContext().getString(R.string.pref_therapy_targets_hypoclycemia_default)));
    }

    // CATEGORIES

    public String getCategoryName(Measurement.Category category) {
        int position = Measurement.Category.valueOf(category.name()).ordinal();
        // TODO: Get resourceId by key
        String[] categories = DiaguardApplication.getContext().getResources().getStringArray(R.array.categories);
        return categories[position];
    }

    public int getCategoryImageResourceId(Measurement.Category category) {
        return DiaguardApplication.getContext().getResources().getIdentifier("ic_category_" + category.name().toLowerCase(),
                "drawable", DiaguardApplication.getContext().getPackageName());
    }

    public int getShowcaseImageResourceId(Measurement.Category category) {
        return DiaguardApplication.getContext().getResources().getIdentifier("ic_showcase_" + category.name().toLowerCase(),
                "drawable", DiaguardApplication.getContext().getPackageName());
    }

    public int getMonthResourceId(DateTime daytime) {
        int monthOfYear = daytime.monthOfYear().get();
        String identifier = String.format("bg_month_%d", monthOfYear - 1);
        return DiaguardApplication.getContext().getResources().getIdentifier(identifier,
                "drawable", DiaguardApplication.getContext().getPackageName());
    }

    public int getMonthSmallResourceId(DateTime daytime) {
        int monthOfYear = daytime.monthOfYear().get();
        String identifier = String.format("bg_month_%d_small", monthOfYear - 1);
        return DiaguardApplication.getContext().getResources().getIdentifier(identifier,
                "drawable", DiaguardApplication.getContext().getPackageName());
    }

    public boolean isCategoryActive(Measurement.Category category) {
        return sharedPreferences.getBoolean(category.name() + CategoryPreference.ACTIVE, true);
    }

    public Measurement.Category[] getActiveCategories() {
        List<Measurement.Category> activeCategories = new ArrayList<Measurement.Category>();
        for(int item = 0; item < Measurement.Category.values().length; item++) {
            if (isCategoryActive(Measurement.Category.values()[item])) {
                activeCategories.add(Measurement.Category.values()[item]);
            }
        }
        return activeCategories.toArray(new Measurement.Category[activeCategories.size()]);
    }

    private String getCategoryPinnedName(Measurement.Category category) {
        return String.format(Keys.CATEGORY_PINNED, category.name());
    }

    public boolean isCategoryPinned(Measurement.Category category) {
        return sharedPreferences.getBoolean(getCategoryPinnedName(category), false);
    }

    public void setCategoryPinned(Measurement.Category category, boolean isPinned) {
        sharedPreferences.edit().putBoolean(getCategoryPinnedName(category), isPinned).apply();
    }

    // UNITS

    public String[] getUnitsNames(Measurement.Category category) {
        String categoryName = category.name().toLowerCase();
        int resourceIdUnits = DiaguardApplication.getContext().getResources().getIdentifier(categoryName +
                "_units", "array", DiaguardApplication.getContext().getPackageName());
        return DiaguardApplication.getContext().getResources().getStringArray(resourceIdUnits);
    }

    public String getUnitName(Measurement.Category category) {
        String sharedPref = sharedPreferences.
                getString("unit_" + category.name().toLowerCase(), "1");
        return getUnitsNames(category)[Arrays.asList(getUnitsValues(category)).indexOf(sharedPref)];
    }

    public String[] getUnitsAcronyms(Measurement.Category category) {
        String categoryName = category.name().toLowerCase();
        int resourceIdUnits = DiaguardApplication.getContext().getResources().getIdentifier(categoryName +
                "_units_acronyms", "array", DiaguardApplication.getContext().getPackageName());
        if(resourceIdUnits == 0)
            return null;
        else
            return DiaguardApplication.getContext().getResources().getStringArray(resourceIdUnits);
    }

    public String getUnitAcronym(Measurement.Category category) {
        String[] acronyms = getUnitsAcronyms(category);
        if(acronyms == null)
            return getUnitName(category);
        else {
            String sharedPref = sharedPreferences.
                    getString("unit_" + category.name().toLowerCase(), "1");
            int indexOfAcronym = Arrays.asList(getUnitsValues(category)).indexOf(sharedPref);
            if(indexOfAcronym < acronyms.length) {
                return acronyms[indexOfAcronym];
            }
            else {
                return getUnitName(category);
            }
        }
    }

    public String getLabelForMealPer100g(Context context) {
        return String.format("%s %s 100 g / ml", getUnitAcronym(Measurement.Category.MEAL), context.getString(R.string.per));
    }

    public String[] getUnitsValues(Measurement.Category category) {
        String categoryName = category.name().toLowerCase();
        int resourceIdUnits = DiaguardApplication.getContext().getResources().getIdentifier(categoryName +
                "_units_values", "array", DiaguardApplication.getContext().getPackageName());
        return DiaguardApplication.getContext().getResources().getStringArray(resourceIdUnits);
    }

    public float getUnitValue(Measurement.Category category) {
        String sharedPref = sharedPreferences.
                getString("unit_" + category.name().toLowerCase(), "1");
        String value = getUnitsValues(category)[Arrays.asList(getUnitsValues(category)).indexOf(sharedPref)];
        return NumberUtils.parseNumber(value);
    }

    public float formatCustomToDefaultUnit(Measurement.Category category, float value) {
        return value / getUnitValue(category);
    }

    public float formatDefaultToCustomUnit(Measurement.Category category, float value) {
        return value * getUnitValue(category);
    }

    public String getMeasurementForUi(Measurement.Category category, float defaultValue) {
        return Helper.parseFloat(formatDefaultToCustomUnit(category, defaultValue));
    }

    // FACTORS

    public TimeInterval getFactorInterval() {
        int position = sharedPreferences.getInt(Keys.INTERVAL_FACTOR, TimeInterval.EVERY_SIX_HOURS.ordinal());
        TimeInterval[] timeIntervals = TimeInterval.values();
        return position >= 0 && position < timeIntervals.length ? timeIntervals[position] : TimeInterval.EVERY_SIX_HOURS;
    }

    public void setFactorInterval(TimeInterval interval) {
        sharedPreferences.edit().putInt(Keys.INTERVAL_FACTOR, interval.ordinal()).apply();
    }

    public float getFactorForHour(int hourOfDay) {
        String key = String.format(Keys.INTERVAL_FACTOR_FOR_HOUR, hourOfDay);
        return sharedPreferences.getFloat(key, -1);
    }

    public void setFactorForHour(int hourOfDay, float factor) {
        String key = String.format(Keys.INTERVAL_FACTOR_FOR_HOUR, hourOfDay);
        sharedPreferences.edit().putFloat(key, factor).apply();
    }

    /**
     * Used to migrate from static to dynamic factors
     */
    private void migrateFactors() {
        if (getFactorForHour(0) < 0) {
            for (Daytime daytime : Daytime.values()) {
                float factor = sharedPreferences.getFloat(Keys.FACTOR_DEPRECATED + daytime.toDeprecatedString(), -1);
                if (factor >= 0) {
                    int step = 0;
                    while (step < Daytime.INTERVAL_LENGTH) {
                        int hourOfDay = (daytime.startingHour + step) % DateTimeConstants.HOURS_PER_DAY;
                        setFactorForHour(hourOfDay, factor);
                        step++;
                    }
                    sharedPreferences.edit().putFloat(Keys.FACTOR_DEPRECATED + daytime, -1).apply();
                }
            }
        }
    }

    // CORRECTION

    public TimeInterval getCorrectionInterval() {
        int position = sharedPreferences.getInt(Keys.INTERVAL_CORRECTION, TimeInterval.CONSTANT.ordinal());
        TimeInterval[] timeIntervals = TimeInterval.values();
        return position >= 0 && position < timeIntervals.length ? timeIntervals[position] : TimeInterval.CONSTANT;
    }

    public void setCorrectionInterval(TimeInterval interval) {
        sharedPreferences.edit().putInt(Keys.INTERVAL_CORRECTION, interval.ordinal()).apply();
    }

    public float getCorrectionForHour(int hourOfDay) {
        String key = String.format(Keys.INTERVAL_CORRECTION_FOR_HOUR, hourOfDay);
        return sharedPreferences.getFloat(key, -1);
    }

    public void setCorrectionForHour(int hourOfDay, float factor) {
        String key = String.format(Keys.INTERVAL_CORRECTION_FOR_HOUR, hourOfDay);
        sharedPreferences.edit().putFloat(key, factor).apply();
    }

    private void migrateCorrection() {
        if (getCorrectionForHour(0) < 0) {
            float oldValue = NumberUtils.parseNumber(sharedPreferences.getString(Keys.CORRECTION_DEPRECATED, "40"));
            int hourOfDay = 0;
            while (hourOfDay < DateTimeConstants.HOURS_PER_DAY) {
                setCorrectionForHour(hourOfDay, oldValue);
                hourOfDay++;
            }
        }
    }
}
