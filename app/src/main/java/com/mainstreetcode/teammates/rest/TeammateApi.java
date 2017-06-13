package com.mainstreetcode.teammates.rest;

import com.google.gson.JsonObject;
import com.mainstreetcode.teammates.model.User;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * RESTful client implementation
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

public interface TeammateApi {
    @POST("api/signUp")
    Observable<User> signUp(@Body User user);

    @POST("api/signIn")
    Observable<User> signIn(@Body JsonObject request);

    @GET("api/signOut")
    Observable<JsonObject> signOut();
}
