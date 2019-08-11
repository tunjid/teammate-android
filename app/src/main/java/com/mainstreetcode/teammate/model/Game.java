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

import androidx.room.Ignore;
import android.os.Parcel;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.persistence.entity.GameEntity;
import com.mainstreetcode.teammate.util.IdCache;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING;
import static com.mainstreetcode.teammate.util.ModelUtils.areNotEmpty;

/**
 * Event events
 */

public class Game extends GameEntity
        implements
        TeamHost,
        Model<Game>,
        HeaderedModel<Game>,
        ListableModel<Game> {

    @Ignore private static final IdCache holder = IdCache.cache(5);

    public Game(@NonNull String id, String name, String refPath, String score, String matchUp,
                String homeEntityId, String awayEntityId, String winnerEntityId,
                Date created, Sport sport, User referee, Team host, Event event, Tournament tournament,
                Competitor home, Competitor away, Competitor winner,
                int seed, int leg, int round, int homeScore, int awayScore,
                boolean ended, boolean canDraw) {
        super(id, name, refPath, score, matchUp, homeEntityId, awayEntityId, winnerEntityId, created, sport, referee, host, event, tournament, home, away, winner, seed, leg, round, homeScore, awayScore, ended, canDraw);
    }

    public static Game empty(Team team) {
        Sport sport = team.getSport();
        return new Game("", "", sport.refType(), "TBD", "", "", "", "",
                new Date(), sport, User.empty(), team, Event.empty(), Tournament.empty(),
                Competitor.empty(), Competitor.empty(), Competitor.empty(), 0, 0, 0, 0, 0, false, true);
    }

    public static Game withId(String id) {
        Game empty = empty(Team.empty());
        empty.setId(id);
        return empty;
    }

    protected Game(Parcel in) {
        super(in);
    }

    @Override
    public List<Item<Game>> asItems() {
        return Arrays.asList(
                Item.Companion.text(holder.get(0), 0, Item.NUMBER, R.string.game_competitors, this::getName, ignored -> {}, this),
                Item.Companion.number(holder.get(1), 1, Item.INPUT, R.string.game_home_score, () -> String.valueOf(getHomeScore()), this::setHomeScore, this),
                Item.Companion.number(holder.get(2), 2, Item.INPUT, R.string.game_away_score, () -> String.valueOf(getAwayScore()), this::setAwayScore, this),
                Item.Companion.number(holder.get(3), 3, Item.NUMBER, R.string.game_round, () -> String.valueOf(getRound()), ignored -> {}, this),
                Item.Companion.number(holder.get(4), 4, Item.NUMBER, R.string.game_leg, () -> String.valueOf(getLeg()), ignored -> {}, this)
        );
    }

    @Override
    public Item<Game> getHeaderItem() {
        return Item.Companion.text(EMPTY_STRING, 0, Item.IMAGE, R.string.team_logo, () -> "", url -> {}, this);
    }

    @Override
    public boolean areContentsTheSame(Differentiable other) {
        if (!(other instanceof Game)) return getId().equals(other.getId());
        Game casted = (Game) other;
        return getScore().equals(casted.getScore())
                && getHome().areContentsTheSame(casted.getHome())
                && getAway().areContentsTheSame(casted.getAway());
    }

    @Override
    public boolean hasMajorFields() {
        return areNotEmpty(getId(), getRefPath(), getScore()) && getHome().hasMajorFields() && getAway().hasMajorFields();
    }

    @Override
    public Object getChangePayload(Differentiable other) {
        return other;
    }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(getId());
    }

    @Override
    public Team getTeam() { return getHost(); }

    @Override
    @SuppressWarnings("unchecked")
    public void update(Game updatedGame) {
        this.setId(updatedGame.getId());
        this.setScore(updatedGame.getScore());
        this.setMatchUp(updatedGame.getMatchUp());
        this.setCreated(updatedGame.getCreated());
        this.setLeg(updatedGame.getLeg());
        this.setSeed(updatedGame.getSeed());
        this.setRound(updatedGame.getRound());
        this.setHomeScore(updatedGame.getHomeScore());
        this.setAwayScore(updatedGame.getAwayScore());
        this.setEnded(updatedGame.isEnded());
        this.setCanDraw(updatedGame.getCanDraw());
        this.getSport().update(updatedGame.getSport());
        this.setHomeEntityId(updatedGame.getHomeEntityId());
        this.setAwayEntityId(updatedGame.getAwayEntityId());
        this.setWinnerEntityId(updatedGame.getWinnerEntityId());
        if (updatedGame.getReferee().hasMajorFields())
            this.getReferee().update(updatedGame.getReferee());
        if (updatedGame.getHost().hasMajorFields())
            this.getHost().update(updatedGame.getHost());
        if (updatedGame.getTournament().hasMajorFields())
            this.getTournament().update(updatedGame.getTournament());
        if (updatedGame.getHome().hasMajorFields() && this.getHome().hasSameType(updatedGame.getHome()))
            this.getHome().update(updatedGame.getHome());
        else this.setHome(updatedGame.getHome());
        if (updatedGame.getAway().hasMajorFields() && this.getAway().hasSameType(updatedGame.getAway()))
            this.getAway().update(updatedGame.getAway());
        else this.setAway(updatedGame.getAway());
        if (updatedGame.getWinner().hasMajorFields() && this.getWinner().hasSameType(updatedGame.getWinner()))
            this.getWinner().update(updatedGame.getWinner());
        else this.setWinner(updatedGame.getWinner());
        if (updatedGame.getEvent().hasMajorFields()) this.getEvent().update(updatedGame.getEvent());
    }

    @Override
    public int compareTo(@NonNull Game o) {
        int createdComparison = getCreated().compareTo(o.getCreated());

        return createdComparison != 0 ? createdComparison : getId().compareTo(o.getId());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Game> CREATOR = new Creator<Game>() {
        @Override
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        @Override
        public Game[] newArray(int size) {
            return new Game[size];
        }
    };

    public static class GsonAdapter
            implements
            JsonSerializer<Game>,
            JsonDeserializer<Game> {

        private static final String ID_KEY = "_id";
        private static final String NAME = "name";
        private static final String REF_PATH = "refPath";
        private static final String SCORE = "score";
        private static final String MATCH_UP = "matchUp";
        private static final String CREATED_KEY = "created";
        private static final String SPORT_KEY = "sport";
        private static final String REFEREE = "referee";
        private static final String EVENT = "event";
        private static final String TOURNAMENT = "tournament";
        private static final String HOST = "host";
        private static final String HOME_ENTITY_ID = "homeEntity";
        private static final String AWAY_ENTITY_ID = "awayEntity";
        private static final String WINNER_ENTITY_ID = "winnerEntity";
        private static final String HOME = "home";
        private static final String AWAY = "away";
        private static final String WINNER = "winner";
        private static final String LEG = "leg";
        private static final String SEED = "seed";
        private static final String ROUND = "round";
        private static final String HOME_SCORE = "homeScore";
        private static final String AWAY_SCORE = "awayScore";
        private static final String ENDED = "ended";
        private static final String CAN_DRAW = "canDraw";

        @Override
        public JsonElement serialize(Game src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject body = new JsonObject();
            body.addProperty(ENDED, src.isEnded());
            body.addProperty(REF_PATH, src.getRefPath());
            body.addProperty(HOME_SCORE, src.getHomeScore());
            body.addProperty(AWAY_SCORE, src.getAwayScore());
            body.addProperty(HOME, src.getHome().getEntity().getId());
            body.addProperty(AWAY, src.getAway().getEntity().getId());
            body.addProperty(REFEREE, src.getReferee().isEmpty() ? null : src.getReferee().getId());
            return body;
        }

        @Override
        public Game deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return new Game(json.getAsString(), "", "", "TBD", "", "", "", "",
                        new Date(), Sport.Companion.empty(), User.empty(), Team.empty(), Event.empty(), Tournament.empty(Team.empty()),
                        Competitor.empty(), Competitor.empty(), Competitor.empty(),
                        0, 0, 0, 0, 0, false, false);
            }

            JsonObject body = json.getAsJsonObject();

            String id = ModelUtils.asString(ID_KEY, body);
            String name = ModelUtils.asString(NAME, body);
            String refPath = ModelUtils.asString(REF_PATH, body);
            String score = ModelUtils.asString(SCORE, body);
            String matchUp = ModelUtils.asString(MATCH_UP, body);
            String homeEntityId = ModelUtils.asString(HOME_ENTITY_ID, body);
            String awayEntityId = ModelUtils.asString(AWAY_ENTITY_ID, body);
            String winnerEntityId = ModelUtils.asString(WINNER_ENTITY_ID, body);
            String created = ModelUtils.asString(CREATED_KEY, body);
            String sportCode = ModelUtils.asString(SPORT_KEY, body);

            int seed = (int) ModelUtils.asFloat(SEED, body);
            int leg = (int) ModelUtils.asFloat(LEG, body);
            int round = (int) ModelUtils.asFloat(ROUND, body);
            int homeScore = (int) ModelUtils.asFloat(HOME_SCORE, body);
            int awayScore = (int) ModelUtils.asFloat(AWAY_SCORE, body);
            boolean ended = ModelUtils.asBoolean(ENDED, body);
            boolean canDraw = ModelUtils.asBoolean(CAN_DRAW, body);

            Sport sport = Config.sportFromCode(sportCode);
            User referee = context.deserialize(body.get(REFEREE), User.class);
            Team host = context.deserialize(body.get(HOST), Team.class);
            Event event = context.deserialize(body.get(EVENT), Event.class);
            Tournament tournament = context.deserialize(body.get(TOURNAMENT), Tournament.class);
            Competitor home = context.deserialize(body.get(HOME), Competitor.class);
            Competitor away = context.deserialize(body.get(AWAY), Competitor.class);
            Competitor winner = body.has(WINNER) ? context.deserialize(body.get(WINNER), Competitor.class) : Competitor.empty();

            if (referee == null) referee = User.empty();
            if (host == null) host = Team.empty();
            if (event == null) event = Event.empty();
            if (home == null) home = Competitor.empty();
            if (away == null) away = Competitor.empty();
            if (tournament == null) tournament = Tournament.empty();

            return new Game(id, name, refPath, score, matchUp, homeEntityId, awayEntityId, winnerEntityId,
                    ModelUtils.parseDate(created), sport, referee, host, event, tournament,
                    home, away, winner, seed, leg, round, homeScore, awayScore, ended, canDraw);
        }
    }
}
