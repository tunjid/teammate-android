package com.mainstreetcode.teammates.repository;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.model.TeamChatRoom;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.socket.SocketFactory;

import org.json.JSONObject;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.socket.client.Socket;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class TeamChatRoomRepository extends CrudRespository<TeamChatRoom> {

    private static final String JOIN_EVENT = "join";
    private static final String NEW_MESSAGE_EVENT = "newMessage";
    private static final String ERROR_EVENT = "error";

    private static final Gson gson = TeammateService.getGson();

    private static TeamChatRoomRepository ourInstance;

    private final TeammateApi api;

    private TeamChatRoomRepository() {
        api = TeammateService.getApiInstance();
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
        return api.getTeamChatRoom(id).toFlowable().observeOn(mainThread());
    }

    @Override
    public Single<TeamChatRoom> delete(TeamChatRoom model) {
        return null;
    }

    @Override
    Function<List<TeamChatRoom>, List<TeamChatRoom>> provideSaveManyFunction() {
        return null;
    }

    public Flowable<List<TeamChatRoom>> getTeamChatRooms() {
        return api.getTeamChatRooms().toFlowable().observeOn(mainThread());
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
        Socket socket = SocketFactory.getInstance().getTeamChatSocket();

        if (socket == null) {
            return Completable.error(new Exception("Could not establish connection"));
        }
        return Completable.create(new ChatCompletable(socket, chat)).observeOn(mainThread());
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
            this.emitter.onError((Throwable) args[0]);
            socket.close();
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

        ChatCompletable(Socket socket, TeamChat chat) {
            this.socket = socket;
            this.chat = chat;
            socket.on(NEW_MESSAGE_EVENT, this::parseChat);
            socket.on(ERROR_EVENT, this::handleError);
        }

        @Override
        public void subscribe(CompletableEmitter emitter) throws Exception {
            emitter.setCancellable(this);
            this.emitter = emitter;

            JSONObject result = null;

            try { result = new JSONObject(gson.toJson(chat));}
            catch (Exception e) {e.printStackTrace();}

            socket.emit(NEW_MESSAGE_EVENT, new Object[]{result}, this::parseChat);
        }

        private void parseChat(Object... args) {
            if (isCancelled) return;

            TeamChat teamChat = TeamChatRoomRepository.parseChat(args);
            chat.update(teamChat);
            if (teamChat != null && emitter != null) emitter.onComplete();
        }

        private void handleError(Object... args) {
            this.emitter.onError((Throwable) args[0]);
        }

        @Override
        public void cancel() throws Exception {
            isCancelled = true;
        }
    }
}
