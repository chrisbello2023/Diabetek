package com.faltenreich.diaguard.ui.list.viewholder;

import android.view.View;
import android.widget.TextView;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.ui.list.item.ListItemHourValue;
import com.faltenreich.diaguard.data.Daytime;
import com.faltenreich.diaguard.data.TimeInterval;
import com.faltenreich.diaguard.ui.view.LocalizedNumberEditText;
import com.faltenreich.diaguard.util.Helper;

import org.joda.time.DateTimeConstants;

import butterknife.BindView;

/**
 * Created by Faltenreich on 03.09.2016.
 */
public class FactorViewHolder extends BaseViewHolder<ListItemHourValue> {

    @BindView(R.id.list_item_time_text)
    protected TextView time;

    @BindView(R.id.list_item_time_value)
    public LocalizedNumberEditText value;

    public FactorViewHolder(View view) {
        super(view);
    }

    @Override
    protected void bindData() {
        final ListItemHourValue preference = getListItem();

        if (preference.getInterval() == TimeInterval.CONSTANT) {
            time.setVisibility(View.GONE);
        } else {
            time.setVisibility(View.VISIBLE);
            int hourOfDay = preference.getHourOfDay();
            int target = (preference.getHourOfDay() + preference.getInterval().interval) % DateTimeConstants.HOURS_PER_DAY;
            if (preference.getInterval() == TimeInterval.EVERY_SIX_HOURS) {
                String timeOfDay = getContext().getString(Daytime.toDayTime(hourOfDay).textResourceId);
                time.setText(String.format("%s (%02d - %02d:00)", timeOfDay, hourOfDay, target));
            } else {
                time.setText(String.format("%02d:00 - %02d:00", hourOfDay, target));
            }
        }

        value.setText(preference.getValue() >= 0 ? Helper.parseFloat(preference.getValue()) : null);
    }
}