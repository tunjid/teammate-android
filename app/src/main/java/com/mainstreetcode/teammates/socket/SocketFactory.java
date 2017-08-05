package com.mainstreetcode.teammates.socket;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mainstreetcode.teammates.Application;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.Transport;

import static android.content.Context.MODE_PRIVATE;
import static com.mainstreetcode.teammates.rest.TeammateService.API_BASE_URL;
import static com.mainstreetcode.teammates.rest.TeammateService.SESSION_COOKIE;
import static com.mainstreetcode.teammates.rest.TeammateService.SESSION_PREFS;
import static io.socket.client.Manager.EVENT_TRANSPORT;
import static io.socket.engineio.client.Transport.EVENT_REQUEST_HEADERS;


public class SocketFactory {

    private static SocketFactory INSTANCE;
    private Application app = Application.getInstance();

    public static SocketFactory getInstance() {
        if (INSTANCE == null) INSTANCE = new SocketFactory();
        return INSTANCE;
    }

    private SocketFactory() {}

    @Nullable
    public Socket get() {
        Socket socket = null;

        try {socket = IO.socket(API_BASE_URL);}
        catch (URISyntaxException e) {e.printStackTrace();}

        if (socket != null) {
            socket.io().on(EVENT_TRANSPORT, this::routeTransportEvent);
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
        headers.put("Cookie", Collections.singletonList(serializedCookie));
    }
}