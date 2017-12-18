package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Chat;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.ChatRepository;
import com.mainstreetcode.teammates.util.ModelDiffCallback;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;

import static io.reactivex.Flowable.concat;
import static io.reactivex.Flowable.just;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.computation;


public class TeamChatViewModel extends ViewModel {

    private static final int NO_MORE = -1;
    private static final int RETRY = -2;

    private final ChatRepository repository;

    private final Map<Team, Integer> chatMap;

    public TeamChatViewModel() {
        repository = ChatRepository.getInstance();
        chatMap = new HashMap<>();
    }

    public void onChatRoomLeft(Team team) {
        chatMap.put(team, RETRY);
    }

    public void updateLastSeen(Team team) {
        repository.updateLastSeen(team);
    }

    public Flowable<Chat> listenForChat(Team team) {
        return repository.listenForChat(team).retry();
    }

    public Completable post(Chat chat) {
        return repository.post(chat).retry(2);
    }

    public Flowable<Pair<Boolean, DiffUtil.DiffResult>> chatsBefore(final List<Chat> chats, final Team team, Date date) {
        final Integer lastSize = chatMap.get(team);
        final Integer currentSize = chats.size();

        if (currentSize.equals(NO_MORE)) return just(getPair(false, getDiffResult(chats, chats)));
        if (currentSize.equals(lastSize)) return just(getPair(true, getDiffResult(chats, chats)));

        chatMap.put(team, currentSize);

        final List<Chat> updated = new ArrayList<>(chats);

        return concat(
                Flowable.fromCallable(() -> getPair(true, getDiffResult(chats, chats)))
                        .subscribeOn(computation())
                        .observeOn(mainThread()),
                repository.chatsBefore(team, date)
                        .doOnError(throwable -> chatMap.put(team, RETRY))
                        .doOnCancel(() -> chatMap.put(team, RETRY))
                        .concatMapDelayError(fetchedChats -> Flowable.fromCallable(() -> {
                                    ModelUtils.preserveList(updated, fetchedChats);
                                    List<Chat> stale = new ArrayList<>(chats);

                                    if (updated.size() == currentSize) chatMap.put(team, NO_MORE);
                                    return getPair(false, getDiffResult(updated, stale));
                                })
                                        .subscribeOn(computation())
                                        .observeOn(mainThread())
                                        .doOnNext(pair -> {
                                            chats.clear();
                                            chats.addAll(updated);
                                        })
                        ));
    }

    private DiffUtil.DiffResult getDiffResult(List<Chat> updated, List<Chat> stale) {
        return DiffUtil.calculateDiff(new ModelDiffCallback(updated, stale));
    }

    private Pair<Boolean, DiffUtil.DiffResult> getPair(Boolean flag, DiffUtil.DiffResult diffResult) {
        return new Pair<>(flag, diffResult);
    }
}
