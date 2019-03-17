package com.mainstreetcode.teammate.model;

import androidx.room.Ignore;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.model.enums.StatType;
import com.mainstreetcode.teammate.util.IdCache;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatAggregate {

    public static class Request {
        @Ignore private static final IdCache holder = IdCache.cache(1);

        private User user;
        private Team team;
        private Sport sport;

        private final List<Differentiable> items;

        private Request(User user, Team team, Sport sport) {
            this.user = user;
            this.team = team;
            this.sport = sport;
            items = buildItems();
        }

        public static StatAggregate.Request empty() {
            return new StatAggregate.Request(User.empty(), Team.empty(), Config.sportFromCode(""));
        }

        public Sport getSport() { return sport; }

        public void updateUser(User user) { this.user.update(user); }

        public void updateTeam(Team team) { this.team.update(team); }

        public void setSport(String sport) { this.sport = Config.sportFromCode(sport); }


        public List<Differentiable> getItems() { return items; }

        private List<Differentiable> buildItems() {
            return Arrays.asList(
                    Item.text(holder.get(0), 0, Item.SPORT, R.string.team_sport, sport::getName, this::setSport, this)
                            .textTransformer(value -> Config.sportFromCode(value.toString()).getName()),
                    user,
                    team
            );
        }

        public static class GsonAdapter implements JsonSerializer<Request> {

            private static final String SPORT_KEY = "sport";
            private static final String USER = "user";
            private static final String TEAM = "team";

            @Override
            public JsonElement serialize(StatAggregate.Request src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject serialized = new JsonObject();

                if (!src.user.isEmpty()) serialized.addProperty(USER, src.user.getId());
                if (!src.team.isEmpty()) serialized.addProperty(TEAM, src.team.getId());

                if (src.sport != null && !src.sport.isInvalid()) {
                    serialized.addProperty(SPORT_KEY, src.sport.getCode());
                }

                return serialized;
            }

        }
    }

    public static class Result {

        private final List<Aggregate> aggregates = new ArrayList<>();

        public List<Aggregate> getAggregates() { return aggregates; }

        public static class GsonAdapter
                implements
                JsonDeserializer<Result> {

            private static final String ID = "_id";
            private static final String COUNT = "count";

            @Override
            public Result deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

                JsonArray jsonArray = json.getAsJsonArray();
                Result result = new Result();

                for (JsonElement element : jsonArray) {
                    JsonObject object = element.getAsJsonObject();
                    int count = (int) ModelUtils.asFloat(COUNT, object);
                    StatType type = Config.statTypeFromCode(ModelUtils.asString(ID, object));

                    result.aggregates.add(new Aggregate(count, type));
                }

                return result;
            }
        }
    }

    public static class Aggregate implements Differentiable {
        int count;
        StatType type;

        Aggregate(int count, StatType type) {
            this.count = count;
            this.type = type;
        }

        public String getCount() { return String.valueOf(count); }

        public CharSequence getType() { return type.getEmojiAndName();}

        @Override
        public String getId() { return type.getId(); }
    }
}
