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

import android.os.Parcel;
import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.model.enums.TournamentStyle;
import com.mainstreetcode.teammate.model.enums.TournamentType;
import com.mainstreetcode.teammate.persistence.entity.TournamentEntity;
import com.mainstreetcode.teammate.util.IdCache;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Ignore;

import static com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING;
import static com.mainstreetcode.teammate.util.ModelUtils.areNotEmpty;

/**
 * Event events
 */

public class Tournament extends TournamentEntity
        implements
        TeamHost,
        Model<Tournament>,
        HeaderedModel<Tournament>,
        ListableModel<Tournament> {

    public static final String PHOTO_UPLOAD_KEY = "tournament-photo";

    @Ignore private static final IdCache holder = IdCache.cache(6);

    public static Tournament empty() { return empty(Team.empty()); }

    public static Tournament empty(Team host) {
        Date date = new Date();
        Sport sport = host.getSport();
        return new Tournament("", Config.getDefaultTournamentLogo(), "", "", "", date, host, sport,
                sport.defaultTournamentType(), sport.defaultTournamentStyle(), Competitor.empty(),
                1, 1, 0, 0, false);
    }

    public static Tournament withId(String id) {
        Tournament empty = empty();
        empty.setId(id);
        return empty;
    }

    public Tournament(@NonNull String id, String imageUrl, String refPath,
                      CharSequence name, CharSequence description,
                      Date created, Team host, Sport sport, TournamentType type, TournamentStyle style,
                      Competitor winner,
                      int numLegs, int numRounds, int currentRound, int numCompetitors,
                      boolean singleFinal) {
        super(id, imageUrl, refPath, name, description, created, host, sport, type, style, winner, numLegs, numRounds, currentRound, numCompetitors, singleFinal);
    }

    protected Tournament(Parcel in) {
        super(in);
    }

    @Override
    public List<Item<Tournament>> asItems() {
        return Arrays.asList(
                Item.Companion.text(holder.get(0), 0, Item.INPUT, R.string.tournament_name, Item.Companion.nullToEmpty(getName()), this::setName, this),
                Item.Companion.text(holder.get(1), 1, Item.DESCRIPTION, R.string.tournament_description, Item.Companion.nullToEmpty(getDescription()), this::setDescription, this),
                Item.Companion.text(holder.get(2), 2, Item.TOURNAMENT_TYPE, R.string.tournament_type, getType()::getCode, this::setType, this)
                        .textTransformer(value -> Config.tournamentTypeFromCode(value.toString()).getName()),
                Item.Companion.text(holder.get(3), 3, Item.TOURNAMENT_STYLE, R.string.tournament_style, getStyle()::getCode, this::setStyle, this)
                        .textTransformer(value -> Config.tournamentStyleFromCode(value.toString()).getName()),
                Item.Companion.number(holder.get(4), 4, Item.NUMBER, R.string.tournament_legs, () -> String.valueOf(getNumLegs()), this::setNumLegs, this),
                Item.Companion.number(holder.get(5), 5, Item.INFO, R.string.tournament_single_final, () -> App.getInstance().getString(isSingleFinal() ? R.string.yes : R.string.no), this::setSingleFinal, this)
        );
    }

    @Override
    public Item<Tournament> getHeaderItem() {
        return Item.Companion.text(EMPTY_STRING, 0, Item.IMAGE, R.string.team_logo, Item.Companion.nullToEmpty(getImageUrl()), this::setImageUrl, this);
    }

    @Override
    public Team getTeam() {
        return getHost();
    }

    @Override
    public boolean areContentsTheSame(Differentiable other) {
        if (!(other instanceof Tournament)) return getId().equals(other.getId());
        Tournament casted = (Tournament) other;
        return getName().equals(casted.getName())
                && getDescription().equals(casted.getDescription()) && getCurrentRound() == casted.getCurrentRound()
                && getImageUrl().equals(casted.getImageUrl());
    }

    @Override
    public boolean hasMajorFields() {
        return areNotEmpty(getId(), getName());
    }

    @Override
    public Object getChangePayload(Differentiable other) {
        return other;
    }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(getId());
    }

    public void updateHost(Team team) { getHost().update(team); }

    @Override
    public void update(Tournament updatedTournament) {
        this.setId(updatedTournament.getId());
        this.setName(updatedTournament.getName());
        this.setRefPath(updatedTournament.getRefPath());
        this.setDescription(updatedTournament.getDescription());
        this.setImageUrl(updatedTournament.getImageUrl());
        this.setCreated(updatedTournament.getCreated());
        this.setNumLegs(updatedTournament.getNumLegs());
        this.setNumRounds(updatedTournament.getNumRounds());
        this.setCurrentRound(updatedTournament.getCurrentRound());
        this.setNumCompetitors(updatedTournament.getNumCompetitors());
        this.setSingleFinal(updatedTournament.isSingleFinal());
        this.getType().update(updatedTournament.getType());
        this.getStyle().update(updatedTournament.getStyle());
        this.getSport().update(updatedTournament.getSport());
        if (updatedTournament.getHost().hasMajorFields()) this.getHost().update(updatedTournament.getHost());
        if (this.getWinner().hasSameType(updatedTournament.getWinner()))
            getWinner().update(updatedTournament.getWinner());
        else this.setWinner(updatedTournament.getWinner());
    }

    @Override
    public int compareTo(@NonNull Tournament o) {
        int createdComparison = getCreated().compareTo(o.getCreated());
        return createdComparison != 0 ? createdComparison : getId().compareTo(o.getId());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Tournament> CREATOR = new Creator<Tournament>() {
        @Override
        public Tournament createFromParcel(Parcel in) {
            return new Tournament(in);
        }

        @Override
        public Tournament[] newArray(int size) {
            return new Tournament[size];
        }
    };

    public static class GsonAdapter
            implements
            JsonSerializer<Tournament>,
            JsonDeserializer<Tournament> {

        private static final String ID_KEY = "_id";
        private static final String IMAGE_KEY = "imageUrl";
        private static final String NAME_KEY = "name";
        private static final String DESCRIPTION_KEY = "description";
        private static final String HOST_KEY = "host";
        private static final String CREATED_KEY = "created";
        private static final String SPORT_KEY = "sport";
        private static final String TYPE_KEY = "type";
        private static final String STYLE_KEY = "style";
        private static final String REF_PATH = "refPath";
        private static final String WINNER = "winner";
        private static final String NUM_LEGS = "numLegs";
        private static final String NUM_ROUNDS = "numRounds";
        private static final String CURRENT_ROUND = "currentRound";
        private static final String NUM_COMPETITORS = "numCompetitors";
        private static final String SINGLE_FINAL = "singleFinal";

        @Override
        public JsonElement serialize(Tournament src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(NAME_KEY, src.getName().toString());
            serialized.addProperty(DESCRIPTION_KEY, src.getDescription().toString());
            serialized.addProperty(TYPE_KEY, src.getType().toString());
            serialized.addProperty(STYLE_KEY, src.getStyle().toString());
            serialized.addProperty(NUM_LEGS, src.getNumLegs());
            serialized.addProperty(HOST_KEY, src.getHost().getId());
            serialized.addProperty(SINGLE_FINAL, src.isSingleFinal());

            String typeCode = src.getType().getCode();
            String styleCode = src.getStyle().getCode();

            if (!TextUtils.isEmpty(typeCode)) serialized.addProperty(TYPE_KEY, typeCode);
            if (!TextUtils.isEmpty(styleCode)) serialized.addProperty(STYLE_KEY, styleCode);

            return serialized;
        }

        @Override
        public Tournament deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return new Tournament(json.getAsString(), "", "", "", "", new Date(), Team.empty(),
                        Sport.empty(), TournamentType.empty(), TournamentStyle.empty(), Competitor.empty(),
                        1, 1, 0, 0, false);
            }

            JsonObject body = json.getAsJsonObject();

            String id = ModelUtils.asString(ID_KEY, body);
            String imageUrl = ModelUtils.asString(IMAGE_KEY, body);
            String name = ModelUtils.asString(NAME_KEY, body);
            String description = ModelUtils.asString(DESCRIPTION_KEY, body);

            String refPath = ModelUtils.asString(REF_PATH, body);
            String sportCode = ModelUtils.asString(SPORT_KEY, body);
            String typeCode = ModelUtils.asString(TYPE_KEY, body);
            String styleCode = ModelUtils.asString(STYLE_KEY, body);

            String created = ModelUtils.asString(CREATED_KEY, body);
            int numLegs = (int) ModelUtils.asFloat(NUM_LEGS, body);
            int numRounds = (int) ModelUtils.asFloat(NUM_ROUNDS, body);
            int currentRound = (int) ModelUtils.asFloat(CURRENT_ROUND, body);
            int numCompetitors = (int) ModelUtils.asFloat(NUM_COMPETITORS, body);
            boolean singleFinal = ModelUtils.asBoolean(SINGLE_FINAL, body);

            Team host = context.deserialize(body.get(HOST_KEY), Team.class);
            Sport sport = Config.sportFromCode(sportCode);
            TournamentType type = Config.tournamentTypeFromCode(typeCode);
            TournamentStyle style = Config.tournamentStyleFromCode(styleCode);

            JsonObject winnerObject = body.has(WINNER) && body.get(WINNER).isJsonObject()
                    ? body.get(WINNER).getAsJsonObject() : null;

            if (winnerObject != null) winnerObject.addProperty("tournament", id);
            Competitor winner = winnerObject != null ? context.deserialize(winnerObject, Competitor.class) : Competitor.empty();

            if (host == null) host = Team.empty();

            return new Tournament(id, imageUrl, refPath, name, description, ModelUtils.parseDate(created), host,
                    sport, type, style, winner, numLegs, numRounds, currentRound, numCompetitors, singleFinal);
        }
    }
}
