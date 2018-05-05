package com.mainstreetcode.teammate.rest;

import com.facebook.login.LoginResult;
import com.google.gson.JsonObject;
import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Device;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.EventSearchRequest;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamMember;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.notifications.FeedItem;

import java.util.Date;
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
 */

public interface TeammateApi {

    String ID_PATH = "id";
    String TEAM_PATH = "teamId";
    String DATE_QUERY = "date";

    @GET("api/config")
    Single<Config> getConfig();

    // =============================================================================================
    // User endpoints
    // =============================================================================================

    @POST("api/signUp")
    Single<User> signUp(@Body User user);

    @POST("api/signIn")
    Single<User> signIn(@Body JsonObject request);

    @POST("api/signIn")
    Single<User> signIn(@Body LoginResult loginResult);

    @PUT("api/users/{id}")
    Single<User> updateUser(@Path(ID_PATH) String userId, @Body User user);

    @Multipart
    @POST("api/users/{id}")
    Single<User> uploadUserPhoto(@Path(ID_PATH) String userId, @Part MultipartBody.Part file);

    @GET("api/me")
    Single<User> getMe();

    @GET("api/signOut")
    Single<JsonObject> signOut(@Query("device") String currentDevice);

    @GET("api/me/feed")
    Single<List<FeedItem>> getFeed();

    @POST("api/forgotPassword")
    Single<Message> forgotPassword(@Body JsonObject json);

    @POST("api/resetPassword")
    Single<Message> resetPassword(@Body JsonObject json);

    // =============================================================================================
    // Team endpoints
    // =============================================================================================

    @POST("api/teams")
    Single<Team> createTeam(@Body Team team);

    @GET("api/teams/{id}")
    Single<Team> getTeam(@Path(ID_PATH) String teamId);

    @PUT("api/teams/{id}")
    Single<Team> updateTeam(@Path(ID_PATH) String teamId, @Body Team team);

    @Multipart
    @POST("api/teams/{id}")
    Single<Team> uploadTeamLogo(@Path(ID_PATH) String teamId, @Part MultipartBody.Part file);

    @DELETE("api/teams/{id}")
    Single<Team> deleteTeam(@Path(ID_PATH) String teamId);

    @GET("api/teams")
    Single<List<Team>> findTeam(@Query("name") String teamName);

    @GET("api/teams/{id}/members")
    Single<List<TeamMember>> getTeamMembers(@Path(ID_PATH) String teamId, @Query(DATE_QUERY) Date date);

    @POST("api/teams/{id}/block")
    Single<JsonObject> blockUser(@Path(ID_PATH) String teamId, @Body BlockedUser blockedUser);

    // =============================================================================================
    // Role endpoints
    // =============================================================================================

    @GET("api/me/roles")
    Single<List<Role>> getMyRoles();

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

    @POST("api/join-requests/invite")
    Single<JoinRequest> inviteUser(@Body JoinRequest joinRequest);

    @GET("api/join-requests/{requestId}")
    Single<JoinRequest> getJoinRequest(@Path("requestId") String requestId);

    @GET("api/join-requests/{requestId}/approve")
    Single<Role> approveUser(@Path("requestId") String requestId);

    @GET("api/join-requests/{requestId}/accept")
    Single<Role> acceptInvite(@Path("requestId") String requestId);

    @DELETE("api/join-requests/{requestId}")
    Single<JoinRequest> deleteJoinRequest(@Path("requestId") String requestId);

    // =============================================================================================
    // Event endpoints
    // =============================================================================================

    @GET("/api/teams/{teamId}/events")
    Single<List<Event>> getEvents(@Path(TEAM_PATH) String teamId, @Query(DATE_QUERY) Date date);

    @POST("/api/events/public")
    Single<List<Event>> getPublicEvents(@Body EventSearchRequest request);

    @POST("api/events")
    Single<Event> createEvent(@Body Event event);

    @Multipart
    @POST("api/events/{id}")
    Single<Event> uploadEventPhoto(@Path(ID_PATH) String eventId, @Part MultipartBody.Part file);

    @PUT("api/events/{id}")
    Single<Event> updateEvent(@Path(ID_PATH) String eventId, @Body Event event);

    @GET("api/events/{id}")
    Single<Event> getEvent(@Path(ID_PATH) String eventId);

    @GET("api/events/{id}/guests")
    Single<List<Guest>> getEventGuests(@Path(ID_PATH) String eventId, @Query(DATE_QUERY) Date date);

    @DELETE("api/events/{id}")
    Single<Event> deleteEvent(@Path(ID_PATH) String eventId);

    @GET("api/events/{id}/rsvpGuest")
    Single<Guest> rsvpEvent(@Path(ID_PATH) String eventId, @Query("attending") boolean attending);

    // =============================================================================================
    // Team Chat endpoints
    // =============================================================================================

    @GET("/api/chats/{id}")
    Single<Chat> getTeamChat(@Path(ID_PATH) String chatId);

    @DELETE("/api/chats/{id}")
    Single<Chat> deleteChat(@Path(ID_PATH) String chatId);

    @GET("/api/teams/{teamId}/chats")
    Single<List<Chat>> chatsBefore(@Path(TEAM_PATH) String teamId, @Query(DATE_QUERY) Date date);

    // =============================================================================================
    // Team Media endpoints
    // =============================================================================================

    @GET("/api/media/{mediaId}")
    Single<Media> getMedia(@Path("mediaId") String mediaId);

    @GET("/api/media/{mediaId}/flag")
    Single<Media> flagMedia(@Path("mediaId") String mediaId);

    @DELETE("/api/media/{mediaId}")
    Single<Media> deleteMedia(@Path("mediaId") String mediaId);

    @GET("/api/teams/{teamId}/media")
    Single<List<Media>> getTeamMedia(@Path(TEAM_PATH) String teamId, @Query(DATE_QUERY) Date date);

    @Multipart
    @POST("api/teams/{teamId}/media")
    Single<Media> uploadTeamMedia(@Path(TEAM_PATH) String teamId, @Part MultipartBody.Part file);

    @POST("api/media/delete")
    Single<List<Media>> deleteMedia(@Body List<Media> delete);

    @POST("api/teams/{teamId}/media/delete")
    Single<List<Media>> adminDeleteMedia(@Path(TEAM_PATH) String teamId, @Body List<Media> delete);

    // =============================================================================================
    // Device endpoints
    // =============================================================================================

    @POST("api/me/devices")
    Single<Device> createDevice(@Body Device device);

    @PUT("api/me/devices/{id}")
    Single<Device> updateDevice(@Path(ID_PATH) String deviceId, @Body Device device);
}
