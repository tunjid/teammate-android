package com.mainstreetcode.teammates.rest;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mainstreetcode.teammates.Application;
import com.mainstreetcode.teammates.model.Message;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.schedulers.Schedulers;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Teammates RESTful API
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

public class TeammateService {

    public static final String API_BASE_URL = "http://10.0.2.2:3000/";
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Team.class, new Team.JsonDeserializer())
            .registerTypeAdapter(User.class, new User.JsonDeserializer())
            .registerTypeAdapter(Message.class, new Message.JsonDeserializer())
            .create();

    private static TeammateApi INSTANCE;

    public static TeammateApi getApiInstance() {
        if (INSTANCE == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .cookieJar(new SessionCookieJar())
                    .build();

            INSTANCE = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .client(client)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .addConverterFactory(GsonConverterFactory.create(GSON))
                    .build()
                    .create(TeammateApi.class);
        }

        return INSTANCE;
    }

    public static Gson getGson() {
        return GSON;
    }

    private static class SessionCookieJar implements CookieJar {

        private static final String SESSION_PREFS = "session.prefs";
        private static final String SESSION_COOKIE = "connect.sid";
        private static final String SIGN_IN_PATH = "/api/signIn";
        private static final String SIGN_UP_PATH = "/api/signUp";
        private static final String SIGN_OUT_PATH = "/api/signOut";

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

            // Delete cookies when signing out
            if (url.encodedPath().equals(SIGN_OUT_PATH)) {
                preferences.edit().remove(SESSION_COOKIE).apply();
            }

            String serializedCookie = preferences.getString(SESSION_COOKIE, "");

            if (!TextUtils.isEmpty(serializedCookie)) {
                cookies.add(Cookie.parse(url, serializedCookie));
            }

            return cookies;
        }
    }

}
