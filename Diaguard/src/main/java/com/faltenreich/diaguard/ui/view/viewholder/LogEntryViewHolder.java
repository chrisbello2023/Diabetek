package com.faltenreich.diaguard.ui.view.viewholder;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.adapter.list.ListItemEntry;
import com.faltenreich.diaguard.data.PreferenceHelper;
import com.faltenreich.diaguard.data.entity.BloodSugar;
import com.faltenreich.diaguard.data.entity.Entry;
import com.faltenreich.diaguard.data.entity.Insulin;
import com.faltenreich.diaguard.data.entity.Measurement;
import com.faltenreich.diaguard.ui.activity.BaseActivity;
import com.faltenreich.diaguard.ui.activity.EntryActivity;
import com.faltenreich.diaguard.ui.view.TintImageView;

import butterknife.BindView;

/**
 * Created by Faltenreich on 17.10.2015.
 */
public class LogEntryViewHolder extends BaseViewHolder<ListItemEntry> implements View.OnClickListener {

    @BindView(R.id.cardview) CardView cardView;
    @BindView(R.id.time) TextView time;
    @BindView(R.id.note) TextView note;
    @BindView(R.id.measurements) public ViewGroup measurements;

    public LogEntryViewHolder(View view) {
        super(view);
    }

    @Override
    public void bindData() {
        cardView.setOnClickListener(this);

        Entry entry = getListItem().getEntry();

        time.setText(entry.getDate().toString("HH:mm"));

        if (entry.getNote() != null && entry.getNote().length() > 0) {
            note.setVisibility(View.VISIBLE);
            note.setText(entry.getNote());
        } else {
            note.setVisibility(View.GONE);
        }

        LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (Measurement measurement : entry.getMeasurementCache()) {
            Measurement.Category category = measurement.getCategory();
            View viewMeasurement = inflate.inflate(R.layout.list_item_log_measurement, measurements, false);
            TintImageView categoryImage = (TintImageView) viewMeasurement.findViewById(R.id.image);
            int imageResourceId = PreferenceHelper.getInstance().getCategoryImageResourceId(category);
            categoryImage.setImageDrawable(ContextCompat.getDrawable(getContext(), imageResourceId));
            categoryImage.setTintColor(ContextCompat.getColor(getContext(), R.color.gray_dark));
            TextView value = (TextView) viewMeasurement.findViewById(R.id.value);

            switch (category) {
                case INSULIN:
                    value.setText(((Insulin) measurement).toStringDetail());
                    break;
                case BLOODSUGAR:
                    BloodSugar bloodSugar = (BloodSugar) measurement;
                    if (PreferenceHelper.getInstance().limitsAreHighlighted()) {
                        int backgroundColor = ContextCompat.getColor(getContext(), R.color.green);
                        if (bloodSugar.getMgDl() > PreferenceHelper.getInstance().getLimitHyperglycemia()) {
                            backgroundColor = ContextCompat.getColor(getContext(), R.color.red);
                        } else if (bloodSugar.getMgDl() < PreferenceHelper.getInstance().getLimitHypoglycemia()) {
                            backgroundColor = ContextCompat.getColor(getContext(), R.color.blue);
                        }
                        categoryImage.setTintColor(backgroundColor);
                    }
                default:
                    value.setText(String.format("%s %s",
                            measurement.toString(),
                            PreferenceHelper.getInstance().getUnitAcronym(category)));
            }

            measurements.addView(viewMeasurement);
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(getContext(), EntryActivity.class);
        intent.putExtra(EntryActivity.EXTRA_ENTRY, getListItem().getEntry().getId());

        if (getContext() instanceof BaseActivity) {
            BaseActivity activity = (BaseActivity)getContext();
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            activity,
                            view,
                            "transitionEntry");
            activity.startActivity(intent, options);

        } else {
            getContext().startActivity(intent);
        }
    }
}
