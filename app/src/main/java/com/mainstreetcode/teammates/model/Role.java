package com.mainstreetcode.teammates.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammates.persistence.entity.TeamEntity;
import com.mainstreetcode.teammates.persistence.entity.UserEntity;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.lang.reflect.Type;

/**
 * Roles on a team
 * <p>
 * Created by Shemanigans on 6/6/17.
 */
@Entity(
        tableName = "roles",
        primaryKeys = {"userId", "teamId"},
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class, parentColumns = "user_id", childColumns = "userId"),
                @ForeignKey(entity = TeamEntity.class, parentColumns = "team_id", childColumns = "teamId")
        }
)
public class Role implements Parcelable {

    public static final String PHOTO_UPLOAD_KEY = "role-photo";
    private static final String ADMIN = "Admin";

    private String name;
    private String userId;
    private String teamId;
    private String imageUrl;

    @SuppressWarnings("unused")
    public Role(String name, String userId, String teamId, String imageUrl) {
        this.name = name;
        this.teamId = teamId;
        this.userId = userId;
        this.imageUrl = imageUrl;
    }

    public static class GsonAdapter
            implements
            JsonDeserializer<Role> {

        private static final String NAME_KEY = "name";
        private static final String USER_KEY = "user";
        private static final String TEAM_KEY = "team";
        private static final String IMAGE_KEY = "imageUrl";

        @Override
        public Role deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject roleJson = json.getAsJsonObject();

            String name = ModelUtils.asString(NAME_KEY, roleJson);
            String userId = ModelUtils.asString(USER_KEY, roleJson);
            String teamId = ModelUtils.asString(TEAM_KEY, roleJson);
            String imageUrl = TeammateService.API_BASE_URL + ModelUtils.asString(IMAGE_KEY, roleJson);

            return new Role(name, userId, teamId, imageUrl);
        }
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static boolean isTeamAdmin(Team team, User user) {
        int index = team.getUsers().indexOf(user);
        if (index == -1) return false;
        Role role = team.getUsers().get(index).getRole();
        return !(role == null || TextUtils.isEmpty(role.getName())) && ADMIN.equals(role.getName());
    }

    protected Role(Parcel in) {
        name = in.readString();
        userId = in.readString();
        teamId = in.readString();
        imageUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(userId);
        dest.writeString(teamId);
        dest.writeString(imageUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Role> CREATOR = new Parcelable.Creator<Role>() {
        @Override
        public Role createFromParcel(Parcel in) {
            return new Role(in);
        }

        @Override
        public Role[] newArray(int size) {
            return new Role[size];
        }
    };
}
