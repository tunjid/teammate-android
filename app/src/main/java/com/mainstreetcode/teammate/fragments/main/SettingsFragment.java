package com.mainstreetcode.teammate.fragments.main;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.activities.MainActivity;
import com.mainstreetcode.teammate.adapters.SettingsAdapter;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.SettingsItem;
import com.mainstreetcode.teammate.util.ScrollManager;

import java.util.Arrays;
import java.util.List;

public final class SettingsFragment extends MainActivityFragment
        implements SettingsAdapter.SettingsAdapterListener {

    private static final List<SettingsItem> items = Arrays.asList(
            new SettingsItem(R.string.show_on_boarding, R.drawable.ic_teach_24dp),
            new SettingsItem(R.string.sign_out, R.drawable.ic_logout_white_24dp)
    );

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.settings_list))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new SettingsAdapter(items, this))
                .withLinearLayoutManager()
                .build();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_settings, menu);
    }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setToolbarTitle(getString(R.string.settings));
    }

    @Override
    public boolean showsFab() { return false; }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.action_delete_account)
            return super.onOptionsItemSelected(item);

        new AlertDialog.Builder(requireContext()).setTitle(getString(R.string.delete_user_prompt))
                .setMessage(R.string.delete_user_prompt_body)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteAccount())
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .show();

        return true;
    }

    private void deleteAccount() {
        disposables.add(userViewModel.deleteAccount().subscribe(deleted -> {
            Activity activity = requireActivity();
            MainActivity.startRegistrationActivity(activity);
            activity.finish();
        }, defaultErrorHandler));
    }

    @Override
    public void onSettingsItemClicked(SettingsItem item) {
        switch (item.getStringRes()) {
            case R.string.sign_out:
                signOut();
                break;
            case R.string.show_on_boarding:
                prefsViewModel.setOnBoarded(false);
                showFragment(FeedFragment.newInstance());
                break;
            case R.string.my_profile:
                showFragment(UserEditFragment.newInstance(userViewModel.getCurrentUser()));
                break;
        }
    }
}
