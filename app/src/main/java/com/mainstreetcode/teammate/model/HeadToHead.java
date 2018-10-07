package com.mainstreetcode.teammate.model;

import android.arch.persistence.room.Ignore;

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
import com.mainstreetcode.teammate.model.enums.TournamentType;
import com.mainstreetcode.teammate.util.IdCache;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class HeadToHead {

    public static class Request {

        @Ignore private static final IdCache holder = IdCache.cache(2);

        private Sport sport;
        private Competitor home;
        private Competitor away;
        private TournamentType type;

        private final List<Identifiable> items;

        private Request(Competitor home, Competitor away, TournamentType type, Sport sport) {
            this.home = home;
            this.away = away;
            this.type = type;
            this.sport = sport;
            items = buildItems();
        }

        public static Request empty() {
            return new Request(Competitor.empty(), Competitor.empty(), Config.tournamentTypeFromCode(""), Config.sportFromCode(""));
        }

        public boolean hasInvalidType() { return type.isInvalid(); }

        String getHomeId() { return home.getEntity().getId(); }

        String getAwayId() { return away.getEntity().getId(); }

        public String getRefPath() { return type.getRefPath(); }

        public Sport getSport() { return sport; }

        public void setSport(String sport) { this.sport = Config.sportFromCode(sport); }

        public void setType(String type) { this.type = Config.tournamentTypeFromCode(type); }

        public void updateHome(Competitive entity) {update(home, entity); }

        public void updateAway(Competitive entity) { update(away, entity); }

        private void update(Competitor competitor, Competitive entity) {
            competitor.updateEntity(entity);
        }

        public List<Identifiable> getItems() { return items; }

        private List<Identifiable> buildItems() {
            return Arrays.asList(
                    Item.text(holder.get(0), 0, Item.TOURNAMENT_TYPE, R.string.tournament_type, type::getCode, this::setType, this)
                            .textTransformer(value -> Config.tournamentTypeFromCode(value.toString()).getName()),
                    Item.text(holder.get(1), 1, Item.SPORT, R.string.team_sport, sport::getName, this::setSport, this)
                            .textTransformer(value -> Config.sportFromCode(value.toString()).getName()),
                    home,
                    away
            );
        }

        public static class GsonAdapter implements JsonSerializer<Request> {

            private static final String SPORT_KEY = "sport";
            private static final String HOME = "home";
            private static final String AWAY = "away";

            @Override
            public JsonElement serialize(Request src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject serialized = new JsonObject();

                serialized.add(HOME, context.serialize(src.home));
                serialized.add(AWAY, context.serialize(src.away));

                if (src.sport != null && !src.sport.isInvalid()) {
                    serialized.addProperty(SPORT_KEY, src.sport.getCode());
                }

                return serialized;
            }

        }
    }

    public static class Result {

        private final List<Aggregate> aggregates = new ArrayList<>();

        public Summary getSummary(Request request) {
            String homeId = request.getHomeId();
            String awayId = request.getAwayId();
            Summary summary = new Summary();

            for (Aggregate aggregate : aggregates) {
                if (isEmpty(aggregate.id)) summary.draws = aggregate.count;
                else if (aggregate.id.equals(homeId)) summary.wins = aggregate.count;
                else if (aggregate.id.equals(awayId)) summary.losses = aggregate.count;
            }

            return summary;
        }

        public static class GsonAdapter
                implements
                JsonDeserializer<Result> {

            private static final String ID = "_id";
            private static final String COUNT = "count";
            private static final String WINNER = "winner";

            @Override
            public Result deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

                JsonArray jsonArray = json.getAsJsonArray();
                Result result = new Result();

                for (JsonElement element : jsonArray) {
                    JsonObject object = element.getAsJsonObject();
                    int count = (int) ModelUtils.asFloat(COUNT, object);
                    String id = ModelUtils.asString(WINNER, object.get(ID).getAsJsonObject());

                    result.aggregates.add(new Aggregate(count, id));
                }

                return result;
            }
        }

    }

    public static class Summary {
        int wins;
        int draws;
        int losses;

        private Summary() {}

        public int getWins() { return wins; }

        public int getDraws() { return draws; }

        public int getLosses() { return losses; }
    }

    private static class Aggregate {
        int count;
        String id;

        Aggregate(int count, String id) {
            this.count = count;
            this.id = id;
        }
    }
}
