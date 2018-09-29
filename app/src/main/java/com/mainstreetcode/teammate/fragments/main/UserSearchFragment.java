package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.UserAdapter;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.InstantSearch;
import com.mainstreetcode.teammate.util.ScrollManager;

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

    private static final int[] EXCLUDED_VIEWS = {R.id.team_list};

    private SearchView searchView;
    private InstantSearch<String, User> instantSearch;

    private final List<Identifiable> items = new ArrayList<>();

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

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.team_list))
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
