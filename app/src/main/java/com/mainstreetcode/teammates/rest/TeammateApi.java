package com.mainstreetcode.teammates.rest;

import com.google.gson.JsonObject;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
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

    @GET("api/me/teams")
    Observable<List<Team>> getMyTeams();

    @GET("api/teams")
    Observable<List<Team>> findTeam(@Query("name") String teamName);

    // =============================================================================================
    // Role endpoints
    // =============================================================================================

    @GET("api/roles/values")
    Observable<List<String>> getRoleValues();

    @Multipart
    @POST("api/roles/{roleId}")
    Observable<Role> uploadRolePhoto(@Path("roleId") String roleId, @Part MultipartBody.Part file);

    @PUT("api/roles/{roleId}")
    Observable<Role> updateRole(@Path("roleId") String roleId, @Body Role role);

    @DELETE("api/roles/{roleId}")
    Observable<Role> deleteRole(@Path("roleId") String roleId);

    // =============================================================================================
    // Join Request endpoints
    // =============================================================================================

    @POST("api/join-requests")
    Observable<JoinRequest> joinTeam(@Body JoinRequest joinRequest);

    @GET("api/join-requests/{requestId}/approve")
    Observable<Role> approveUser(@Path("requestId") String requestId);

    @GET("api/join-requests/{requestId}/decline")
    Observable<JoinRequest> declineUser(@Path("requestId") String requestId);

    // =============================================================================================
    // Event endpoints
    // =============================================================================================

    @GET("api/events")
    Observable<List<Event>> getEvents();

    @POST("api/events")
    Observable<Event> createEvent(@Body Event event);

    @Multipart
    @POST("api/events/{id}")
    Observable<Event> uploadEventPhoto(@Path("id") String eventId, @Part MultipartBody.Part file);

    @PUT("api/events/{id}")
    Observable<Event> updateEvent(@Path("id") String eventId, @Body Event event);

    @GET("api/events/{id}")
    Observable<Event> getEvent(@Path("id") String eventId);

    @DELETE("api/events/{id}")
    Observable<Event> deleteEvent(@Path("id") String eventId);
}
