package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamChatAdapter;
import com.mainstreetcode.teammates.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.fragments.headless.TeamPickerFragment;
import com.mainstreetcode.teammates.model.Chat;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.util.EndlessScroller;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.util.Date;
import java.util.List;

import io.reactivex.disposables.Disposable;

import static android.text.TextUtils.isEmpty;
import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

public class ChatFragment extends MainActivityFragment
        implements
        TextView.OnEditorActionListener,
        TeamChatAdapter.ChatAdapterListener {

    private static final String ARG_TEAM = "team";
    private static final int[] EXCLUDED_VIEWS = {R.id.chat};

    private Team team;
    private List<Chat> chats;
    private Disposable chatDisposable;
    private RecyclerView recyclerView;
    private EmptyViewHolder emptyViewHolder;

    public static ChatFragment newInstance(Team team) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        String superResult = super.getStableTag();
        Team team = getArguments().getParcelable(ARG_TEAM);

        return (team != null)
                ? superResult + "-" + team.hashCode()
                : superResult;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        team = getArguments().getParcelable(ARG_TEAM);
        chats = chatViewModel.getModelList(team);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        EditText input = rootView.findViewById(R.id.input);
        View send = rootView.findViewById(R.id.send);

        recyclerView = rootView.findViewById(R.id.chat);

        input.setOnEditorActionListener(this);
        send.setOnClickListener(view -> sendChat(input));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new TeamChatAdapter(chats, userViewModel.getCurrentUser(), this));
        recyclerView.addOnScrollListener(new EndlessScroller(linearLayoutManager) {
            @Override
            public void onLoadMore(int oldCount) {
                toggleProgress(true);
                fetchChatsBefore(getQueryDate());
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE && isNearBottomOfChat())
                    fetchChatsBefore(new Date());
            }
        });

        emptyViewHolder = new EmptyViewHolder(rootView, R.drawable.ic_message_black_24dp, R.string.no_chats);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle(getString(R.string.team_chat_title, team.getName()));

        Date queryDate = restoredFromBackStack() ? new Date() : getQueryDate();
        fetchChatsBefore(queryDate);
    }

    @Override
    public void onResume() {
        super.onResume();
        subscribeToChat();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pick_team:
                teamViewModel.updateDefaultTeam(Team.empty());
                TeamPickerFragment.pick(getActivity(), R.id.request_chat_team_pick);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        chatViewModel.onChatRoomLeft(team);
        chatViewModel.updateLastSeen(team);
        recyclerView = null;
        emptyViewHolder = null;
    }

    @Override
    protected boolean showsFab() {
        return false;
    }

    @Override
    public int[] staticViews() {
        return EXCLUDED_VIEWS;
    }

    @Override
    public void onChatClicked(Chat chat) {
        if (chat.isSuccessful() || !chat.isEmpty()) return;

        int index = chats.indexOf(chat);
        if (index == -1) return;

        chats.remove(index);
        recyclerView.getAdapter().notifyItemRemoved(index);

        postChat(chat);
    }

    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            sendChat(textView);
            return true;
        }
        return false;
    }

    private void fetchChatsBefore(Date date) {
        disposables.add(chatViewModel
                .chatsBefore(team, date)
                .subscribe(ChatFragment.this::onChatsUpdated, defaultErrorHandler));
    }

    private void subscribeToChat() {
        chatDisposable = chatViewModel.listenForChat(team).subscribe(chat -> {
            chats.add(chat);

            notifyAndScrollToLast(isNearBottomOfChat());
        }, ErrorHandler.builder()
                .defaultMessage(getString(R.string.default_error))
                .add(message -> showSnackbar(message.getMessage()))
                .build());

        disposables.add(chatDisposable);
    }

    private void sendChat(TextView textView) {
        String text = textView.getText().toString();
        textView.setText(null);

        if (isEmpty(text)) return;

        Chat chat = Chat.chat(text, userViewModel.getCurrentUser(), team);
        chats.add(chat);

        notifyAndScrollToLast(true);

        postChat(chat);
    }

    private void postChat(Chat chat) {
        final RecyclerView.Adapter adapter = recyclerView.getAdapter();

        chatViewModel.post(chat).subscribe(() -> {
            chatViewModel.updateLastSeen(team);
            int index = chats.indexOf(chat);

            if (index != 0) adapter.notifyItemChanged(index);
            if (!isSubscribedToChat()) subscribeToChat();
        }, ErrorHandler.builder()
                .defaultMessage(getString(R.string.default_error))
                .add(errorMessage -> {
                    chat.setSuccessful(false);

                    int index = chats.indexOf(chat);
                    if (index != -1) adapter.notifyItemChanged(index);
                })
                .build());
    }

    private void notifyAndScrollToLast(boolean scrollToLast) {
        final int index = chats.size() - 1;

        recyclerView.getAdapter().notifyItemInserted(index);
        if (scrollToLast) recyclerView.smoothScrollToPosition(index);
    }

    private Date getQueryDate() {
        return chats.isEmpty() ? new Date() : chats.get(0).getCreated();
    }

    private boolean isSubscribedToChat() {
        return chatDisposable != null && !chatDisposable.isDisposed();
    }

    private boolean isNearBottomOfChat() {
        LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

        return Math.abs(chats.size() - lastVisibleItemPosition) < 4;
    }

    private void onChatsUpdated(Pair<Boolean, DiffUtil.DiffResult> resultPair) {
        boolean showProgress = resultPair.first != null ? resultPair.first : false;
        DiffUtil.DiffResult result = resultPair.second;

        toggleProgress(showProgress);
        chatViewModel.updateLastSeen(team);
        emptyViewHolder.toggle(chats.isEmpty());
        if (!showProgress && result != null) result.dispatchUpdatesTo(recyclerView.getAdapter());
    }
}
