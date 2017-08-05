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
import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.model.TeamChatRoom;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * Edits a Team member
 */

public class TeamChatFragment extends MainActivityFragment
        implements TextView.OnEditorActionListener {

    private static final String ARG_CHAT_ROOM = "chat-room";

    private TeamChatRoom chatRoom;
    private RecyclerView recyclerView;

    public static TeamChatFragment newInstance(TeamChatRoom chatRoom) {
        TeamChatFragment fragment = new TeamChatFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_CHAT_ROOM, chatRoom);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getStableTag() {
        String superResult = super.getStableTag();
        TeamChatRoom tempRoom = getArguments().getParcelable(ARG_CHAT_ROOM);

        return (tempRoom != null)
                ? superResult + "-" + tempRoom.hashCode()
                : superResult;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        chatRoom = getArguments().getParcelable(ARG_CHAT_ROOM);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team_chat, container, false);
        EditText input = rootView.findViewById(R.id.input);

        recyclerView = rootView.findViewById(R.id.chat);

        input.setOnEditorActionListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new TeamChatAdapter(chatRoom, userViewModel.getCurrentUser()));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toggleFab(false);
        setToolbarTitle(getString(R.string.edit_user));

        subsribeToChat();

        disposables.add(teamChatViewModel.getTeamChatRoom(chatRoom).subscribe(chat ->
                recyclerView.getAdapter().notifyDataSetChanged(), defaultErrorHandler));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
        toggleFab(false);
    }

    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                && (event.getAction() == KeyEvent.ACTION_DOWN))) {

            String text = textView.getText().toString();
            textView.setText(null);

            if (isEmpty(text)) return true;

            TeamChat chat = chatRoom.chat(text, userViewModel.getCurrentUser());
            chatRoom.add(chat);

            final List<TeamChat> chats = chatRoom.getChats();
            final int index = chats.indexOf(chat);
            final RecyclerView.Adapter adapter = recyclerView.getAdapter();

            recyclerView.smoothScrollToPosition(index);
            adapter.notifyItemInserted(index);
            teamChatViewModel.post(chat).subscribe(() -> adapter.notifyItemChanged(index), defaultErrorHandler);

            return true;
        }
        return false;
    }

    private void subsribeToChat() {
        disposables.add(teamChatViewModel.listenForChat(chatRoom).subscribe(chat -> {
            chatRoom.add(chat);
            recyclerView.getAdapter().notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(chatRoom.getChats().size() - 1);
        }, ErrorHandler.builder()
                .defaultMessage(getString(R.string.default_error))
                .add(message -> {
                    showSnackbar(message);
                    subsribeToChat();
                })
                .build()));
    }
}
