package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.model.TeamChatRoom;
import com.mainstreetcode.teammates.repository.TeamChatRoomRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;


public class TeamChatViewModel extends ViewModel {

    private final TeamChatRoomRepository repository;


    public TeamChatViewModel() {
        repository = TeamChatRoomRepository.getInstance();
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
}
