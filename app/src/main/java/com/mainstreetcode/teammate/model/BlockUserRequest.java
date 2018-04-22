package com.mainstreetcode.teammate.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.model.enums.BlockReason;

import java.lang.reflect.Type;

public class BlockUserRequest {

    private final User user;
    private final Team team;
    private final BlockReason reason;

    private BlockUserRequest(User user, Team team, BlockReason reason) {
        this.user = user;
        this.team = team;
        this.reason = reason;
    }

    public static BlockUserRequest block(User user, Team team, BlockReason reason) {
        return new BlockUserRequest(user, team, reason);
    }

    public User getUser() {
        return user;
    }

    public Team getTeam() {
        return team;
    }

    public static class GsonAdapter implements JsonSerializer<BlockUserRequest> {

        private static final String REASON_KEY = "reason";
        private static final String USER_KEY = "user";
        private static final String TEAM_KEY = "team";

        @Override
        public JsonElement serialize(BlockUserRequest src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(REASON_KEY, src.reason.getCode());
            serialized.addProperty(USER_KEY, src.user.getId());
            serialized.addProperty(TEAM_KEY, src.team.getId());

            return serialized;
        }
    }
}
