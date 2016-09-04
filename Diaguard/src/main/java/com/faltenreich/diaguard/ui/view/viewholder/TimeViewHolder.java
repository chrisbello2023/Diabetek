package com.faltenreich.diaguard.ui.view.viewholder;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.adapter.list.ListItemTimePreference;
import com.faltenreich.diaguard.data.PreferenceHelper;
import com.faltenreich.diaguard.data.TimeInterval;
import com.faltenreich.diaguard.data.entity.Measurement;
import com.faltenreich.diaguard.util.Helper;

import org.joda.time.DateTimeConstants;

import butterknife.BindView;

/**
 * Created by Faltenreich on 03.09.2016.
 */
public class TimeViewHolder extends BaseViewHolder<ListItemTimePreference> {

    @BindView(R.id.list_item_time_text)
    protected TextView time;

    @BindView(R.id.list_item_time_value)
    protected EditText value;

    public TimeViewHolder(View view) {
        super(view);
    }

    @Override
    protected void bindData() {
        ListItemTimePreference preference = getListItem();
        if (preference.getInterval() == TimeInterval.CONSTANT) {
            time.setVisibility(View.GONE);
        } else {
            time.setVisibility(View.VISIBLE);
            int hourOfDay = preference.getHourOfDay();
            int target = (preference.getHourOfDay() + preference.getInterval().interval) % DateTimeConstants.HOURS_PER_DAY;
            time.setText(String.format("%02d:00 - %02d:00", hourOfDay, target));
        }
        switch (preference.getType()) {
            case BLOOD_SUGAR:
                value.setText(PreferenceHelper.getInstance().getMeasurementForUi(Measurement.Category.BLOODSUGAR, preference.getValue()));
                break;
            case FACTOR:
                value.setText(Helper.parseFloatWithDigit(preference.getValue()));
                break;
        }
    }
}
