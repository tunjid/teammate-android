package com.mainstreetcode.teammate.model;

import android.arch.persistence.room.Ignore;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.model.enums.TournamentType;
import com.mainstreetcode.teammate.util.IdCache;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class HeadToHeadRequest {

    @Ignore private static final IdCache holder = IdCache.cache(2);

    private Sport sport;
    private Competitor home;
    private Competitor away;
    private TournamentType type;

    private final List<Identifiable> items;

    private HeadToHeadRequest(Competitor home, Competitor away, TournamentType type, Sport sport) {
        this.home = home;
        this.away = away;
        this.type = type;
        this.sport = sport;
        items = buildItems();
    }

    public static HeadToHeadRequest empty() {
        return new HeadToHeadRequest(Competitor.empty(), Competitor.empty(), Config.tournamentTypeFromCode(""), Config.sportFromCode(""));
    }

    public boolean hasInvalidType() { return type.isInvalid(); }

    String getHomeId() { return home.getEntity().getId(); }

    String getAwayId() { return away.getEntity().getId(); }

    public String getRefPath() { return type.getRefPath(); }

    public Sport getSport() { return sport; }

    public void setSport(String sport) { this.sport = Config.sportFromCode(sport); }

    public void setType(String type) { this.type = Config.tournamentTypeFromCode(type); }

    public void updateHome(Competitive entity) { this.home.update(Competitor.empty(entity)); }

    public void updateAway(Competitive entity) { this.away.update(Competitor.empty(entity)); }

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

    public static class GsonAdapter implements JsonSerializer<HeadToHeadRequest> {

        private static final String SPORT_KEY = "sport";
        private static final String HOME = "home";
        private static final String AWAY = "away";

        @Override
        public JsonElement serialize(HeadToHeadRequest src, Type typeOfSrc, JsonSerializationContext context) {
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
