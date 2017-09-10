package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamChatRoomAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.TeamChatRoom;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;


public final class TeamChatRoomFragment extends MainActivityFragment
        implements
        TeamChatRoomAdapter.TeamChatRoomAdapterListener {

    private RecyclerView recyclerView;
    private final List<TeamChatRoom> chatRooms = new ArrayList<>();

    private final Consumer<List<TeamChatRoom>> teamConsumer = (teams) -> {
        this.chatRooms.clear();
        this.chatRooms.addAll(teams);
        recyclerView.getAdapter().notifyDataSetChanged();
    };

    public static TeamChatRoomFragment newInstance() {
        TeamChatRoomFragment fragment = new TeamChatRoomFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getStableTag() {
        String superResult = super.getStableTag();
        if (getTargetFragment() != null) superResult += getTargetRequestCode();
        return superResult;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_teams, container, false);
        recyclerView = rootView.findViewById(R.id.team_list);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new TeamChatRoomAdapter(chatRooms, this));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle(getString(R.string.team_chat_rooms));

        disposables.add(teamChatViewModel.getTeamChatRooms().subscribe(teamConsumer, defaultErrorHandler));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
    }

    @Override
    protected boolean showsFab() {
        return false;
    }

    @Override
    public void onChatRoomClicked(TeamChatRoom item) {
        showFragment(TeamChatFragment.newInstance(item));
    }

}
