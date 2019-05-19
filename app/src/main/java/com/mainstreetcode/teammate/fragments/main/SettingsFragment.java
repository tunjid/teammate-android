/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.fragments.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.activities.MainActivity;
import com.mainstreetcode.teammate.adapters.SettingsAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.SettingsViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.SettingsItem;
import com.mainstreetcode.teammate.util.ScrollManager;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        scrollManager = ScrollManager.<SettingsViewHolder>with(rootView.findViewById(R.id.settings_list))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new SettingsAdapter(items, this))
                .withLinearLayoutManager()
                .build();

        return rootView;
    }

    @Override
    protected int getToolbarMenu() { return R.menu.fragment_settings; }

    @Override
    public boolean showsFab() { return false; }

    @Override
    protected CharSequence getToolbarTitle() {
        return getString(R.string.settings);
    }

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
