package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.SettingsAdapter;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.SettingsItem;
import com.mainstreetcode.teammate.util.ScrollManager;

import java.util.Arrays;
import java.util.List;

public final class SettingsFragment extends MainActivityFragment
        implements SettingsAdapter.SettingsAdapterListener {

    private static final List<SettingsItem> items = Arrays.asList(
            new SettingsItem(R.string.my_profile,R.drawable.ic_account_white_24dp),
            new SettingsItem(R.string.sign_out, R.drawable.ic_logout_white_24dp)
    );

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.settings_list))
                .withLinearLayoutManager()
                .withAdapter(new SettingsAdapter(items, this))
                .build();

        return rootView;
    }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setToolbarTitle(getString(R.string.settings));
    }

    @Override
    public boolean showsFab() {
        return false;
    }

    @Override
    public void onSettingsItemClicked(SettingsItem item) {
        switch (item.getStringRes()) {
            case R.string.sign_out:
                signOut();
                break;
            case R.string.my_profile:
                showFragment(UserEditFragment.newInstance(userViewModel.getCurrentUser()));
                break;
        }

    }
}
