package com.mainstreetcode.teammates.rest;

import com.google.gson.JsonObject;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * RESTful client implementation
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

public interface TeammateApi {

    // =============================================================================================
    // User endpoints
    // =============================================================================================

    @POST("api/signUp")
    Observable<User> signUp(@Body User user);

    @POST("api/signIn")
    Observable<User> signIn(@Body JsonObject request);

    @GET("api/me")
    Observable<User> getMe();

    @GET("api/signOut")
    Observable<JsonObject> signOut();

    // =============================================================================================
    // Team endpoints
    // =============================================================================================

    @POST("api/teams")
    Observable<Team> createTeam(@Body Team team);

    @GET("api/teams/{id}")
    Observable<Team> getTeam(@Path("id") String teamId);

    @PUT("api/teams/{id}")
    Observable<Team> updateTeam(@Path("id") String teamId, @Body Team team);

    @Multipart
    @POST("api/teams/{id}")
    Observable<Team> uploadTeamLogo(@Path("id") String teamId, @Part MultipartBody.Part file);

    @DELETE("api/teams/{id}")
    Observable<Team> deleteTeam(@Path("id") String teamId);

    @GET("api/teams/{id}/join")
    Observable<JoinRequest> joinTeam(@Path("id") String teamId, @Query("role") String role);

    @PUT("api/teams/{teamId}/user/{userId}")
    Observable<User> updateTeamUser(@Path("teamId") String teamId, @Path("userId") String userId,
                                    @Body User user);

    @GET("api/teams/{teamId}/user/{userId}/approve")
    Observable<JoinRequest> approveUser(@Path("teamId") String teamId, @Path("userId") String userId);

    @GET("api/teams/{teamId}/user/{userId}/decline")
    Observable<JoinRequest> declineUser(@Path("teamId") String teamId, @Path("userId") String userId);

    @GET("api/teams/{teamId}/user/{userId}/drop")
    Observable<User> dropUser(@Path("teamId") String teamId, @Path("userId") String userId);

    @Multipart
    @POST("api/teams/{teamId}/user/{userId}")
    Observable<User> uploadUserPhoto(@Path("teamId") String teamId, @Path("userId") String userId, @Part MultipartBody.Part file);

    @GET("api/me/teams")
    Observable<List<Team>> getMyTeams();

    @GET("api/teams")
    Observable<List<Team>> findTeam(@Query("name") String teamName);

    // =============================================================================================
    // Role endpoints
    // =============================================================================================

    @GET("api/roles/values")
    Observable<List<String>> getRoleValues();

    // =============================================================================================
    // Event endpoints
    // =============================================================================================

    @GET("api/events")
    Observable<List<Event>> getEvents();
}
