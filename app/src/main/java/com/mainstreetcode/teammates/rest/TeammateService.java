package com.mainstreetcode.teammates.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mainstreetcode.teammates.model.Message;
import com.mainstreetcode.teammates.model.User;

import io.reactivex.schedulers.Schedulers;
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

    private static final String API_BASE_URL = "http://10.0.2.2:3000/";
    private static final Gson GSON = new GsonBuilder()
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
}
