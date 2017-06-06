package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamSearchAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Searches for teams
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public final class TeamSearchFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        SearchView.OnQueryTextListener,
        TeamSearchAdapter.TeamAdapterListener {

    private View createTeam;
    private RecyclerView recyclerView;
    private DatabaseReference teamDbReference;
    private final List<Team> items = new ArrayList<>();

    private final ValueEventListener teamListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot == null) return;

            if (items.isEmpty()) {
                TransitionManager.beginDelayedTransition((ViewGroup) createTeam.getParent(), new AutoTransition());
                createTeam.setVisibility(View.VISIBLE);
            }

            items.clear();

            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                items.add(new Team(childSnapshot.getKey(), childSnapshot));
            }
            recyclerView.getAdapter().notifyDataSetChanged();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            databaseError.toException().printStackTrace();
        }
    };

    public static TeamSearchFragment newInstance() {
        TeamSearchFragment fragment = new TeamSearchFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        teamDbReference = FirebaseDatabase.getInstance().getReference().child(Team.DB_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team_search, container, false);
        createTeam = rootView.findViewById(R.id.create_team);
        recyclerView = rootView.findViewById(R.id.team_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TeamSearchAdapter(items, this));

        createTeam.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_team_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchItem.expandActionView();
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toggleFab(false);
        setToolbarTitle(getString(R.string.team_search));

        teamDbReference.limitToFirst(5)
                .addListenerForSingleValueEvent(teamListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        teamDbReference.removeEventListener(teamListener);
        createTeam = null;
        recyclerView = null;
    }

    @Override
    public void onTeamClicked(Team team) {
        showFragment(TeamDetailFragment.newInstance(team, false));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_team:
                showFragment(TeamDetailFragment.newInstance(new Team(), true));
                break;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String queryText) {
        if (getView() == null) return true;
        if (TextUtils.isEmpty(queryText)) {
            items.clear();
            recyclerView.getAdapter().notifyDataSetChanged();
            return true;
        }
        teamDbReference.removeEventListener(teamListener);
        teamDbReference.orderByChild(Team.SEARCH_INDEX_KEY)
                .startAt(queryText)
                .endAt(queryText + "\uf8ff")
                .limitToFirst(10)
                .addListenerForSingleValueEvent(teamListener);
        return true;
    }
}
