package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.TeamChatRoom;

public class TeamMediaFragment extends MainActivityFragment {

    private static final String ARG_TEAM = "team";

    private Team team;
    private RecyclerView recyclerView;

    public static TeamMediaFragment newInstance(Team team) {
        TeamMediaFragment fragment = new TeamMediaFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getStableTag() {
        String superResult = super.getStableTag();
        TeamChatRoom tempRoom = getArguments().getParcelable(ARG_TEAM);

        return (tempRoom != null)
                ? superResult + "-" + tempRoom.hashCode()
                : superResult;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        team = getArguments().getParcelable(ARG_TEAM);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team_chat, container, false);

        recyclerView = rootView.findViewById(R.id.chat);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setAdapter(new TeamChatAdapter(team, userViewModel.getCurrentUser()));
//        recyclerView.addOnScrollListener(new EndlessScroller(linearLayoutManager) {
//            @Override
//            public void onLoadMore(int oldCount) {
//                toggleProgress(true);
//                disposables.add(teamChatViewModel
//                        .fetchOlderChats(team)
//                        .subscribe(showProgress -> onChatsUpdated(showProgress, oldCount), defaultErrorHandler));
//            }
//        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toggleFab(false);
//        setToolbarTitle(getString(R.string.team_chat_title, team.getTeam().getName()));


//        disposables.add(teamChatViewModel.getTeamChatRoom(team).subscribe(chat -> {
//            recyclerView.getAdapter().notifyItemInserted(0);
//            toggleProgress(false);
//        }, defaultErrorHandler));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
    }
}
