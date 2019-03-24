package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamChatAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.TeamChatViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment;
import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.util.Deferrer;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.tunjid.androidbootstrap.view.animator.ViewHider;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import io.reactivex.disposables.Disposable;

import static android.text.TextUtils.isEmpty;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING;
import static com.tunjid.androidbootstrap.view.animator.ViewHider.TOP;

public class ChatFragment extends MainActivityFragment
        implements
        TextView.OnEditorActionListener,
        TeamChatAdapter.ChatAdapterListener {

    private static final String ARG_TEAM = "team";
    private static final int[] EXCLUDED_VIEWS = {R.id.chat};

    private boolean wasScrolling;
    private int unreadCount;

    private Team team;
    private List<Differentiable> items;
    private Disposable chatDisposable;

    private TextView dateView;
    private TextView newMessages;

    private Deferrer deferrer;
    private ViewHider dateHider;
    private ViewHider newMessageHider;

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
        team = getArguments().getParcelable(ARG_TEAM);
        items = chatViewModel.getModelList(team);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        SwipeRefreshLayout refresh = rootView.findViewById(R.id.refresh_layout);
        EditText input = rootView.findViewById(R.id.input);
        View send = rootView.findViewById(R.id.send);
        dateView = rootView.findViewById(R.id.date);
        newMessages = rootView.findViewById(R.id.new_messages);

        dateHider = ViewHider.of(dateView).setDirection(TOP).build();
        newMessageHider = ViewHider.of(newMessages).setDirection(ViewHider.BOTTOM).build();
        deferrer = new Deferrer(2000, dateHider::hide);

        scrollManager = ScrollManager.<TeamChatViewHolder>with(rootView.findViewById(R.id.chat))
                .withPlaceholder(new EmptyViewHolder(rootView, R.drawable.ic_message_black_24dp, R.string.no_chats))
                .onLayoutManager(layoutManager -> ((LinearLayoutManager) layoutManager).setStackFromEnd(true))
                .withAdapter(new TeamChatAdapter(items, userViewModel.getCurrentUser(), this))
                .withEndlessScroll(() -> fetchChatsBefore(false))
                .withRefreshLayout(refresh, () -> refresh.setRefreshing(false))
                .addScrollListener((dx, dy) -> updateTopSpacerElevation())
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .addStateListener(this::onScrollStateChanged)
                .addScrollListener(this::onScroll)
                .withLinearLayoutManager()
                .build();

        newMessages.setOnClickListener(view -> scrollManager.withRecyclerView(rv -> rv.smoothScrollToPosition(items.size() - 1)));
        dateView.setOnClickListener(view -> dateHider.hide());
        send.setOnClickListener(view -> sendChat(input));
        input.setOnEditorActionListener(this);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) { wasScrolling = false; }
        });

        newMessageHider.hide();
        dateHider.hide();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        subscribeToChat();
        fetchChatsBefore(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pick_team:
                TeamPickerFragment.change(getActivity(), R.id.request_chat_team_pick);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        chatViewModel.updateLastSeen(team);
        newMessageHider = null;
        newMessages = null;
        dateHider = null;
        dateView = null;
        deferrer = null;
    }

    @Override
    protected int getToolbarMenu() { return R.menu.fragment_chat; }

    @Override
    public boolean showsFab() {
        return false;
    }

    @Override
    protected CharSequence getToolbarTitle() {
        return getString(R.string.team_chat_title, team.getName());
    }

    @Override
    public int[] staticViews() {
        return EXCLUDED_VIEWS;
    }

    @Override
    public void onChatClicked(Chat chat) {
        if (chat.isSuccessful() || !chat.isEmpty()) return;

        int index = items.indexOf(chat);
        if (index == -1) return;

        items.remove(index);
        scrollManager.notifyItemRemoved(index);

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

    private void fetchChatsBefore(boolean fetchLatest) {
        scrollManager.setRefreshing();
        disposables.add(chatViewModel.getMany(team, fetchLatest).subscribe(ChatFragment.this::onChatsUpdated, defaultErrorHandler));
    }

    private void subscribeToChat() {
        chatDisposable = chatViewModel.listenForChat(team).subscribe(chat -> {
            items.add(chat);
            boolean nearBottomOfChat = isNearBottomOfChat();
            unreadCount = nearBottomOfChat ? 0 : unreadCount + 1;
            notifyAndScrollToLast(nearBottomOfChat);
            updateUnreadCount();
        }, ErrorHandler.builder()
                .defaultMessage(getString(R.string.error_default))
                .add(message -> showSnackbar(message.getMessage()))
                .build());

        disposables.add(chatDisposable);
    }

    private void sendChat(TextView textView) {
        String text = textView.getText().toString();
        textView.setText(null);

        if (isEmpty(text)) return;

        Chat chat = Chat.chat(text, userViewModel.getCurrentUser(), team);
        items.add(chat);

        wasScrolling = false;
        notifyAndScrollToLast(true);
        postChat(chat);
    }

    private void postChat(Chat chat) {
        disposables.add(chatViewModel.post(chat).subscribe(__ -> {
            chatViewModel.updateLastSeen(team);
            int index = items.indexOf(chat);

            if (index != -1) scrollManager.notifyItemChanged(index);
            if (!isSubscribedToChat()) subscribeToChat();
        }, ErrorHandler.builder()
                .defaultMessage(getString(R.string.error_default))
                .add(errorMessage -> {
                    chat.setSuccessful(false);

                    int index = items.indexOf(chat);
                    if (index != -1) scrollManager.notifyItemChanged(index);
                })
                .build()));
    }

    private void notifyAndScrollToLast(boolean scrollToLast) {
        final int index = items.size() - 1;

        RecyclerView recyclerView = scrollManager.getRecyclerView();
        if (recyclerView == null) return;

        scrollManager.notifyItemInserted(index);
        if (scrollToLast) recyclerView.smoothScrollToPosition(index);
    }


    private boolean isSubscribedToChat() {
        return chatDisposable != null && !chatDisposable.isDisposed();
    }

    private boolean isNearBottomOfChat() {
        RecyclerView recyclerView = scrollManager.getRecyclerView();
        if (recyclerView == null) return false;

        int lastVisibleItemPosition = scrollManager.getLastVisiblePosition();

        return Math.abs(items.size() - lastVisibleItemPosition) < 4;
    }

    private void onChatsUpdated(DiffUtil.DiffResult result) {
        toggleProgress(false);
        chatViewModel.updateLastSeen(team);
        if (result != null) scrollManager.onDiff(result);
    }

    @SuppressWarnings("unused")
    private void onScroll(int dx, int dy) {
        if (Math.abs(dy) > 8) wasScrolling = true;

        deferrer.advanceDeadline();
        String date = chatViewModel.onScrollPositionChanged(team, scrollManager.getFirstVisiblePosition());

        if (TextUtils.isEmpty(date)) dateHider.hide();
        else dateHider.show();

        if (date.equals(dateView.getText().toString())) return;

        TransitionManager.beginDelayedTransition(
                (ViewGroup) dateView.getParent(),
                new AutoTransition().addTarget(dateView));
        dateView.setText(date);
    }

    private void onScrollStateChanged(int newState) {
        if (newState == SCROLL_STATE_DRAGGING) deferrer.advanceDeadline();
        if (wasScrolling && newState == SCROLL_STATE_IDLE && isNearBottomOfChat()) {
            unreadCount = 0;
            updateUnreadCount();
            fetchChatsBefore(true);
        }
    }

    private void updateUnreadCount() {
        if (unreadCount == 0) newMessageHider.hide();
        else {
            newMessages.setText(unreadCount == 1 ? getString(R.string.chat_new_message) : getString(R.string.chat_new_messages, unreadCount));
            newMessageHider.show();
        }
    }
}
