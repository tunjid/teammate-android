package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.repository.TeamChatRepository;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Flowable;

import static com.mainstreetcode.teammates.model.TeamChat.COMPARATOR;


public class TeamChatViewModel extends ViewModel {

    private static final int NO_MORE_CHATS = -1;

    private final TeamChatRepository repository;

    private final Map<Team, Integer> chatMap;

    public TeamChatViewModel() {
        repository = TeamChatRepository.getInstance();
        chatMap = new HashMap<>();
    }

    public Flowable<TeamChat> listenForChat(Team team) {
        return repository.listenForChat(team);
    }

    public Completable post(TeamChat chat) {
        return repository.post(chat);
    }

    public Flowable<Boolean> fetchOlderChats(final List<TeamChat> chats, final Team team, Date date) {
        final Integer lastSize = chatMap.get(team);
        final Integer currentSize = chats.size();

        if (currentSize.equals(NO_MORE_CHATS)) return Flowable.just(false);
        if (currentSize.equals(lastSize)) return Flowable.just(true);

        chatMap.put(team, currentSize);
        return Flowable.concat(Flowable.just(true), repository.fetchOlderChats(team, date)
                .doOnError(throwable -> chatMap.put(team, 0))
                .map(updatedChats -> {
                    Set<TeamChat> set = new HashSet<>(chats);
                    set.addAll(updatedChats);
                    chats.clear();
                    chats.addAll(set);
                    Collections.sort(chats, COMPARATOR);

                    if (chats.size() == currentSize) chatMap.put(team, NO_MORE_CHATS);
                    return false;
                }));
    }
}
