package com.mainstreetcode.teammate.viewmodel;

import android.arch.core.util.Function;

import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.notifications.ChatNotifier;
import com.mainstreetcode.teammate.repository.ChatRepository;
import com.mainstreetcode.teammate.util.Logger;

import java.io.EOFException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.socket.engineio.client.EngineIOException;

import static com.mainstreetcode.teammate.util.ModelUtils.findFirst;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;


public class ChatViewModel extends TeamMappedViewModel<Chat> {

    private static final String XHR_POST_ERROR = "xhr post error";

    private final ChatRepository repository;
    private final ChatNotifier notifier;

    public ChatViewModel() {
        repository = ChatRepository.getInstance();
        notifier = ChatNotifier.getInstance();
    }

    @Override
    boolean hasNativeAds() {return false;}

    @Override
    boolean sortsAscending() {return true;}

    @Override
    Class<Chat> valueClass() { return Chat.class; }

    public void updateLastSeen(Team team) {
        repository.updateLastSeen(team);

        Chat chat = findFirst(getModelList(team), Chat.class);
        if (chat != null) clearNotifications(chat);
    }

    public Flowable<Chat> listenForChat(Team team) {
        return repository.listenForChat(team)
                .onErrorResumeNext(listenRetryFunction(team)::apply)
                .doOnSubscribe(subscription -> notifier.setChatVisibility(team, true))
                .doFinally(() -> notifier.setChatVisibility(team, false))
                .observeOn(mainThread());
    }

    public Completable post(Chat chat) {
        return repository.post(chat).onErrorResumeNext(postRetryFunction(chat, 0)::apply).observeOn(mainThread());
    }

    @Override
    Flowable<List<Chat>> fetch(Team key, boolean fetchLatest) {
        return repository.modelsBefore(key, getQueryDate(fetchLatest, key, Chat::getCreated));
    }

    private Function<Throwable, Flowable<Chat>> listenRetryFunction(Team team) {
        return (throwable -> shouldRetry(throwable)
                ? repository.listenForChat(team)
                .onErrorResumeNext(listenRetryFunction(team)::apply)
                .doOnSubscribe(subscription -> notifier.setChatVisibility(team, true))
                .doFinally(() -> notifier.setChatVisibility(team, false))
                .observeOn(mainThread())
                : Flowable.error(throwable)
        );
    }

    private Function<Throwable, Completable> postRetryFunction(Chat chat, int previousRetries) {
        return throwable -> {
            int retries = previousRetries + 1;
            return retries <= 3
                    ? Completable.timer(300, TimeUnit.MILLISECONDS)
                    .andThen(repository.post(chat).onErrorResumeNext(postRetryFunction(chat, retries)::apply))
                    .observeOn(mainThread())
                    : Completable.error(throwable);
        };
    }

    private boolean shouldRetry(Throwable throwable) {
        boolean retry = XHR_POST_ERROR.equals(throwable.getMessage()) ||
                (throwable instanceof EngineIOException && throwable.getCause() instanceof EOFException);
        if (retry) Logger.log("CHAT", "Retrying because of predictable error", throwable);
        return retry;
    }

    @Override
    Date getQueryDate(boolean fetchLatest, Team key, Function<Chat, Date> dateFunction) {
        if (fetchLatest) return null;

        // Chats use find first
        Chat value = findFirst(getModelList(key), valueClass());
        return value == null ? null : dateFunction.apply(value);
    }
}
