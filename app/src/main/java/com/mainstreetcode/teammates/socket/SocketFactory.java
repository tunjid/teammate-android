package com.mainstreetcode.teammates.socket;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.mainstreetcode.teammates.Application;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.engineio.client.Transport;

import static android.content.Context.MODE_PRIVATE;
import static com.mainstreetcode.teammates.rest.TeammateService.API_BASE_URL;
import static com.mainstreetcode.teammates.rest.TeammateService.SESSION_COOKIE;
import static com.mainstreetcode.teammates.rest.TeammateService.SESSION_PREFS;
import static io.socket.client.Manager.EVENT_CLOSE;
import static io.socket.client.Manager.EVENT_TRANSPORT;
import static io.socket.client.Socket.EVENT_ERROR;
import static io.socket.client.Socket.EVENT_RECONNECT_ATTEMPT;
import static io.socket.client.Socket.EVENT_RECONNECT_ERROR;
import static io.socket.engineio.client.Transport.EVENT_REQUEST_HEADERS;


public class SocketFactory {

    private static final int RECONNECTION_ATTEMPTS = 3;
    private static final String TAG = "Socket Factory";
    private static final String COOKIE = "Cookie";
    private static final String TEAM_CHAT_NAMESPACE = "/team-chat";

    private static SocketFactory INSTANCE;

    private Application app = Application.getInstance();

    public static SocketFactory getInstance() {
        if (INSTANCE == null) INSTANCE = new SocketFactory();
        return INSTANCE;
    }

    private Socket teamChatSocket = buildTeamChatSocket();

    private SocketFactory() {}

    @Nullable
    public Socket getTeamChatSocket() {
        return teamChatSocket;
    }

    public void recreateTeamChatSocket() {
        teamChatSocket.close();
        teamChatSocket = buildTeamChatSocket();
    }

    @Nullable
    private Socket buildBaseSocket() {
        Socket socket = null;

        IO.Options options = new IO.Options();
        options.forceNew = false;
        options.reconnection = true;
        options.reconnectionAttempts = RECONNECTION_ATTEMPTS;

        try {socket = IO.socket(API_BASE_URL, options);}
        catch (URISyntaxException e) {e.printStackTrace();}

        return socket;
    }

    @Nullable
    private Socket buildTeamChatSocket() {
        Socket socket = buildBaseSocket();

        if (socket != null) {
            socket = socket.io().socket(TEAM_CHAT_NAMESPACE);

            Manager manager = socket.io();
            manager.on(EVENT_TRANSPORT, this::routeTransportEvent);
            manager.on(EVENT_CLOSE, i -> onDisconnection());

            socket.on(EVENT_ERROR, this::onError);
            socket.on(EVENT_RECONNECT_ERROR, this::onReconnectionError);
            socket.on(EVENT_RECONNECT_ATTEMPT, this::onReconnectionAttempt);
            //socket.on(EVENT_DISCONNECT, i -> onDisconnection());
            //socket.on(EVENT_CLOSE, i -> onDisconnection());

            socket.connect();
        }
        return socket;
    }

    private void routeTransportEvent(Object... transportArgs) {
        Transport transport = (Transport) transportArgs[0];
        transport.on(EVENT_REQUEST_HEADERS, this::routeHeaderRequestEvent);
    }

    private void routeHeaderRequestEvent(Object... headerArgs) {

        SharedPreferences preferences = app.getSharedPreferences(SESSION_PREFS, MODE_PRIVATE);
        String serializedCookie = preferences.getString(SESSION_COOKIE, "");

        if (TextUtils.isEmpty(serializedCookie)) return;

        @SuppressWarnings("unchecked")
        Map<String, List<String>> headers = (Map<String, List<String>>) headerArgs[0];

        // modify request headers
        headers.put(COOKIE, Collections.singletonList(serializedCookie));
    }

    private void onReconnectionAttempt(Object... args) {
        Log.i(TAG, "Reconnection attempt: " + args[0]);
    }

    private void onReconnectionError(Object... args) {
        Log.i(TAG, "Error reconnecting: " + args[0]);
        ((Exception) args[0]).printStackTrace();
    }

    private void onError(Object... args) {
        Log.i(TAG, "Socket Error: " + args[0]);
        ((Exception) args[0]).printStackTrace();
        teamChatSocket = buildTeamChatSocket();
    }

    private void onDisconnection() {
        teamChatSocket = buildTeamChatSocket();
    }
}