package com.mainstreetcode.teammates.rest;

import com.google.gson.JsonObject;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.FeedItem;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.TeamChatRoom;
import com.mainstreetcode.teammates.model.User;

import java.util.List;

import io.reactivex.Single;
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
    Single<User> signUp(@Body User user);

    @POST("api/signIn")
    Single<User> signIn(@Body JsonObject request);

    @GET("api/me")
    Single<User> getMe();

    @GET("api/signOut")
    Single<JsonObject> signOut();

    @GET("api/me/feed")
    Single<List<FeedItem>> getFeed();

    // =============================================================================================
    // Team endpoints
    // =============================================================================================

    @POST("api/teams")
    Single<Team> createTeam(@Body Team team);

    @GET("api/teams/{id}")
    Single<Team> getTeam(@Path("id") String teamId);

    @PUT("api/teams/{id}")
    Single<Team> updateTeam(@Path("id") String teamId, @Body Team team);

    @Multipart
    @POST("api/teams/{id}")
    Single<Team> uploadTeamLogo(@Path("id") String teamId, @Part MultipartBody.Part file);

    @DELETE("api/teams/{id}")
    Single<Team> deleteTeam(@Path("id") String teamId);

    @GET("api/me/teams")
    Single<List<Team>> getMyTeams();

    @GET("api/teams")
    Single<List<Team>> findTeam(@Query("name") String teamName);

    // =============================================================================================
    // Role endpoints
    // =============================================================================================

    @GET("api/roles/values")
    Single<List<String>> getRoleValues();

    @Multipart
    @POST("api/roles/{roleId}")
    Single<Role> uploadRolePhoto(@Path("roleId") String roleId, @Part MultipartBody.Part file);

    @PUT("api/roles/{roleId}")
    Single<Role> updateRole(@Path("roleId") String roleId, @Body Role role);

    @DELETE("api/roles/{roleId}")
    Single<Role> deleteRole(@Path("roleId") String roleId);

    // =============================================================================================
    // Join Request endpoints
    // =============================================================================================

    @POST("api/join-requests")
    Single<JoinRequest> joinTeam(@Body JoinRequest joinRequest);

    @GET("api/join-requests/{requestId}/approve")
    Single<Role> approveUser(@Path("requestId") String requestId);

    @GET("api/join-requests/{requestId}/decline")
    Single<JoinRequest> declineUser(@Path("requestId") String requestId);

    // =============================================================================================
    // Event endpoints
    // =============================================================================================

    @GET("api/events")
    Single<List<Event>> getEvents();

    @POST("api/events")
    Single<Event> createEvent(@Body Event event);

    @Multipart
    @POST("api/events/{id}")
    Single<Event> uploadEventPhoto(@Path("id") String eventId, @Part MultipartBody.Part file);

    @PUT("api/events/{id}")
    Single<Event> updateEvent(@Path("id") String eventId, @Body Event event);

    @GET("api/events/{id}")
    Single<Event> getEvent(@Path("id") String eventId);

    @DELETE("api/events/{id}")
    Single<Event> deleteEvent(@Path("id") String eventId);

    @GET("api/events/{id}/rsvp")
    Single<Event> rsvpEvent(@Path("id") String eventId, @Query("attending") boolean attending);

    // =============================================================================================
    // Team Chat endpoints
    // =============================================================================================

    @GET("/api/team-chats/{id}")
    Single<TeamChatRoom> getTeamChatRoom(@Path("id") String roomId);

    @GET("/api/team-chats")
    Single<List<TeamChatRoom>> getTeamChatRooms();
}