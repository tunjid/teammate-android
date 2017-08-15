package com.mainstreetcode.teammates.repository;

import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.TeamChatDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.TeammateException;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

import static io.reactivex.schedulers.Schedulers.io;

public class TeamChatRepository extends ModelRespository<TeamChat> {

    private static TeamChatRepository ourInstance;

    private final TeammateApi api;
    private final TeamChatDao chatDao;
    private final UserRepository userRepository;

    private TeamChatRepository() {
        api = TeammateService.getApiInstance();
        chatDao = AppDatabase.getInstance().teamChatDao();
        userRepository = UserRepository.getInstance();
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

    @Override
    public Predicate<TeamChat> getNotificationFilter() {
        return teamChat -> !teamChat.getUser().equals(userRepository.getCurrentUser());
    }
}
