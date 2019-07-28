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

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.UserAdapter;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.InstantSearch;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

/**
 * Searches for users
 */

public final class UserSearchFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        SearchView.OnQueryTextListener,
        UserAdapter.AdapterListener {

    private static final int[] EXCLUDED_VIEWS = {R.id.list_layout};

    private SearchView searchView;
    private InstantSearch<String, Differentiable> instantSearch;

    public static UserSearchFragment newInstance() {
        UserSearchFragment fragment = new UserSearchFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instantSearch = userViewModel.instantSearch();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_search, container, false);
        searchView = rootView.findViewById(R.id.searchView);

        scrollManager = ScrollManager.<InteractiveViewHolder>with(rootView.findViewById(R.id.list_layout))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new UserAdapter(instantSearch.getCurrentItems(), this))
                .withGridLayoutManager(2)
                .build();

        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);

        if (getTargetRequestCode() != 0) {
            List<Differentiable> items = instantSearch.getCurrentItems();
            items.clear();
            disposables.add(teamMemberViewModel.getAllUsers()
                    .startWith(userViewModel.getCurrentUser())
                    .distinct()
                    .subscribe(items::add, ErrorHandler.EMPTY));
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        subScribeToSearch();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchView.clearFocus();
        searchView = null;
    }

    @Override
    public int[] staticViews() {return EXCLUDED_VIEWS;}

    @Override
    public boolean showsFab() { return false; }

    @Override
    public boolean showsToolBar() { return false; }

    @Override
    public void onUserClicked(User user) {
        Fragment target = getTargetFragment();
        boolean canPick = target instanceof UserAdapter.AdapterListener;

        if (canPick) ((UserAdapter.AdapterListener) target).onUserClicked(user);
        else showFragment(UserEditFragment.newInstance(user));
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String queryText) {
        if (getView() == null || TextUtils.isEmpty(queryText)) return true;
        instantSearch.postSearch(queryText);
        return true;
    }

    private void subScribeToSearch() {
        disposables.add(instantSearch.subscribe()
                .subscribe(scrollManager::onDiff, defaultErrorHandler));
    }
}
