package com.mainstreetcode.teammates.fragments.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.MediaAdapter;
import com.mainstreetcode.teammates.adapters.viewholders.MediaViewHolder;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class TeamMediaFragment extends MainActivityFragment {

    private static final String ARG_TEAM = "team";

    private Team team;
    private List<Media> mediaList = new ArrayList<>();
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
        Team tempTeam = getArguments().getParcelable(ARG_TEAM);

        return (tempTeam != null)
                ? superResult + "-" + tempTeam.hashCode()
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
        View rootView = inflater.inflate(R.layout.fragment_team_media, container, false);

        recyclerView = rootView.findViewById(R.id.team_media);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new MediaAdapter(mediaList, media -> showFragment(MediaDetailFragment.newInstance(media))));
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
        setToolbarTitle(getString(R.string.meda_title, team.getName()));

        disposables.add(mediaViewModel.getTeamMedia(team).subscribe(newList -> {
            mediaList.clear();
            mediaList.addAll(newList);
            recyclerView.getAdapter().notifyDataSetChanged();
            toggleProgress(false);
        }, defaultErrorHandler));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_media, menu);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
    }

    @Override
    @Nullable
    @SuppressLint("CommitTransaction")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        if (fragmentTo.getStableTag().contains(MediaDetailFragment.class.getSimpleName())) {
            Media media = fragmentTo.getArguments().getParcelable(MediaDetailFragment.ARG_MEDIA);

            if (media == null) return null;
            MediaViewHolder holder = (MediaViewHolder) recyclerView.findViewHolderForItemId(media.hashCode());

            return getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .addSharedElement(holder.thumbnailView, holder.media.hashCode() + "-" + holder.thumbnailView.getId());
        }
        return null;
    }
}
