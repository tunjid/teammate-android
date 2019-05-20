/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.model;

import androidx.annotation.NonNull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.lang.reflect.Type;

/**
 * Roles on a team
 */

public class StatRank
        implements
        UserHost,
        TeamHost,
        RemoteImage,
        Differentiable,
        Comparable<StatRank> {


    private final int count;
    private final Team team;
    private final User user;

    StatRank(int count, Team team, User user) {
        this.count = count;
        this.team = team;
        this.user = user;
    }

    public String getInset() { return team.getImageUrl(); }

    public String getCount() { return String.valueOf(count); }

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
    public boolean areContentsTheSame(Differentiable other) {
        if (!(other instanceof StatRank)) return getId().equals(other.getId());
        StatRank casted = (StatRank) other;
        return user.areContentsTheSame(casted.getUser())
                && team.areContentsTheSame(casted.team);
    }

    @Override
    public Object getChangePayload(Differentiable other) {
        return other;
    }

    @Override
    public int compareTo(@NonNull StatRank o) {
        return -Integer.compare(count, o.count);
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
