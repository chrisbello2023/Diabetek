package com.faltenreich.diaguard.feature.log.day;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.databinding.ListItemLogDayBinding;
import com.faltenreich.diaguard.shared.view.recyclerview.viewholder.BaseViewHolder;

import org.joda.time.DateTime;

import butterknife.BindView;

/**
 * Created by Faltenreich on 17.10.2015.
 */
public class LogDayViewHolder extends BaseViewHolder<ListItemLogDayBinding, LogDayListItem> {

    @BindView(R.id.day) TextView day;
    @BindView(R.id.weekday) TextView weekDay;

    public LogDayViewHolder(ViewGroup parent) {
        super(parent, R.layout.list_item_log_day);
    }

    @Override
    protected ListItemLogDayBinding createBinding(View view) {
        return ListItemLogDayBinding.bind(view);
    }

    @Override
    public void onBind(LogDayListItem item) {
        DateTime dateTime = item.getDateTime();

        day.setText(dateTime.toString("dd"));
        weekDay.setText(dateTime.dayOfWeek().getAsShortText());

        // Highlight current day
        boolean isToday = dateTime.withTimeAtStartOfDay().isEqual(DateTime.now().withTimeAtStartOfDay());
        int textColor =  isToday ?
                ContextCompat.getColor(getContext(), R.color.green) :
                ContextCompat.getColor(getContext(), R.color.gray_dark);
        day.setTextColor(textColor);
        weekDay.setTextColor(textColor);
    }
}
