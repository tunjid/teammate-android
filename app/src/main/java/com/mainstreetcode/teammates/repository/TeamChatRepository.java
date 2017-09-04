package com.mainstreetcode.teammates.repository;

import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.model.TeamChatRoom;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.TeamChatDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.TeammateException;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;
import static java.util.Collections.sort;

public class TeamChatRepository extends ModelRespository<TeamChat> {

    private static TeamChatRepository ourInstance;

    private final TeammateApi api;
    private final TeamChatDao chatDao;

    private TeamChatRepository() {
        api = TeammateService.getApiInstance();
        chatDao = AppDatabase.getInstance().teamChatDao();
    }

    public static TeamChatRepository getInstance() {
        if (ourInstance == null) ourInstance = new TeamChatRepository();
        return ourInstance;
    }

    @Override
    public Single<TeamChat> createOrUpdate(TeamChat model) {
        return Single.error(new TeammateException("Chats are created via socket IO"));
    }

    @Override
    public Flowable<TeamChat> get(String id) {
        Maybe<TeamChat> local = chatDao.get(id).subscribeOn(io());
        Maybe<TeamChat> remote = api.getTeamChat(id).map(getSaveFunction()).toMaybe();

        return cacheThenRemote(local, remote);
    }

    @Override
    public Single<TeamChat> delete(TeamChat chat) {
        return api.deleteChat(chat.getId())
                .map(ignored -> {
                    chatDao.delete(chat);
                    return chat;
                });
    }

    @Override
    Function<List<TeamChat>, List<TeamChat>> provideSaveManyFunction() {
        return chats -> {
            chatDao.upsert(chats);
            return chats;
        };
    }

    public Single<TeamChatRoom> fetchOlderChats(final TeamChatRoom chatRoom) {
        final List<TeamChat> chats = chatRoom.getChats();
        final Date date = chats.isEmpty() ? new Date() : chats.get(0).getCreated();

        Maybe<List<TeamChat>> local = chatDao.chatsBefore(chatRoom.getId(), date)
                .filter(teamChats -> false);
        Single<List<TeamChat>> remote = api.chatsBefore(chatRoom, date);

        return Maybe.concat(local, remote.toMaybe())
                .firstOrError()
                .map(fetchedChats -> {
                    getSaveManyFunction().apply(fetchedChats);
                    Set<TeamChat> chatSet = new HashSet<>(chats);
                    chatSet.addAll(fetchedChats);

                    chats.clear();
                    chats.addAll(chatSet);
                    sort(chats, TeamChat.COMPARATOR);
                    return chatRoom;
                })
                .subscribeOn(io())
                .observeOn(mainThread());
    }

    public Single<List<TeamChat>> fetchUnreadChats(String chatRoomId) {
        return TeamChatRoomRepository.getInstance().get(chatRoomId).firstOrError()
                .flatMapMaybe(chatRoom -> chatDao.chatsAfter(chatRoom.getId(), chatRoom.getLastSeen()).subscribeOn(io())
                        .observeOn(mainThread()))
                .toSingle();
    }
}
