package com.faltenreich.diaguard.util;

import android.support.annotation.StringRes;

import com.faltenreich.diaguard.DiaguardApplication;
import com.faltenreich.diaguard.R;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * Created by Faltenreich on 22.03.2016.
 */
public enum TimeSpan {

    WEEK(R.string.week, R.string.day),
    MONTH(R.string.month, R.string.calendarweek),
    YEAR(R.string.year, R.string.month);

    private int intervalStringResId;
    private int subIntervalStringResId;

    TimeSpan(@StringRes int intervalStringResId, @StringRes int subIntervalStringResId) {
        this.intervalStringResId = intervalStringResId;
        this.subIntervalStringResId = subIntervalStringResId;
    }

    public String toIntervalLabel() {
        return DiaguardApplication.getContext().getString(intervalStringResId);
    }

    public String toSubIntervalLabel() {
        return DiaguardApplication.getContext().getString(subIntervalStringResId);
    }

    public Interval getPastInterval(DateTime end) {
        switch (this) {
            case WEEK:
                return new Interval(end.minusWeeks(1), end);
            case MONTH:
                return new Interval(end.minusMonths(1), end);
            case YEAR:
                return new Interval(end.minusYears(1), end);
            default:
                return new Interval(end.minusWeeks(1), end);
        }
    }

    public Interval getUpcomingInterval(DateTime start) {
        switch (this) {
            case WEEK:
                return new Interval(start, start.plusWeeks(1));
            case MONTH:
                return new Interval(start, start.plusMonths(1));
            case YEAR:
                return new Interval(start, start.plusYears(1));
            default:
                return new Interval(start, start.plusWeeks(1));
        }
    }

    public DateTime getNextInterval(DateTime dateTime, int step) {
        switch (this) {
            case WEEK:
                return dateTime.plusDays(step);
            case MONTH:
                return dateTime.plusWeeks(step);
            case YEAR:
                return dateTime.plusMonths(step);
            default:
                return dateTime.plusDays(step);
        }
    }

    public String getLabel(DateTime dateTime) {
        switch (this) {
            case WEEK:
                return DateTimeUtils.toWeekDayShort(dateTime);
            case MONTH:
                return dateTime.toString("w");
            case YEAR:
                return dateTime.toString("MMM");
            default:
                return dateTime.toString();
        }
    }
}