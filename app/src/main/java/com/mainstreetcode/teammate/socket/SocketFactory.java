/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.socket;

import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.util.Logger;
import com.mainstreetcode.teammate.util.TeammateException;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Single;
import io.reactivex.processors.PublishProcessor;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.engineio.client.Transport;
import io.socket.engineio.client.transports.WebSocket;
import okhttp3.OkHttpClient;

import static android.content.Context.MODE_PRIVATE;
import static com.mainstreetcode.teammate.rest.TeammateService.API_BASE_URL;
import static com.mainstreetcode.teammate.rest.TeammateService.SESSION_COOKIE;
import static com.mainstreetcode.teammate.rest.TeammateService.SESSION_PREFS;
import static com.mainstreetcode.teammate.rest.TeammateService.getHttpClient;
import static io.socket.client.Manager.EVENT_OPEN;
import static io.socket.client.Manager.EVENT_TRANSPORT;
import static io.socket.client.Socket.EVENT_ERROR;
import static io.socket.client.Socket.EVENT_RECONNECT_ERROR;
import static io.socket.engineio.client.Transport.EVENT_REQUEST_HEADERS;


public class SocketFactory {

    private static final int RECONNECTION_ATTEMPTS = 3;
    private static final String TAG = "Socket.Factory";
    private static final String COOKIE = "Cookie";
    private static final String TEAM_CHAT_NAMESPACE = "/team-chat";
    public static final String EVENT_NEW_MESSAGE = "newMessage";
    public static final String EVENT_JOIN = "join";

    private static SocketFactory INSTANCE;

    private App app = App.Companion.getInstance();

    public static SocketFactory getInstance() {
        if (INSTANCE == null) INSTANCE = new SocketFactory();
        return INSTANCE;
    }

    private AtomicReference<Socket> teamChatSocket = new AtomicReference<>();

    private SocketFactory() {}

    public Single<Socket> getTeamChatSocket() {
        Socket current = teamChatSocket.get();

        if (isConnected(current)) return Single.just(current);
        if (current != null) return Single.timer(1200, TimeUnit.MILLISECONDS).map(ignored -> {
            Socket delayed = teamChatSocket.get();
            boolean isConnected = isConnected(delayed);

            if (!isConnected) teamChatSocket.set(null);

            if (isConnected) return delayed;
            else throw new TeammateException(app.getString(R.string.error_socket));
        });

        Socket pending = buildTeamChatSocket();
        if (pending == null) return Single.error(new TeammateException(app.getString(R.string.error_socket)));

        PublishProcessor<Socket> processor = PublishProcessor.create();

        pending.io().once(EVENT_OPEN, args -> {
            teamChatSocket.set(pending);
            processor.onNext(pending);
            processor.onComplete();
        });

        pending.once(EVENT_ERROR, this::onError);
        pending.connect();

        return processor.firstOrError();
    }

    @Nullable
    private Socket buildBaseSocket() {
        Socket socket = null;
        OkHttpClient client = getHttpClient();

        IO.setDefaultOkHttpWebSocketFactory(client);
        IO.setDefaultOkHttpCallFactory(client);

        IO.Options options = new IO.Options();
        options.secure = true;
        options.forceNew = true;
        options.reconnection = false;
        options.transports = new String[] {WebSocket.NAME};
        options.reconnectionAttempts = RECONNECTION_ATTEMPTS;

        try {socket = IO.socket(API_BASE_URL, options);}
        catch (URISyntaxException e) {Logger.INSTANCE.log(TAG, "Unable to build Socket", e);}

        return socket;
    }

    @Nullable
    private Socket buildTeamChatSocket() {
        Socket socket = buildBaseSocket();

        if (socket != null) {
            socket = socket.io().socket(TEAM_CHAT_NAMESPACE);

            Manager manager = socket.io();
            manager.on(EVENT_TRANSPORT, this::routeTransportEvent);
//            manager.on(EVENT_CLOSE, i -> onDisconnection());

            socket.on(EVENT_RECONNECT_ERROR, this::onReconnectionError);
            //socket.on(EVENT_RECONNECT_ATTEMPT, this::onReconnectionAttempt);
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

//    private void onReconnectionAttempt(Object... args) {}

    private void onReconnectionError(Object... args) {
        Logger.INSTANCE.log(TAG, "Reconnection Error", ((Exception) args[0]));
    }

    private void onError(Object... args) {
        Logger.INSTANCE.log(TAG, "Socket Error", ((Exception) args[0]));

        Socket socket = teamChatSocket.get();

        if (socket != null) {
            socket.off(EVENT_NEW_MESSAGE);
            socket.close();
        }

        teamChatSocket.set(null);
    }

    private boolean isConnected(@Nullable Socket socket) {
        return socket != null && socket.connected();
    }
}