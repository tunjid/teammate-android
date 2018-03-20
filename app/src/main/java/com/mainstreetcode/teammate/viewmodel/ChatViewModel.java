package com.mainstreetcode.teammate.viewmodel;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;
import android.util.Log;

import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.notifications.ChatNotifier;
import com.mainstreetcode.teammate.repository.ChatRepository;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.socket.engineio.client.EngineIOException;

import static com.mainstreetcode.teammate.util.ModelUtils.findFirst;
import static io.reactivex.Flowable.concat;
import static io.reactivex.Flowable.just;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;


public class ChatViewModel extends TeamMappedViewModel<Chat> {

    private static final int NO_MORE = -1;
    private static final int RETRY = -2;
    private static final String XHR_POST_ERROR = "xhr post error";

    private final ChatRepository repository;
    private final ChatNotifier notifier;

    private final Map<Team, Integer> chatMap;

    public ChatViewModel() {
        repository = ChatRepository.getInstance();
        notifier = ChatNotifier.getInstance();
        chatMap = new HashMap<>();
    }

    @Override
    boolean hasNativeAds() {
        return false;
    }

    @Override
    boolean sortsAscending() {return true;}

    public void onChatRoomLeft(Team team) {chatMap.put(team, RETRY);}

    public void updateLastSeen(Team team) {
        repository.updateLastSeen(team);

        Chat chat = findFirst(getModelList(team), Chat.class);
        if (chat != null) clearNotifications(chat);
    }

    public Flowable<Chat> listenForChat(Team team) {
        return repository.listenForChat(team)
                .onErrorResumeNext(listenRetryFunction(team))
                .doOnSubscribe(subscription -> notifier.setChatVisibility(team, true))
                .doFinally(() -> notifier.setChatVisibility(team, false))
                .observeOn(mainThread());
    }

    public Completable post(Chat chat) {
        return repository.post(chat).onErrorResumeNext(postRetryFunction(chat, 0)).observeOn(mainThread());
    }

    @Override
    Flowable<List<Chat>> fetch(Team key, boolean fetchLatest) {
        return repository.modelsBefore(key, getQueryDate(key, fetchLatest))
                .doOnError(throwable -> checkForInvalidTeam(throwable, key));
    }

    public Flowable<Pair<Boolean, DiffUtil.DiffResult>> chatsBefore(final Team team, boolean fetchLatest) {
        final List<Identifiable> chats = getModelList(team);

        final Integer lastSize = chatMap.get(team);
        final Integer currentSize = chats.size();

        if (currentSize.equals(NO_MORE)) return getDiffResult(false, chats);
        if (currentSize.equals(lastSize)) return getDiffResult(true, chats);

        chatMap.put(team, currentSize);

        Flowable<DiffUtil.DiffResult> source = fetchLatest ? getLatest(team) : getMore(team);
        source.doOnCancel(() -> chatMap.put(team, RETRY));

        Flowable<Pair<Boolean, DiffUtil.DiffResult>> immediate = getDiffResult(true, chats);

        Flowable<Pair<Boolean, DiffUtil.DiffResult>> fetched = source
                .map(diffResult -> new Pair<>(false, diffResult))
                .doOnNext(pair -> {if (chats.size() == currentSize) chatMap.put(team, NO_MORE);});

        return concat(immediate, fetched);
    }

    private Flowable<Pair<Boolean, DiffUtil.DiffResult>> getDiffResult(boolean showProgress, List<Identifiable> chats) {
        Flowable<List<Identifiable>> sourceFlowable = just(new ArrayList<Identifiable>());
        return Identifiable.diff(sourceFlowable, () -> chats, (sameChats, emptyAdditions) -> sameChats)
                .map(diffResult -> new Pair<>(showProgress, diffResult));
    }

    private Function<Throwable, Flowable<Chat>> listenRetryFunction(Team team) {
        return (throwable -> shouldRetry(throwable)
                ? repository.listenForChat(team)
                .onErrorResumeNext(listenRetryFunction(team))
                .doOnSubscribe(subscription -> notifier.setChatVisibility(team, true))
                .doFinally(() -> notifier.setChatVisibility(team, false))
                .observeOn(mainThread())
                : Flowable.error(throwable)
        );
    }

    @Override
    protected boolean checkForInvalidTeam(Throwable throwable, Team team) {
        boolean isInvalid = super.checkForInvalidTeam(throwable, team);
        if (!isInvalid) chatMap.put(team, RETRY);
        return isInvalid;
    }

    private Function<Throwable, Completable> postRetryFunction(Chat chat, int previousRetries) {
        return throwable -> {
            int retries = previousRetries + 1;
            return retries <= 3
                    ? Completable.timer(300, TimeUnit.MILLISECONDS)
                    .andThen(repository.post(chat).onErrorResumeNext(postRetryFunction(chat, retries)))
                    .observeOn(mainThread())
                    : Completable.error(throwable);
        };
    }

    private boolean shouldRetry(Throwable throwable) {
        boolean retry = XHR_POST_ERROR.equals(throwable.getMessage()) ||
                (throwable instanceof EngineIOException && throwable.getCause() instanceof EOFException);
        if (retry) Log.i("CHAT", "Retrying because of predictable error");
        return retry;
    }

    @Nullable
    private Date getQueryDate(Team team, boolean fetchLatest) {
        if (fetchLatest) return null;
        Chat chat = findFirst(getModelList(team), Chat.class);
        return chat == null ? null : chat.getCreated();
    }
}
