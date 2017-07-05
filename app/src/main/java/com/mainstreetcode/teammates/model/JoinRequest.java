package com.mainstreetcode.teammates.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import lombok.Getter;
import lombok.experimental.Builder;

/**
 * Join request for a {@link Team}
 */

@Getter
@Builder
public class JoinRequest {

    public static final String DB_NAME = "joinRequests";

    public static final String TEAM_KEY = "teamId";
    public static final String USER_KEY = "memberId";
    public static final String ROLE_KEY = "roleId";

    boolean teamApproved;
    boolean userApproved;
    String teamId;
    String userId;
    String roleName;

     JoinRequest(boolean teamApproved, boolean userApproved, String teamId, String userId, String roleName) {
        this.teamApproved = teamApproved;
        this.userApproved = userApproved;
        this.teamId = teamId;
        this.userId = userId;
        this.roleName = roleName;
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

            String userId = ModelUtils.asString(USER_KEY, requestJson);
            String teamId = ModelUtils.asString(TEAM_KEY, requestJson);
            String roleName = ModelUtils.asString(NAME_KEY, requestJson);

            return new JoinRequest(teamApproved, userApproved, teamId, userId, roleName);
        }
    }
}
