package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.model.TeamChatRoom;
import com.mainstreetcode.teammates.repository.TeamChatRepository;
import com.mainstreetcode.teammates.repository.TeamChatRoomRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;


public class TeamChatViewModel extends ViewModel {

    private static final int NO_MORE_CHATS = -1;

    private final TeamChatRoomRepository repository;
    private final TeamChatRepository chatRepository;

    private final Map<TeamChatRoom, Integer> chatRoomMap;

    public TeamChatViewModel() {
        repository = TeamChatRoomRepository.getInstance();
        chatRepository = TeamChatRepository.getInstance();
        chatRoomMap = new HashMap<>();
    }

    public Flowable<TeamChatRoom> getTeamChatRoom(TeamChatRoom chatRoom) {
        return repository.get(chatRoom);
    }

    public Flowable<List<TeamChatRoom>> getTeamChatRooms() {
        return repository.getTeamChatRooms();
    }

    public Flowable<TeamChat> listenForChat(TeamChatRoom chatRoom) {
        return repository.listenForChat(chatRoom);
    }

    public Completable post(TeamChat chat) {
        return repository.post(chat);
    }

    public Flowable<Boolean> fetchOlderChats(final TeamChatRoom chatRoom) {
        final Integer lastSize = chatRoomMap.get(chatRoom);
        final Integer currentSize = chatRoom.getChats().size();

        if (currentSize.equals(NO_MORE_CHATS)) return Flowable.just(false);
        if (currentSize.equals(lastSize)) return Flowable.just(true);

        chatRoomMap.put(chatRoom, currentSize);
        return Flowable.concat(Flowable.just(true), chatRepository.fetchOlderChats(chatRoom)
                .doOnError(throwable -> chatRoomMap.put(chatRoom, 0))
                .toFlowable()
                .map(updatedRoom -> {
                    if (updatedRoom.getChats().size() == currentSize) {
                        chatRoomMap.put(chatRoom, NO_MORE_CHATS);
                    }
                    return false;
                }));
    }
}
