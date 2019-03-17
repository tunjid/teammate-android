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
        empty.id = id;
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
                Item.text(holder.get(0), 0, Item.INPUT, R.string.tournament_name, Item.nullToEmpty(name), this::setName, this),
                Item.text(holder.get(1), 1, Item.DESCRIPTION, R.string.tournament_description, Item.nullToEmpty(description), this::setDescription, this),
                Item.text(holder.get(2), 2, Item.TOURNAMENT_TYPE, R.string.tournament_type, type::getCode, this::setType, this)
                        .textTransformer(value -> Config.tournamentTypeFromCode(value.toString()).getName()),
                Item.text(holder.get(3), 3, Item.TOURNAMENT_STYLE, R.string.tournament_style, style::getCode, this::setStyle, this)
                        .textTransformer(value -> Config.tournamentStyleFromCode(value.toString()).getName()),
                Item.number(holder.get(4), 4, Item.NUMBER, R.string.tournament_legs, () -> String.valueOf(numLegs), this::setNumLegs, this),
                Item.number(holder.get(5), 5, Item.INFO, R.string.tournament_single_final, () -> App.getInstance().getString(singleFinal ? R.string.yes : R.string.no), this::setSingleFinal, this)
        );
    }

    @Override
    public Item<Tournament> getHeaderItem() {
        return Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.team_logo, Item.nullToEmpty(imageUrl), this::setImageUrl, this);
    }

    @Override
    public Team getTeam() {
        return host;
    }

    @Override
    public boolean areContentsTheSame(Differentiable other) {
        if (!(other instanceof Tournament)) return id.equals(other.getId());
        Tournament casted = (Tournament) other;
        return name.equals(casted.name)
                && description.equals(casted.description) && currentRound == casted.currentRound
                && imageUrl.equals(casted.getImageUrl());
    }

    @Override
    public boolean hasMajorFields() {
        return areNotEmpty(id, name);
    }

    @Override
    public Object getChangePayload(Differentiable other) {
        return other;
    }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(id);
    }

    public void updateHost(Team team) { host.update(team); }

    @Override
    @SuppressWarnings("unchecked")
    public void update(Tournament updatedTournament) {
        this.id = updatedTournament.id;
        this.name = updatedTournament.name;
        this.refPath = updatedTournament.refPath;
        this.description = updatedTournament.description;
        this.imageUrl = updatedTournament.imageUrl;
        this.created = updatedTournament.created;
        this.numLegs = updatedTournament.numLegs;
        this.numRounds = updatedTournament.numRounds;
        this.currentRound = updatedTournament.currentRound;
        this.numCompetitors = updatedTournament.numCompetitors;
        this.singleFinal = updatedTournament.singleFinal;
        this.type.update(updatedTournament.type);
        this.style.update(updatedTournament.style);
        this.sport.update(updatedTournament.sport);
        if (updatedTournament.host.hasMajorFields()) this.host.update(updatedTournament.host);
        if (this.winner.hasSameType(updatedTournament.winner))
            winner.update(updatedTournament.winner);
        else this.winner = updatedTournament.winner;
    }

    @Override
    public int compareTo(@NonNull Tournament o) {
        int createdComparison = created.compareTo(o.created);
        return createdComparison != 0 ? createdComparison : id.compareTo(o.id);
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

            serialized.addProperty(NAME_KEY, src.name.toString());
            serialized.addProperty(DESCRIPTION_KEY, src.description.toString());
            serialized.addProperty(TYPE_KEY, src.type.toString());
            serialized.addProperty(STYLE_KEY, src.style.toString());
            serialized.addProperty(NUM_LEGS, src.numLegs);
            serialized.addProperty(HOST_KEY, src.host.getId());
            serialized.addProperty(SINGLE_FINAL, src.singleFinal);

            String typeCode = src.type != null ? src.type.getCode() : "";
            String styleCode = src.style != null ? src.style.getCode() : "";

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
