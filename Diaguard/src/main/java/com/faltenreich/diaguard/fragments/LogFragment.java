package com.faltenreich.diaguard.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.faltenreich.diaguard.R;

public class LogFragment extends Fragment {

    private static View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // To multiple instantiation of the same fragment
        // Needed for fragments in fragments
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_log, container, false);
        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }
        setHasOptionsMenu(true);

        if (view.findViewById(R.id.entry_detail) != null) {
            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            FragmentActivity activity = getActivity();
            FragmentManager fragmentManager = getChildFragmentManager();
            EntryListFragment fragment = (EntryListFragment) fragmentManager.findFragmentById(R.id.entry_list);
            fragment.setActivateOnItemClick(true);
        }

        return view;
    }
}