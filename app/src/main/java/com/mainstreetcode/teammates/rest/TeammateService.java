package com.mainstreetcode.teammates.rest;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.facebook.login.LoginResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammates.Application;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Chat;
import com.mainstreetcode.teammates.model.Device;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Guest;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Message;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.notifications.FeedItem;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.schedulers.Schedulers;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.text.TextUtils.isEmpty;

/**
 * Teammates RESTful API
 */

public class TeammateService {

    public static final String API_BASE_URL = "https://teammateapp.org/";
    public static final String SESSION_PREFS = "session.prefs";
    public static final String SESSION_COOKIE = "connect.sid";

    private static final Gson GSON = getGson();

    private static TeammateApi api;
    private static OkHttpClient httpClient;

    public static TeammateApi getApiInstance() {
        if (api == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .cookieJar(new SessionCookieJar());

            assignSSLSocketFactory(builder);

            httpClient = builder.build();

            api = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .client(httpClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .addConverterFactory(GsonConverterFactory.create(GSON))
                    .build()
                    .create(TeammateApi.class);
        }

        return api;
    }

    public static Gson getGson() {

        return new GsonBuilder()
                .registerTypeAdapter(Team.class, new Team.GsonAdapter())
                .registerTypeAdapter(User.class, new User.GsonAdapter())
                .registerTypeAdapter(Role.class, new Role.GsonAdapter())
                .registerTypeAdapter(Chat.class, new Chat.GsonAdapter())
                .registerTypeAdapter(Event.class, new Event.GsonAdapter())
                .registerTypeAdapter(Media.class, new Media.GsonAdapter())
                .registerTypeAdapter(Guest.class, new Guest.GsonAdapter())
                .registerTypeAdapter(Device.class, new Device.GsonAdapter())
                .registerTypeAdapter(Message.class, new Message.GsonAdapter())
                .registerTypeAdapter(FeedItem.class, new FeedItem.GsonAdapter())
                .registerTypeAdapter(JoinRequest.class, new JoinRequest.GsonAdapter())
                .registerTypeAdapter(LoginResult.class, (JsonSerializer<LoginResult>) (src, typeOfSrc, context) -> {
                    JsonObject body = new JsonObject();
                    body.addProperty("access_token", src.getAccessToken().getToken());
                    return body;
                })
                .create();
    }

    public static OkHttpClient getHttpClient() {
        if (api == null) getApiInstance();

        return httpClient;
    }

    private static void assignSSLSocketFactory(OkHttpClient.Builder builder) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream stream = Application.getInstance().getResources().openRawResource(R.raw.server);

            Certificate certificate = certificateFactory.generateCertificate(stream);
            stream.close();

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", certificate);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
            trustManagerFactory.init(keyStore);

            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }

            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
        }
        catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | KeyManagementException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private static class SessionCookieJar implements CookieJar {
        private static final String SIGN_IN_PATH = "/api/signIn";
        private static final String SIGN_UP_PATH = "/api/signUp";

        Application application = Application.getInstance();

        @Override
        public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
            String path = url.encodedPath();
            if (!path.equals(SIGN_UP_PATH) && !path.equals(SIGN_IN_PATH)) return;

            for (Cookie cookie : cookies) {
                if (cookie.name().equals(SESSION_COOKIE)) {
                    application.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
                            .edit().putString(SESSION_COOKIE, cookie.toString()).apply();
                    break;
                }
            }
        }

        @Override
        public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
            List<Cookie> cookies = new ArrayList<>();

            SharedPreferences preferences = application
                    .getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE);

            String serializedCookie = preferences.getString(SESSION_COOKIE, "");

            if (!isEmpty(serializedCookie)) cookies.add(Cookie.parse(url, serializedCookie));

            return cookies;
        }
    }
}
