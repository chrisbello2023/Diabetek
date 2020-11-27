package com.faltenreich.diaguard.feature.entry.search;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.databinding.FragmentEntrySearchBinding;
import com.faltenreich.diaguard.feature.log.entry.LogEntryListItem;
import com.faltenreich.diaguard.feature.navigation.ToolbarDescribing;
import com.faltenreich.diaguard.feature.navigation.ToolbarProperties;
import com.faltenreich.diaguard.shared.data.async.DataLoader;
import com.faltenreich.diaguard.shared.data.async.DataLoaderListener;
import com.faltenreich.diaguard.shared.data.database.dao.EntryDao;
import com.faltenreich.diaguard.shared.data.database.dao.EntryTagDao;
import com.faltenreich.diaguard.shared.data.database.dao.FoodEatenDao;
import com.faltenreich.diaguard.shared.data.database.dao.TagDao;
import com.faltenreich.diaguard.shared.data.database.entity.Entry;
import com.faltenreich.diaguard.shared.data.database.entity.EntryTag;
import com.faltenreich.diaguard.shared.data.database.entity.FoodEaten;
import com.faltenreich.diaguard.shared.data.database.entity.Meal;
import com.faltenreich.diaguard.shared.data.database.entity.Measurement;
import com.faltenreich.diaguard.shared.data.database.entity.Tag;
import com.faltenreich.diaguard.shared.view.fragment.BaseFragment;
import com.faltenreich.diaguard.shared.view.recyclerview.layoutmanager.SafeLinearLayoutManager;
import com.faltenreich.diaguard.shared.view.search.SearchViewListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class EntrySearchFragment extends BaseFragment<FragmentEntrySearchBinding> implements ToolbarDescribing, SearchViewListener {

    private static final String TAG = EntrySearchFragment.class.getSimpleName();
    private static final int PAGE_SIZE = 25;

    static final String EXTRA_TAG_ID = "tagId";

    private EntrySearchListAdapter listAdapter;
    private long tagId = -1;
    private int currentPage = 0;

    public EntrySearchFragment() {
        super(R.layout.fragment_entry_search);
    }

    @Override
    protected FragmentEntrySearchBinding createBinding(LayoutInflater layoutInflater) {
        return FragmentEntrySearchBinding.inflate(layoutInflater);
    }

    @Override
    public ToolbarProperties getToolbarProperties() {
        return new ToolbarProperties.Builder()
            .setTitle(getContext(), R.string.search)
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
        preFillQuery();
    }

    private void init() {
        if (getActivity() != null && getActivity().getIntent() != null && getActivity().getIntent().getExtras() != null) {
            Bundle arguments = getActivity().getIntent().getExtras();
            tagId = arguments.getLong(EXTRA_TAG_ID, -1);
        }
    }

    private void initLayout() {
        RecyclerView listView = getBinding().listView;
        listView.setLayoutManager(new SafeLinearLayoutManager(getActivity()));
        listAdapter = new EntrySearchListAdapter(getContext(), (tag, view) -> { if (isAdded()) getBinding().searchView.setQuery(tag.getName(), true); });
        listAdapter.setOnEndlessListener(scrollingDown -> { if (scrollingDown) continueSearch(); });
        listView.setAdapter(listAdapter);

        getBinding().searchView.setSearchListener(this);

        invalidateEmptyView();
    }

    private void preFillQuery() {
        if (tagId >= 0) {
            DataLoader.getInstance().load(getContext(), new DataLoaderListener<Tag>() {
                @Override
                public Tag onShouldLoad() {
                    return TagDao.getInstance().getById(tagId);
                }
                @Override
                public void onDidLoad(Tag tag) {
                    if (tag != null) {
                        getBinding().searchView.setQuery(tag.getName(), false);
                        newSearch();
                    }
                }
            });
        } else {
            // Workaround to focus EditText onViewCreated
            new Handler().postDelayed(() -> getBinding().searchView.focusSearchField(), 500);
        }
    }

    private void newSearch() {
        int oldCount = listAdapter.getItemCount();
        if (oldCount > 0) {
            listAdapter.clear();
            listAdapter.notifyItemRangeRemoved(0, oldCount);
        }
        currentPage = 0;

        if (StringUtils.isNotBlank(getBinding().searchView.getQuery())) {
            getBinding().progressIndicator.setVisibility(View.VISIBLE);
            continueSearch();
        }
        invalidateEmptyView();
    }

    private void continueSearch() {
        final String query = getBinding().searchView.getQuery();
        DataLoader.getInstance().load(getContext(), new DataLoaderListener<List<LogEntryListItem>>() {
            @Override
            public List<LogEntryListItem> onShouldLoad() {
                List<LogEntryListItem> listItems = new ArrayList<>();
                List<Entry> entries = EntryDao.getInstance().search(query, currentPage, PAGE_SIZE);
                for (Entry entry : entries) {
                    List<Measurement> measurements = EntryDao.getInstance().getMeasurements(entry);
                    entry.setMeasurementCache(measurements);
                    List<EntryTag> entryTags = EntryTagDao.getInstance().getAll(entry);
                    List<FoodEaten> foodEatenList = new ArrayList<>();
                    for (Measurement measurement : measurements) {
                        if (measurement instanceof Meal) {
                            foodEatenList.addAll(FoodEatenDao.getInstance().getAll((Meal) measurement));
                        }
                    }
                    listItems.add(new LogEntryListItem(entry, entryTags, foodEatenList));
                }
                return listItems;
            }
            @Override
            public void onDidLoad(List<LogEntryListItem> items) {
                String currentQuery = getBinding().searchView.getQuery();
                if (query.equals(currentQuery)) {
                    currentPage++;
                    int oldCount = listAdapter.getItemCount();
                    listAdapter.addItems(items);
                    listAdapter.notifyItemRangeInserted(oldCount, items.size());
                    getBinding().progressIndicator.setVisibility(View.GONE);
                    invalidateEmptyView();
                } else {
                    Log.d(TAG, "Dropping obsolete result for \'" + query + "\' (is now: \'" + currentQuery + "\'");
                }
            }
        });
    }

    private void invalidateEmptyView() {
        TextView emptyLabel = getBinding().emptyLabel;
        emptyLabel.setVisibility(getBinding().progressIndicator.getVisibility() != View.VISIBLE && listAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        emptyLabel.setText(StringUtils.isBlank(getBinding().searchView.getQuery()) ? R.string.search_prompt : R.string.no_results_found);
    }

    @Override
    public void onQueryChanged(String query) {
        newSearch();
    }

    @Override
    public void onQueryClosed() {
        finish();
    }
}
