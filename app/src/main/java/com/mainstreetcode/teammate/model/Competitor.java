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
import android.os.Parcelable;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.persistence.entity.CompetitorEntity;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.lang.reflect.Type;
import java.util.Date;

import static com.mainstreetcode.teammate.util.ModelUtils.areNotEmpty;

public class Competitor extends CompetitorEntity
        implements
        Competitive,
        Model<Competitor> {

    @Ignore private transient CharSequence competitonName = "";

    public static Competitor empty() {
        return new Competitor("", "", null, null, new EmptyCompetitor(), new Date(), -1, false, false);
    }

    public static Competitor empty(Competitive entity) {
        return new Competitor("", "", null, null, entity, new Date(), -1, false, false);
    }

    public Competitor(@NonNull String id, String refPath, String tournamentId, String gameId,
                      Competitive entity, Date created,
                      int seed, boolean accepted, boolean declined) {
        super(id, refPath, tournamentId, gameId, entity, created, seed, accepted, declined);
    }

    protected Competitor(Parcel in) {
        super(in);
    }

    boolean hasSameType(Competitor other) {
        return getRefType().equals(other.getRefType());
    }

    public Tournament getTournament() { return TextUtils.isEmpty(getTournamentId()) ? Tournament.empty() : Tournament.withId(getTournamentId()); }

    public Game getGame() { return TextUtils.isEmpty(getGameId()) ? Game.empty(Team.empty()) : Game.withId(getGameId()); }

    public CharSequence getCompetitionName() { return competitonName; }

    public boolean inOneOffGame() { return !TextUtils.isEmpty(getGameId());}

    @Override
    public boolean hasMajorFields() { return areNotEmpty(getId(), getRefPath()) && getEntity().hasMajorFields(); }

    @Override
    public String getRefType() { return getEntity().getRefType(); }

    @Override
    public CharSequence getName() { return getEntity().getName(); }

    @Override
    public Competitive makeCopy() { return getEntity().makeCopy(); }

    @Override
    public void update(Competitor updated) {
        this.setId(updated.getId());
        this.setSeed(updated.getSeed());
        this.setAccepted(updated.isAccepted());
        this.setDeclined(updated.isDeclined());

        this.setTournamentId(updated.getTournamentId());
        this.setGameId(updated.getGameId());

        updateEntity(updated.getEntity());
    }

    public void updateEntity(Competitive updated) {
        if (getEntity().update(updated)) return;
        setEntity(updated.makeCopy());
    }

    @Override
    public boolean isEmpty() { return TextUtils.isEmpty(getId()); }

    @Override
    public String getImageUrl() { return getEntity().getImageUrl(); }

    @Override
    public boolean areContentsTheSame(Differentiable other) {
        if (!(other instanceof Competitor)) return getId().equals(other.getId());
        Competitor casted = (Competitor) other;
        return getEntity().getClass().equals(casted.getEntity().getClass())
                && getEntity().getRefType().equals(casted.getEntity().getRefType())
                && getEntity().getId().equals(casted.getEntity().getId());
    }

    @Override
    public int compareTo(@NonNull Competitor competitor) {
        Competitive other = competitor.getEntity();
        if (getEntity() instanceof User && other instanceof User)
            return ((User) getEntity()).compareTo(((User) other));
        if (getEntity() instanceof Team && other instanceof Team)
            return ((Team) getEntity()).compareTo(((Team) other));
        return 0;
    }

    public static final Parcelable.Creator<Competitor> CREATOR = new Parcelable.Creator<Competitor>() {
        @Override
        public Competitor createFromParcel(Parcel in) {
            return new Competitor(in);
        }

        @Override
        public Competitor[] newArray(int size) {
            return new Competitor[size];
        }
    };


    public static class GsonAdapter
            implements
            JsonSerializer<Competitor>,
            JsonDeserializer<Competitor> {

        private static final String ID = "_id";
        private static final String REF_PATH = "refPath";
        private static final String ENTITY = "entity";
        private static final String TOURNAMENT = "tournament";
        private static final String GAME = "game";
        private static final String CREATED = "created";
        private static final String SEED = "seed";
        private static final String ACCEPTED = "accepted";
        private static final String DECLINED = "declined";

        @Override
        public JsonElement serialize(Competitor src, Type typeOfSrc, JsonSerializationContext context) {
            if (src.isEmpty()) return new JsonPrimitive(src.getEntity().getId());

            JsonObject json = new JsonObject();
            json.addProperty(ACCEPTED, src.isAccepted());
            json.addProperty(DECLINED, src.isDeclined());

            return json;
        }

        @Override
        public Competitor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            if (json.isJsonPrimitive()) {
                return new Competitor(json.getAsString(), "", "", "", new EmptyCompetitor(), new Date(), -1, false, false);
            }

            JsonObject jsonObject = json.getAsJsonObject();

            Tournament tournament = context.deserialize(jsonObject.get(TOURNAMENT), Tournament.class);
            Game game = context.deserialize(jsonObject.get(GAME), Game.class);

            if (tournament == null) tournament = Tournament.empty();
            if (game == null) game = Game.empty(Team.empty());

            String id = ModelUtils.asString(ID, jsonObject);
            String refPath = ModelUtils.asString(REF_PATH, jsonObject);
            String created = ModelUtils.asString(CREATED, jsonObject);
            String tournamentId = tournament.isEmpty() ? null : tournament.getId();
            String gameId = game.isEmpty() ? null : game.getId();

            int seed = (int) ModelUtils.asFloat(SEED, jsonObject);
            boolean accepted = ModelUtils.asBoolean(ACCEPTED, jsonObject);
            boolean declined = ModelUtils.asBoolean(DECLINED, jsonObject);

            Competitive competitive = context.deserialize(jsonObject.get(ENTITY),
                    User.COMPETITOR_TYPE.equals(refPath) ? User.class : Team.class);

            Competitor competitor = new Competitor(id, refPath, tournamentId, gameId,
                    competitive, ModelUtils.parseDate(created),
                    seed, accepted, declined);

            if (!game.isEmpty()) competitor.competitonName = game.getName();
            else if (!tournament.isEmpty()) competitor.competitonName = tournament.getName();

            return competitor;
        }
    }
}
