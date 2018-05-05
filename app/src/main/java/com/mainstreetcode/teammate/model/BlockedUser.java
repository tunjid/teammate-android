package com.mainstreetcode.teammate.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.model.enums.BlockReason;

import java.lang.reflect.Type;

@SuppressLint("ParcelCreator")
public class BlockedUser implements UserHost, TeamHost, Model<BlockedUser> {

    private String id;
    private final User user;
    private final Team team;
    private final BlockReason reason;

    private BlockedUser(String id, User user, Team team, BlockReason reason) {
        this.id = id;
        this.user = user;
        this.team = team;
        this.reason = reason;
    }

    public static BlockedUser block(User user, Team team, BlockReason reason) {
        return new BlockedUser("", user, team, reason);
    }

    public User getUser() {
        return user;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public void update(BlockedUser updated) {

    }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(id);
    }

    @Override
    public String getImageUrl() {
        return user.getImageUrl();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public int compareTo(@NonNull BlockedUser o) {
        return 0;
    }

    public static class GsonAdapter implements JsonSerializer<BlockedUser> {

        private static final String REASON_KEY = "reason";
        private static final String USER_KEY = "user";
        private static final String TEAM_KEY = "team";

        @Override
        public JsonElement serialize(BlockedUser src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(REASON_KEY, src.reason.getCode());
            serialized.addProperty(USER_KEY, src.user.getId());
            serialized.addProperty(TEAM_KEY, src.team.getId());

            return serialized;
        }
    }
}
