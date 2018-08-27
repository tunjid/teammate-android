package com.mainstreetcode.teammate.model;

import android.support.annotation.NonNull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;

/**
 * Roles on a team
 */

public class StatRank
        implements
        UserHost,
        TeamHost,
        RemoteImage,
        Identifiable,
        Comparable<StatRank> {


    private final int count;
    private final Team team;
    private final User user;

    StatRank(int count, Team team, User user) {
        this.count = count;
        this.team = team;
        this.user = user;
    }

    public CharSequence getTitle() { return user.getName(); }

    public CharSequence getSubtitle() { return team.getName(); }

    @Override
    public Team getTeam() { return team; }

    @Override
    public User getUser() { return user; }

    @Override
    public String getImageUrl() { return user.getImageUrl(); }

    @Override
    public String getId() { return user.getId() + "-" + team.getId(); }

    @Override
    public boolean areContentsTheSame(Identifiable other) {
        if (!(other instanceof StatRank)) return getId().equals(other.getId());
        StatRank casted = (StatRank) other;
        return user.areContentsTheSame(casted.getUser())
                && team.areContentsTheSame(casted.team);
    }

    @Override
    public Object getChangePayload(Identifiable other) {
        return other;
    }

    @Override
    public int compareTo(@NonNull StatRank o) {
        return Integer.compare(count, o.count);
    }

    public static class GsonAdapter
            implements
            JsonDeserializer<StatRank> {

        private static final String COUNT = "count";
        private static final String USER_KEY = "user";
        private static final String TEAM_KEY = "team";

        @Override
        public StatRank deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject roleJson = json.getAsJsonObject();

            int count = (int) ModelUtils.asFloat(COUNT, roleJson);
            Team team = context.deserialize(roleJson.get(TEAM_KEY), Team.class);
            User user = context.deserialize(roleJson.get(USER_KEY), User.class);

            if (user == null) user = User.empty();
            if (team == null) team = Team.empty();

            return new StatRank(count, team, user);
        }
    }
}
