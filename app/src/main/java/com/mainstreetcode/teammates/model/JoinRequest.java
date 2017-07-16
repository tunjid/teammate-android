package com.mainstreetcode.teammates.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Join request for a {@link Team}
 */

public class JoinRequest {

    public static final String DB_NAME = "joinRequests";

    public static final String TEAM_KEY = "teamId";
    public static final String USER_KEY = "memberId";
    public static final String ROLE_KEY = "roleId";

    boolean teamApproved;
    boolean userApproved;
    String roleName;
    String teamId;
    User user;

    JoinRequest(boolean teamApproved, boolean userApproved, String roleName, String teamId, User user) {
        this.teamApproved = teamApproved;
        this.userApproved = userApproved;
        this.roleName = roleName;
        this.teamId = teamId;
        this.user = user;
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
