package com.mainstreetcode.teammate.model;

import android.os.Parcel;
import android.support.annotation.NonNull;
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
import com.mainstreetcode.teammate.persistence.entity.StatEntity;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING;
import static com.mainstreetcode.teammate.util.ModelUtils.areNotEmpty;

/**
 * Event events
 */

public class Stat extends StatEntity
        implements
        Model<Stat>,
        HeaderedModel<Stat>,
        ListableModel<Stat> {

    public Stat(@NonNull String id, CharSequence name,
                Date created, Sport sport, User user, Team team, Game game,
                int value, float time) {
        super(id, name, created, sport, user, team, game, value, time);
    }

    protected Stat(Parcel in) {
        super(in);
    }

    @Override
    public List<Item<Stat>> asItems() {
        return Collections.emptyList();
    }

    @Override
    public Item<Stat> getHeaderItem() {
        return Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.team_logo, () -> "", url -> {}, this);
    }

    @Override
    public boolean areContentsTheSame(Identifiable other) {
        if (!(other instanceof Stat)) return id.equals(other.getId());
        Stat casted = (Stat) other;
        return name.equals(casted.name) && user.equals(casted.user)
                && value == casted.value && time == casted.time;
    }

    @Override
    public boolean hasMajorFields() {
        return areNotEmpty(id);
    }

    @Override
    public Object getChangePayload(Identifiable other) {
        return other;
    }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(id);
    }

    @Override
    public void update(Stat updatedEvent) {
        this.id = updatedEvent.id;
        this.name = updatedEvent.name;
        this.created = updatedEvent.created;
        this.value = updatedEvent.value;
        this.time = updatedEvent.time;
        this.sport.update(updatedEvent.sport);
        if (updatedEvent.user.hasMajorFields()) this.user.update(updatedEvent.user);
        if (updatedEvent.team.hasMajorFields()) this.team.update(updatedEvent.team);
        if (updatedEvent.game.hasMajorFields()) this.game.update(updatedEvent.game);
    }

    @Override
    public int compareTo(@NonNull Stat o) {
        int createdComparison = created.compareTo(o.created);

        return createdComparison != 0 ? createdComparison : id.compareTo(o.id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Stat> CREATOR = new Creator<Stat>() {
        @Override
        public Stat createFromParcel(Parcel in) {
            return new Stat(in);
        }

        @Override
        public Stat[] newArray(int size) {
            return new Stat[size];
        }
    };

    public static class GsonAdapter
            implements
            JsonSerializer<Stat>,
            JsonDeserializer<Stat> {

        private static final String ID_KEY = "_id";
        private static final String NAME = "name";
        private static final String CREATED_KEY = "created";
        private static final String SPORT_KEY = "sport";
        private static final String USER = "user";
        private static final String TEAM = "team";
        private static final String GAME = "game";
        private static final String TIME = "time";
        private static final String VALUE = "value";

        @Override
        public JsonElement serialize(Stat src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject stat = new JsonObject();
            stat.addProperty(NAME, src.name.toString());
            stat.addProperty(USER, src.user.getId());
            stat.addProperty(TEAM, src.team.getId());
            stat.addProperty(GAME, src.game.getId());
            stat.addProperty(TIME, src.time);
            stat.addProperty(VALUE, src.value);

            String sportCode = src.sport != null ? src.sport.getCode() : "";
            if (!TextUtils.isEmpty(sportCode)) stat.addProperty(SPORT_KEY, sportCode);

            return stat;
        }

        @Override
        public Stat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return new Stat(json.getAsString(), "", new Date(), Sport.empty(), User.empty(),
                        Team.empty(), null, 0, 0);
            }

            JsonObject body = json.getAsJsonObject();

            String id = ModelUtils.asString(ID_KEY, body);
            String name = ModelUtils.asString(NAME, body);
            String created = ModelUtils.asString(CREATED_KEY, body);
            String sportCode = ModelUtils.asString(SPORT_KEY, body);


            int value = (int) ModelUtils.asFloat(VALUE, body);
            float time = ModelUtils.asFloat(TIME, body);

            Sport sport = Config.sportFromCode(sportCode);
            User user = context.deserialize(body.get(USER), User.class);
            Team team = context.deserialize(body.get(USER), Team.class);
            Game game = context.deserialize(body.get(USER), Game.class);

            return new Stat(id, name, ModelUtils.parseDate(created), sport,
                    user, team, game, value, time);
        }
    }
}
