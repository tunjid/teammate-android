package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamChatAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.util.EndlessScroller;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class TeamChatFragment extends MainActivityFragment
        implements TextView.OnEditorActionListener {

    private static final String ARG_TEAM = "team";

    private Team team;
    private List<TeamChat> chats = new ArrayList<>();
    private RecyclerView recyclerView;

    public static TeamChatFragment newInstance(Team team) {
        TeamChatFragment fragment = new TeamChatFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getStableTag() {
        String superResult = super.getStableTag();
        Team team = getArguments().getParcelable(ARG_TEAM);

        return (team != null)
                ? superResult + "-" + team.hashCode()
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
        EditText input = rootView.findViewById(R.id.input);
        View send = rootView.findViewById(R.id.send);

        recyclerView = rootView.findViewById(R.id.chat);

        input.setOnEditorActionListener(this);
        send.setOnClickListener(view -> sendChat(input));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new TeamChatAdapter(chats, userViewModel.getCurrentUser()));
        recyclerView.addOnScrollListener(new EndlessScroller(linearLayoutManager) {
            @Override
            public void onLoadMore(int oldCount) {
                toggleProgress(true);
                disposables.add(teamChatViewModel
                        .fetchOlderChats(chats, team, getQueryDate())
                        .subscribe(showProgress -> onChatsUpdated(showProgress, oldCount), defaultErrorHandler));
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle(getString(R.string.team_chat_title, team.getName()));

        subsribeToChat();

        if (chats.isEmpty()) {
            disposables.add(teamChatViewModel.fetchOlderChats(chats, team, getQueryDate()).subscribe(chat -> {
                recyclerView.getAdapter().notifyItemInserted(0);
                toggleProgress(false);
            }, defaultErrorHandler));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        team.updateLastSeen();
        recyclerView = null;
    }

    @Override
    protected boolean showsFab() {
        return false;
    }

    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            sendChat(textView);
            return true;
        }
        return false;
    }

    private void onChatsUpdated(boolean showProgess, int oldCount) {
        toggleProgress(showProgess);
        team.updateLastSeen();
        if (!showProgess)
            recyclerView.getAdapter().notifyItemRangeInserted(0, chats.size() - oldCount);
    }

    private void subsribeToChat() {
        disposables.add(teamChatViewModel.listenForChat(team).subscribe(chat -> {
            chats.add(chat);
            recyclerView.getAdapter().notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(chats.size() - 1);
        }, ErrorHandler.builder()
                .defaultMessage(getString(R.string.default_error))
                .add(message -> {
                    showSnackbar(message);
                    subsribeToChat();
                })
                .build()));
    }

    private void sendChat(TextView textView) {
        String text = textView.getText().toString();
        textView.setText(null);

        if (isEmpty(text)) return;

        TeamChat chat = TeamChat.chat(text, userViewModel.getCurrentUser(), team);
        chats.add(chat);

        final int index = chats.indexOf(chat);
        final RecyclerView.Adapter adapter = recyclerView.getAdapter();

        recyclerView.smoothScrollToPosition(index);
        adapter.notifyItemInserted(index);
        teamChatViewModel.post(chat).subscribe(() -> {
            adapter.notifyItemChanged(index);
            team.updateLastSeen();
        }, defaultErrorHandler);
    }

    private Date getQueryDate() {
        return chats.isEmpty() ? new Date() : chats.get(chats.size() - 1).getCreated();
    }
}
