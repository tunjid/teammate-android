package com.mainstreetcode.teammate.model;

import android.arch.persistence.room.Ignore;
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
import com.mainstreetcode.teammate.model.enums.StatAttribute;
import com.mainstreetcode.teammate.model.enums.StatAttributes;
import com.mainstreetcode.teammate.model.enums.StatType;
import com.mainstreetcode.teammate.persistence.entity.StatEntity;
import com.mainstreetcode.teammate.util.IdCache;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
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

    @Ignore private static final IdCache holder = IdCache.cache(2);

    public static Stat empty(Game game) {
        return new Stat("", new Date(), Config.statTypeFromCode(""), game.getSport(), User.empty(),
                Team.empty(), game, new StatAttributes(),0, 0);
    }

    public Stat(@NonNull String id,
                Date created, StatType statType, Sport sport, User user, Team team, Game game,
                StatAttributes attributes, int value, float time) {
        super(id, created, statType, sport, user, team, game, attributes, value, time);
    }

    private Stat(Parcel in) {
        super(in);
    }

    @Override
    public List<Item<Stat>> asItems() {
        return Arrays.asList(
                Item.text(holder.get(0), 0, Item.STAT_TYPE, R.string.stat_type, statType::getCode, this::setStatType, this)
                        .textTransformer(value -> Config.statTypeFromCode(value.toString()).getName()),
                Item.number(holder.get(1), 1, Item.NUMBER, R.string.stat_time, () -> String.valueOf(time), this::setTime, this)
        );
    }

    @Override
    public Item<Stat> getHeaderItem() {
        return Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.team_logo, () -> "", url -> {}, this);
    }

    @Override
    public boolean areContentsTheSame(Identifiable other) {
        if (!(other instanceof Stat)) return id.equals(other.getId());
        Stat casted = (Stat) other;
        return statType.equals(casted.statType) && user.equals(casted.user)
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
    public void update(Stat updatedStat) {
        this.id = updatedStat.id;
        this.created = updatedStat.created;
        this.value = updatedStat.value;
        this.time = updatedStat.time;
        this.statType.update(updatedStat.statType);
        this.sport.update(updatedStat.sport);
        if (updatedStat.user.hasMajorFields()) this.user.update(updatedStat.user);
        if (updatedStat.team.hasMajorFields()) this.team.update(updatedStat.team);
        if (updatedStat.game.hasMajorFields()) this.game.update(updatedStat.game);
    }

    @Override
    public int compareTo(@NonNull Stat o) {
        int timeComparison = -Float.compare(time, o.time);
        return timeComparison != 0 ? timeComparison : -created.compareTo(o.created);
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
        private static final String CREATED_KEY = "created";
        private static final String STAT_TYPE = "name";
        private static final String SPORT_KEY = "sport";
        private static final String USER = "user";
        private static final String TEAM = "team";
        private static final String GAME = "game";
        private static final String TIME = "time";
        private static final String VALUE = "value";
        private static final String ATTRIBUTES = "attributes";

        @Override
        public JsonElement serialize(Stat src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject stat = new JsonObject();
            stat.addProperty(STAT_TYPE, src.statType.getCode());
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
                return new Stat(json.getAsString(), new Date(), StatType.empty(), Sport.empty(), User.empty(),
                        Team.empty(), null, new StatAttributes(),0, 0);
            }

            JsonObject body = json.getAsJsonObject();

            String id = ModelUtils.asString(ID_KEY, body);
            String created = ModelUtils.asString(CREATED_KEY, body);
            String typeCode = ModelUtils.asString(STAT_TYPE, body);
            String sportCode = ModelUtils.asString(SPORT_KEY, body);

            int value = (int) ModelUtils.asFloat(VALUE, body);
            float time = ModelUtils.asFloat(TIME, body);

            StatType statType = Config.statTypeFromCode(typeCode);
            Sport sport = Config.sportFromCode(sportCode);
            User user = context.deserialize(body.get(USER), User.class);
            Team team = context.deserialize(body.get(TEAM), Team.class);
            Game game = context.deserialize(body.get(GAME), Game.class);
            StatAttributes attributes = new StatAttributes();

            ModelUtils.deserializeList(context, body.get(ATTRIBUTES), attributes, StatAttribute.class);

            return new Stat(id, ModelUtils.parseDate(created), statType, sport,
                    user, team, game, attributes, value, time);
        }
    }
}
