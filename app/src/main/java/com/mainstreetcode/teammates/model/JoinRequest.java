package com.mainstreetcode.teammates.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Join request for a {@link Team}
 */

public class JoinRequest implements Parcelable {

    private boolean teamApproved;
    private boolean userApproved;
    private String roleName;
    private String teamId;
    private User user;

    JoinRequest(boolean teamApproved, boolean userApproved, String roleName, String teamId, User user) {
        this.teamApproved = teamApproved;
        this.userApproved = userApproved;
        this.roleName = roleName;
        this.teamId = teamId;
        this.user = user;
    }

    protected JoinRequest(Parcel in) {
        teamApproved = in.readByte() != 0x00;
        userApproved = in.readByte() != 0x00;
        roleName = in.readString();
        teamId = in.readString();
        user = (User) in.readValue(User.class.getClassLoader());
    }

    public boolean isTeamApproved() {
        return teamApproved;
    }

    public boolean isUserApproved() {
        return userApproved;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getTeamId() {
        return teamId;
    }

    public User getUser() {
        return user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (teamApproved ? 0x01 : 0x00));
        dest.writeByte((byte) (userApproved ? 0x01 : 0x00));
        dest.writeString(roleName);
        dest.writeString(teamId);
        dest.writeValue(user);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<JoinRequest> CREATOR = new Parcelable.Creator<JoinRequest>() {
        @Override
        public JoinRequest createFromParcel(Parcel in) {
            return new JoinRequest(in);
        }

        @Override
        public JoinRequest[] newArray(int size) {
            return new JoinRequest[size];
        }
    };

    public static class GsonAdapter
            implements
            JsonDeserializer<JoinRequest> {

        private static final String NAME_KEY = "roleName";
        private static final String USER_KEY = "user";
        private static final String TEAM_KEY = "team";
        private static final String TEAM_APPROVAL_KEY = "teamApproved";
        private static final String USER_APPROVAL_KEY = "userApproved";

        @Override
        public JoinRequest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject requestJson = json.getAsJsonObject();

            boolean teamApproved = ModelUtils.asBoolean(TEAM_APPROVAL_KEY, requestJson);
            boolean userApproved = ModelUtils.asBoolean(USER_APPROVAL_KEY, requestJson);

            String roleName = ModelUtils.asString(NAME_KEY, requestJson);
            String teamId = ModelUtils.asString(TEAM_KEY, requestJson);
            User user = context.deserialize(requestJson.get(USER_KEY), User.class);

            return new JoinRequest(teamApproved, userApproved, roleName, teamId, user);
        }
    }
}
