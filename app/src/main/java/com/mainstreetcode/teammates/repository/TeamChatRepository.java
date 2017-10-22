package com.mainstreetcode.teammates.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mainstreetcode.teammates.Application;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.TeamChatDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.socket.SocketFactory;
import com.mainstreetcode.teammates.util.TeammateException;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.socket.client.Socket;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.computation;
import static io.reactivex.schedulers.Schedulers.io;

public class TeamChatRepository extends ModelRepository<TeamChat> {


    private static final String JOIN_EVENT = "join";
    private static final String NEW_MESSAGE_EVENT = "newMessage";
    private static final String ERROR_EVENT = "error";
    private static final String TEAM_SEEN_TIMES = "TeamRepository.team.seen.times";
    private static final int TEAM_NOT_SEEN = -1;

    private static final Gson CHAT_GSON = getChatGson();

    private static TeamChatRepository ourInstance;

    private final TeammateApi api;
    private final Application app;
    private final TeamChatDao chatDao;
    private final ModelRepository<User> userModelRespository;
    private final ModelRepository<Team> teamModelRespository;

    private TeamChatRepository() {
        app = Application.getInstance();
        api = TeammateService.getApiInstance();
        chatDao = AppDatabase.getInstance().teamChatDao();
        userModelRespository = UserRepository.getInstance();
        teamModelRespository = TeamRepository.getInstance();
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
            int size = chats.size();
            List<User> users = new ArrayList<>(size);
            List<Team> teams = new ArrayList<>(size);

            for (TeamChat chat : chats) {
                users.add(chat.getUser());
                teams.add(chat.getTeam());
            }

            userModelRespository.getSaveManyFunction().apply(users);
            teamModelRespository.getSaveManyFunction().apply(teams);

            chatDao.upsert(chats);
            return chats;
        };
    }

    public Flowable<List<TeamChat>> chatsBefore(Team team, Date date) {
        Maybe<List<TeamChat>> local = chatDao.chatsBefore(team.getId(), date).subscribeOn(io());
        Maybe<List<TeamChat>> remote = api.chatsBefore(team.getId(), date).map(getSaveManyFunction()).toMaybe();

        return cacheThenRemote(local, remote);
    }

    public Single<List<TeamChat>> fetchUnreadChats(Team team) {
        User currentUser = UserRepository.getInstance().getCurrentUser();
        return chatDao.unreadChats(team.getId(), currentUser, getLastTeamSeen(team))
                .subscribeOn(io())
                .observeOn(mainThread())
                .toSingle();
    }

    public Flowable<TeamChat> listenForChat(Team team) {
        Socket socket = SocketFactory.getInstance().getTeamChatSocket();
        if (socket == null) return Flowable.error(new Exception("Null Socket"));

        JSONObject result = null;

        try { result = new JSONObject(CHAT_GSON.toJson(team));}
        catch (Exception e) {e.printStackTrace();}

        socket.emit(JOIN_EVENT, result);
        return Flowable.create(new ChatFlowable(socket), BackpressureStrategy.DROP).observeOn(mainThread());
    }

    public Completable post(TeamChat chat) {
        return Completable.create(new ChatCompletable(chat)).observeOn(mainThread());
    }

    public void updateLastSeen(Team team) {
        SharedPreferences preferences = app.getSharedPreferences(TEAM_SEEN_TIMES, Context.MODE_PRIVATE);
        preferences.edit().putLong(team.getId(), new Date().getTime()).apply();
    }

    private Date getLastTeamSeen(Team team) {
        SharedPreferences preferences = app.getSharedPreferences(TEAM_SEEN_TIMES, Context.MODE_PRIVATE);
        long timeStamp = preferences.getLong(team.getId(), TEAM_NOT_SEEN);
        if (timeStamp == TEAM_NOT_SEEN) updateLastSeen(team);
        return timeStamp == TEAM_NOT_SEEN ? new Date() : new Date(timeStamp);
    }

    @Nullable
    private static TeamChat parseChat(Object... args) {
        try {return CHAT_GSON.fromJson(args[0].toString(), TeamChat.class);}
        catch (Exception e) {return null;}
    }

    private static Gson getChatGson() {
        Team.GsonAdapter adapter = new Team.GsonAdapter() {
            @Override
            public JsonElement serialize(Team src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject result = super.serialize(src, typeOfSrc, context).getAsJsonObject();
                result.addProperty("_id", src.getId());
                return result;
            }
        };
        return new GsonBuilder()
                .registerTypeAdapter(Team.class, adapter)
                .registerTypeAdapter(User.class, new User.GsonAdapter())
                .registerTypeAdapter(TeamChat.class, new TeamChat.GsonAdapter())
                .create();
    }

    private static final class ChatFlowable implements
            Cancellable,
            FlowableOnSubscribe<TeamChat> {

        private final Socket socket;
        private final User signedInUser;

        private FlowableEmitter<TeamChat> emitter;

        ChatFlowable(Socket socket) {
            this.socket = socket;
            signedInUser = UserRepository.getInstance().getCurrentUser();
            socket.on(NEW_MESSAGE_EVENT, this::parseChat);
            socket.on(ERROR_EVENT, this::handleError);
        }

        @Override
        public void subscribe(FlowableEmitter<TeamChat> flowableEmitter) throws Exception {
            flowableEmitter.setCancellable(this);
            this.emitter = flowableEmitter;
        }

        @Override
        public void cancel() throws Exception {
            socket.close();
        }

        private void parseChat(Object... args) {
            TeamChat chat = TeamChatRepository.parseChat(args);
            if (chat != null && !signedInUser.equals(chat.getUser()) && emitter != null)
                emitter.onNext(chat);
        }

        private void handleError(Object... args) {
            SocketFactory.getInstance().recreateTeamChatSocket();
            this.emitter.onError((Throwable) args[0]);
        }
    }

    private static final class ChatCompletable
            implements
            Cancellable,
            CompletableOnSubscribe {

        private volatile boolean isCancelled;

        private final Socket socket;
        private final TeamChat chat;
        private CompletableEmitter emitter;

        ChatCompletable(TeamChat chat) {
            this.socket = SocketFactory.getInstance().getTeamChatSocket();
            this.chat = chat;

            if (socket != null) {
                socket.on(NEW_MESSAGE_EVENT, this::parseChat);
                socket.on(ERROR_EVENT, this::handleError);
            }
        }

        @Override
        public void subscribe(CompletableEmitter emitter) throws Exception {
            emitter.setCancellable(this);
            this.emitter = emitter;

            JSONObject result = new JSONObject(CHAT_GSON.toJson(chat));

            if (socket == null) {
                handleError(new TimeoutException());
                return;
            }

            new Backoff(socket::connected,
                    () -> {
                        socket.connect();
                        socket.emit(NEW_MESSAGE_EVENT, new Object[]{result}, this::parseChat);
                    },
                    () -> handleError(new TimeoutException()));
        }

        private void parseChat(Object... args) {
            if (isCancelled) return;

            TeamChat teamChat = TeamChatRepository.parseChat(args);
            chat.update(teamChat);

            if (teamChat != null && emitter != null && !emitter.isDisposed()) emitter.onComplete();
        }

        private void handleError(Object... args) {
            SocketFactory.getInstance().recreateTeamChatSocket();
            if (emitter != null && !emitter.isDisposed()) this.emitter.onError((Throwable) args[0]);
        }

        @Override
        public void cancel() throws Exception {
            isCancelled = true;
        }
    }

    static class Backoff {

        static int MAX = 3;
        int elapsed;

        private Condition condition;
        private Runnable completeAction;
        private Runnable timeoutAction;

        Backoff(Condition condition, Runnable completeAction, Runnable timeoutAction) {
            this.condition = condition;
            this.completeAction = completeAction;
            this.timeoutAction = timeoutAction;

            start();
        }

        void start() {
            Observable.timer(1, TimeUnit.SECONDS)
                    .subscribeOn(computation())
                    .subscribe(nothing -> evaluate());
        }

        void evaluate() {
            elapsed++;

            if (elapsed > MAX) {
                timeoutAction.run();
                return;
            }

            if (condition.met()) completeAction.run();
            else start();
        }
    }

    interface Condition {
        boolean met();
    }

}
