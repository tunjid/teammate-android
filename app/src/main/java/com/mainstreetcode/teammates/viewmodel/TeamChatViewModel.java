package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.repository.TeamChatRepository;
import com.mainstreetcode.teammates.util.ModelDiffCallback;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;

import static io.reactivex.Flowable.concat;
import static io.reactivex.Flowable.just;


public class TeamChatViewModel extends ViewModel {

    private static final int NO_MORE = -1;
    private static final int RETRY = -2;

    private final TeamChatRepository repository;

    private final Map<Team, Integer> chatMap;

    public TeamChatViewModel() {
        repository = TeamChatRepository.getInstance();
        chatMap = new HashMap<>();
    }

    public void onChatRoomLeft(Team team) {
        chatMap.put(team, RETRY);
    }

    public void updateLastSeen(Team team) {
        repository.updateLastSeen(team);
    }

    public Flowable<TeamChat> listenForChat(Team team) {
        return repository.listenForChat(team).retry();
    }

    public Completable post(TeamChat chat) {
        return repository.post(chat).retry(2);
    }

    public Flowable<Pair<Boolean, DiffUtil.DiffResult>> chatsBefore(final List<TeamChat> chats, final Team team, Date date) {
        final Integer lastSize = chatMap.get(team);
        final Integer currentSize = chats.size();

        if (currentSize.equals(NO_MORE)) return just(getPair(false, getDiffResult(chats, chats)));
        if (currentSize.equals(lastSize)) return just(getPair(true, getDiffResult(chats, chats)));

        chatMap.put(team, currentSize);
        return concat(just(getPair(true, getDiffResult(chats, chats))), repository.chatsBefore(team, date)
                .doOnError(throwable -> chatMap.put(team, RETRY))
                .doOnCancel(() -> chatMap.put(team, RETRY))
                .map(updatedChats -> {
                    List<TeamChat> copy = new ArrayList<>(chats);
                    ModelUtils.preserveList(chats, updatedChats);
                    Collections.sort(chats);

                    if (chats.size() == currentSize) chatMap.put(team, NO_MORE);
                    return getPair(false, getDiffResult(chats, copy));
                }));
    }

    private DiffUtil.DiffResult getDiffResult(List<TeamChat> updated, List<TeamChat> stale) {
        return DiffUtil.calculateDiff(new ModelDiffCallback(updated, stale));
    }

    private Pair<Boolean, DiffUtil.DiffResult> getPair(Boolean flag, DiffUtil.DiffResult diffResult) {
        return new Pair<>(flag, diffResult);
    }
}
