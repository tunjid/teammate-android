package com.mainstreetcode.teammates.repository;

import android.support.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.model.TeamChatRoom;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.TeamChatRoomDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.socket.SocketFactory;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
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

public class TeamChatRoomRepository extends ModelRespository<TeamChatRoom> {

    private static final String JOIN_EVENT = "join";
    private static final String NEW_MESSAGE_EVENT = "newMessage";
    private static final String ERROR_EVENT = "error";

    private static final Gson gson = TeammateService.getGson();

    private static TeamChatRoomRepository ourInstance;

    private final TeammateApi api;
    private final TeamChatRoomDao chatRoomDao;

    private TeamChatRoomRepository() {
        api = TeammateService.getApiInstance();
        chatRoomDao = AppDatabase.getInstance().teamChatRoomDao();
    }

    public static TeamChatRoomRepository getInstance() {
        if (ourInstance == null) ourInstance = new TeamChatRoomRepository();
        return ourInstance;
    }

    @Override
    public Single<TeamChatRoom> createOrUpdate(TeamChatRoom model) {
        return null;
    }

    @Override
    public Flowable<TeamChatRoom> get(String id) {
        Maybe<TeamChatRoom> local = chatRoomDao.get(id).subscribeOn(io());
        Maybe<TeamChatRoom> remote = api.getTeamChatRoom(id).map(getSaveFunction()).toMaybe();

        return cacheThenRemote(local, remote);
    }

    @Override
    public Single<TeamChatRoom> delete(TeamChatRoom model) {
        return null;
    }

    @Override
    Function<List<TeamChatRoom>, List<TeamChatRoom>> provideSaveManyFunction() {
        return new ChatRoomSaver();
    }

    public Flowable<List<TeamChatRoom>> getTeamChatRooms() {
        Maybe<List<TeamChatRoom>> local = chatRoomDao.getTeamChatRooms().subscribeOn(io());
        Maybe<List<TeamChatRoom>> remote = api.getTeamChatRooms().map(getSaveManyFunction()).toMaybe();

        return cacheThenRemote(local, remote);
    }

    public Flowable<TeamChat> listenForChat(TeamChatRoom chatRoom) {
        Socket socket = SocketFactory.getInstance().getTeamChatSocket();
        if (socket == null) return Flowable.error(new Exception("Null Socket"));

        JSONObject result = null;

        try { result = new JSONObject(gson.toJson(chatRoom));}
        catch (Exception e) {e.printStackTrace();}

        socket.emit(JOIN_EVENT, result);
        return Flowable.create(new ChatFlowable(socket), BackpressureStrategy.DROP).observeOn(mainThread());
    }

    public Completable post(TeamChat chat) {
        return Completable.create(new ChatCompletable(chat)).observeOn(mainThread());
    }

    @Nullable
    private static TeamChat parseChat(Object... args) {
        try {
            JSONObject chatJson = (JSONObject) args[0];
            return gson.fromJson(chatJson.toString(), TeamChat.class);
        }
        catch (Exception e) {
            return null;
        }
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
            TeamChat chat = TeamChatRoomRepository.parseChat(args);
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

            JSONObject result = new JSONObject(gson.toJson(chat));

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

            TeamChat teamChat = TeamChatRoomRepository.parseChat(args);
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

    private static final class ChatRoomSaver implements Function<List<TeamChatRoom>, List<TeamChatRoom>> {

        private TeamChatRoomDao chatRoomDao = AppDatabase.getInstance().teamChatRoomDao();
        private ModelRespository<Team> teamRepository = TeamRepository.getInstance();
        private ModelRespository<User> userRepository = UserRepository.getInstance();
        private ModelRespository<TeamChat> teamChatRepository = TeamChatRepository.getInstance();

        @Override
        public List<TeamChatRoom> apply(List<TeamChatRoom> chatRooms) throws Exception {
            List<Team> teams = new ArrayList<>(chatRooms.size());
            List<User> users = new ArrayList<>();

            List<TeamChat> chats = new ArrayList<>();

            FirebaseMessaging messaging = FirebaseMessaging.getInstance();
            for (TeamChatRoom chatRoom : chatRooms) {
                messaging.subscribeToTopic(chatRoom.getId());

                teams.add(chatRoom.getTeam());
                chats.addAll(chatRoom.getChats());
                for (TeamChat chat : chatRoom.getChats()) users.add(chat.getUser());
            }

            teamRepository.getSaveManyFunction().apply(teams);
            userRepository.getSaveManyFunction().apply(users);

            chatRoomDao.upsert(Collections.unmodifiableList(chatRooms));
            teamChatRepository.getSaveManyFunction().apply(chats);

            return chatRooms;
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
