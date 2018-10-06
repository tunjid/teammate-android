package com.mainstreetcode.teammate.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.ChatDao;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.socket.SocketFactory;
import com.mainstreetcode.teammate.util.TeammateException;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static com.mainstreetcode.teammate.socket.SocketFactory.EVENT_NEW_MESSAGE;
import static io.reactivex.schedulers.Schedulers.io;
import static io.socket.client.Socket.EVENT_ERROR;

public class ChatRepository extends TeamQueryRepository<Chat> {


    private static final String TEAM_SEEN_TIMES = "TeamRepository.team.seen.times";
    private static final int TEAM_NOT_SEEN = -1;

    private static final Gson CHAT_GSON = getChatGson();

    private static ChatRepository ourInstance;

    private final App app;
    private final TeammateApi api;
    private final ChatDao chatDao;
    private final ModelRepository<User> userModelRepository;
    private final ModelRepository<Team> teamModelRepository;

    private ChatRepository() {
        app = App.getInstance();
        api = TeammateService.getApiInstance();
        chatDao = AppDatabase.getInstance().teamChatDao();
        userModelRepository = UserRepository.getInstance();
        teamModelRepository = TeamRepository.getInstance();
    }

    public static ChatRepository getInstance() {
        if (ourInstance == null) ourInstance = new ChatRepository();
        return ourInstance;
    }

    @Override
    public EntityDao<? super Chat> dao() {
        return chatDao;
    }

    @Override
    public Single<Chat> createOrUpdate(Chat model) {
        return Single.just(model).map(getSaveFunction()).subscribeOn(io());
    }

    @Override
    public Flowable<Chat> get(String id) {
        Maybe<Chat> local = chatDao.get(id).subscribeOn(io());
        Maybe<Chat> remote = api.getTeamChat(id).map(getSaveFunction()).toMaybe();

        return fetchThenGetModel(local, remote);
    }

    @Override
    public Single<Chat> delete(Chat chat) {
        return api.deleteChat(chat.getId())
                .map(ignored -> {
                    chatDao.delete(chat);
                    return chat;
                });
    }

    @Override
    Function<List<Chat>, List<Chat>> provideSaveManyFunction() {
        return chats -> {
            int size = chats.size();
            List<User> users = new ArrayList<>(size);
            List<Team> teams = new ArrayList<>(size);

            for (Chat chat : chats) {
                users.add(chat.getUser());
                teams.add(chat.getTeam());
            }

            userModelRepository.saveAsNested().apply(users);
            teamModelRepository.saveAsNested().apply(teams);

            chatDao.upsert(chats);
            return chats;
        };
    }

    @Override
    Maybe<List<Chat>> localModelsBefore(Team team, @Nullable Date date) {
        if (date == null) date = new Date();
        return chatDao.chatsBefore(team.getId(), date, DEF_QUERY_LIMIT).subscribeOn(io());
    }

    @Override
    Maybe<List<Chat>> remoteModelsBefore(Team team, @Nullable Date date) {
        return api.chatsBefore(team.getId(), date, DEF_QUERY_LIMIT).map(getSaveManyFunction()).toMaybe();
    }

    public Flowable<List<Chat>> fetchUnreadChats() {
        User currentUser = UserRepository.getInstance().getCurrentUser();
        return RoleRepository.getInstance().getMyRoles()
                .firstElement()
                .toFlowable()
                .flatMap(Flowable::fromIterable)
                .map(Role::getTeam)
                .map(team -> new Pair<>(team.getId(), getLastTeamSeen(team)))
                .flatMapMaybe(teamDatePair -> chatDao.unreadChats(teamDatePair.first, currentUser, teamDatePair.second))
                .filter(chats -> !chats.isEmpty());
    }

    public Flowable<Chat> listenForChat(Team team) {
        return SocketFactory.getInstance().getTeamChatSocket().flatMapPublisher(socket -> {
            JSONObject result;
            try { result = new JSONObject(CHAT_GSON.toJson(team));}
            catch (Exception e) {return Flowable.error(e);}

            socket.emit(SocketFactory.EVENT_JOIN, result);
            User signedInUser = UserRepository.getInstance().getCurrentUser();

            return Flowable.<Chat>create(emitter -> {
                socket.on(EVENT_NEW_MESSAGE, args -> emitter.onNext(parseChat(args)));
                socket.once(EVENT_ERROR, args -> {
                    if (!emitter.isCancelled()) emitter.onError((Throwable) args[0]);
                });
            }, BackpressureStrategy.DROP)
                    .filter(chat -> team.equals(chat.getTeam()) && !signedInUser.equals(chat.getUser()));
        });
    }

    public Completable post(Chat chat) {
        return SocketFactory.getInstance().getTeamChatSocket().flatMapCompletable(socket -> Completable.create(emitter -> {
            JSONObject result = new JSONObject(CHAT_GSON.toJson(chat));

            socket.emit(EVENT_NEW_MESSAGE, new Object[]{result}, args -> {
                Chat created = parseChat(args);
                if (created == null) {
                    emitter.onError(new TeammateException("Unable to post chat"));
                    return;
                }
                chat.update(created);
                emitter.onComplete();
            });

        }));
    }

    public void updateLastSeen(Team team) {
        SharedPreferences preferences = app.getSharedPreferences(TEAM_SEEN_TIMES, Context.MODE_PRIVATE);
        preferences.edit().putLong(team.getId(), new Date().getTime()).apply();
    }

    private Date getLastTeamSeen(Team team) {
        SharedPreferences preferences = app.getSharedPreferences(TEAM_SEEN_TIMES, Context.MODE_PRIVATE);
        long timeStamp = preferences.getLong(team.getId(), TEAM_NOT_SEEN);
        if (timeStamp == TEAM_NOT_SEEN) {
            updateLastSeen(team);
            timeStamp = new Date().getTime() - (1000 * 60 * 2);
        }
        return new Date(timeStamp);
    }

    @Nullable
    private static Chat parseChat(Object... args) {
        try {return CHAT_GSON.fromJson(args[0].toString(), Chat.class);}
        catch (Exception e) {return null;}
    }

    private static Gson getChatGson() {
        Team.GsonAdapter teamAdapter = new Team.GsonAdapter() {
            @Override
            public JsonElement serialize(Team src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject result = super.serialize(src, typeOfSrc, context).getAsJsonObject();
                result.addProperty("_id", src.getId());
                return result;
            }
        };
        Chat.GsonAdapter chatAdapter = new Chat.GsonAdapter() {
            @Override
            public JsonElement serialize(Chat src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject result = super.serialize(src, typeOfSrc, context).getAsJsonObject();
                result.addProperty("_id", src.getId());
                return result;
            }
        };
        return new GsonBuilder()
                .registerTypeAdapter(Team.class, teamAdapter)
                .registerTypeAdapter(Chat.class, chatAdapter)
                .registerTypeAdapter(User.class, new User.GsonAdapter())
                .create();
    }
}
