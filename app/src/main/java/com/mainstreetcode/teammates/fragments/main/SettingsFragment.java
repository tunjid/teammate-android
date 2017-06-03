package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.activities.MainActivity;
import com.mainstreetcode.teammates.adapters.SettingsAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.SettingsItem;

import java.util.Arrays;
import java.util.List;

/**
 * Settings screen
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public final class SettingsFragment extends MainActivityFragment
        implements SettingsAdapter.SettingsAdapterListener {

    private static final List<SettingsItem> items = Arrays.asList(
            new SettingsItem(R.string.join_team),
            new SettingsItem(R.string.sign_out)
    );

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.settings_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new SettingsAdapter(items, this));
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        toggleFab(false);
        if (user != null) {
            setToolbarTitle(getString(R.string.settings));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onSettingsItemClicked(SettingsItem item) {
        switch (item.getStringResorce()) {
            case R.string.sign_out:
                FirebaseAuth.getInstance().signOut();
                MainActivity.startRegistrationActivity(getActivity());
                break;
            case R.string.join_team:
                showFragment(TeamSearchFragment.newInstance());
                break;
        }

    }
}
