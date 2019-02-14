package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.UserAdapter;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.InstantSearch;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Searches for teams
 */

public final class UserSearchFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        SearchView.OnQueryTextListener,
        UserAdapter.AdapterListener {

    private static final int[] EXCLUDED_VIEWS = {R.id.list_layout};

    private SearchView searchView;
    private InstantSearch<String, User> instantSearch;

    private final List<Differentiable> items = new ArrayList<>();

    public static UserSearchFragment newInstance() {
        UserSearchFragment fragment = new UserSearchFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        instantSearch = userViewModel.instantSearch();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_search, container, false);
        searchView = rootView.findViewById(R.id.searchView);

        scrollManager = ScrollManager.<InteractiveViewHolder>with(rootView.findViewById(R.id.list_layout))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new UserAdapter(items, this))
                .withGridLayoutManager(2)
                .build();

        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);

        if (getTargetRequestCode() != 0) {
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
        subScribeToSearch(searchView.getQuery().toString());
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

    private void subScribeToSearch(String query) {
        if (instantSearch.postSearch(query)) return;
        disposables.add(instantSearch.subscribe()
                .doOnSubscribe(subscription -> subScribeToSearch(query))
                .subscribe(this::onUsersUpdated, defaultErrorHandler));
    }

    private void onUsersUpdated(List<User> users) {
        this.items.clear();
        this.items.addAll(users);
        scrollManager.notifyDataSetChanged();
    }
}
