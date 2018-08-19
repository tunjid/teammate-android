package com.mainstreetcode.teammate.model;

import android.arch.persistence.room.Ignore;
import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
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
import com.mainstreetcode.teammate.persistence.entity.TeamEntity;
import com.mainstreetcode.teammate.util.IdCache;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING;
import static com.mainstreetcode.teammate.util.ModelUtils.areNotEmpty;

/**
 * Teams
 */

public class Team extends TeamEntity
        implements
        TeamHost,
        Competitive,
        Model<Team>,
        HeaderedModel<Team>,
        ListableModel<Team> {

    public static final String PHOTO_UPLOAD_KEY = "team-photo";
    public static final String COMPETITOR_TYPE = "team";
    private static final String NEW_TEAM = "new.team";

    @Ignore private static final IdCache holder = IdCache.cache(9);

    public Team(@NonNull String id, String imageUrl, String city, String state, String zip,
                CharSequence name, CharSequence description,
                Date created, LatLng location, Sport sport,
                long storageUsed, long maxStorage, int minAge, int maxAge) {
        super(id, imageUrl, city, state, zip, name, description, created, location, sport, storageUsed, maxStorage, minAge, maxAge);
    }

    private Team(Parcel in) {
        super(in);
    }

    public static Team empty() {
        return new Team(NEW_TEAM, Config.getDefaultTeamLogo(), "", "", "", "", "", new Date(), null, Sport.empty(), 0, 0, 0, 0);
    }

    @Override
    public List<Item<Team>> asItems() {
        return Arrays.asList(
                Item.text(holder.get(0), 0, Item.INPUT, R.string.team_name, Item.nullToEmpty(name), this::setName, this),
                Item.text(holder.get(1), 1, Item.SPORT, R.string.team_sport, sport::getCode, this::setSport, this)
                        .textTransformer(value -> Config.sportFromCode(value.toString()).getName()),
                Item.text(holder.get(2), 2, Item.CITY, R.string.city, Item.nullToEmpty(city), this::setCity, this),
                Item.text(holder.get(3), 3, Item.STATE, R.string.state, Item.nullToEmpty(state), this::setState, this),
                Item.text(holder.get(4), 4, Item.ZIP, R.string.zip, Item.nullToEmpty(zip), this::setZip, this),
                Item.text(holder.get(5), 5, Item.DESCRIPTION, R.string.team_description, Item.nullToEmpty(description), this::setDescription, this),
                Item.number(holder.get(6), 6, Item.NUMBER, R.string.team_min_age, () -> String.valueOf(minAge), this::setMinAge, this),
                Item.number(holder.get(7), 7, Item.NUMBER, R.string.team_max_age, () -> String.valueOf(maxAge), this::setMaxAge, this),
                Item.text(holder.get(8), 8, Item.INFO, R.string.team_storage_used, () -> storageUsed + "/" + maxStorage + " MB", null, this)
        );
    }

    @Override
    public Item<Team> getHeaderItem() {
        return Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.team_logo, Item.nullToEmpty(imageUrl), this::setImageUrl, this);
    }

    @Override
    public boolean areContentsTheSame(Identifiable other) {
        if (!(other instanceof Team)) return id.equals(other.getId());
        Team casted = (Team) other;
        boolean same = name.equals(casted.name) && city.equals(casted.getCity())
                && imageUrl.equals(casted.getImageUrl());

        return same && (sport == null || sport.equals(casted.sport));
    }

    @Override
    public String getRefType() {
        return COMPETITOR_TYPE;
    }

    @Override
    public Object getChangePayload(Identifiable other) {
        return other;
    }

    @Override
    public boolean isEmpty() {
        return this.equals(empty());
    }

    @Override
    public Team getTeam() {
        return this;
    }

    @Override
    public void update(Team updatedTeam) {
        this.id = updatedTeam.id;
        this.name = updatedTeam.name;
        this.city = updatedTeam.city;
        this.state = updatedTeam.state;
        this.zip = updatedTeam.zip;
        this.description = updatedTeam.description;
        this.minAge = updatedTeam.minAge;
        this.maxAge = updatedTeam.maxAge;
        this.imageUrl = updatedTeam.imageUrl;
        this.storageUsed = updatedTeam.storageUsed;

        this.location = updatedTeam.location;
        this.sport.update(updatedTeam.sport);
    }

    @Override
    public int compareTo(@NonNull Team o) {
        int nameComparision = name.toString().compareTo(o.name.toString());
        return nameComparision != 0 ? nameComparision : id.compareTo(o.id);
    }

    @Override
    public boolean hasMajorFields() {
        return areNotEmpty(id, name, city, state);
    }

    public void setAddress(Address address) {
        city = address.getLocality();
        state = address.getAdminArea();
        zip = address.getPostalCode();
        location = new LatLng(address.getLatitude(), address.getLongitude());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Team> CREATOR = new Parcelable.Creator<Team>() {
        @Override
        public Team createFromParcel(Parcel in) {
            return new Team(in);
        }

        @Override
        public Team[] newArray(int size) {
            return new Team[size];
        }
    };


    public static class GsonAdapter
            implements
            JsonSerializer<Team>,
            JsonDeserializer<Team> {

        private static final String UID_KEY = "_id";
        private static final String NAME_KEY = "name";
        private static final String CITY_KEY = "city";
        private static final String STATE_KEY = "state";
        private static final String ZIP_KEY = "zip";
        private static final String SPORT_KEY = "sport";
        private static final String DESCRIPTION_KEY = "description";
        private static final String IMAGE_URL_KEY = "imageUrl";
        private static final String CREATED_KEY = "created";
        private static final String LOCATION_KEY = "location";
        private static final String STORAGE_USED_KEY = "storageUsed";
        private static final String MAX_STORAGE_KEY = "maxStorage";
        private static final String MIN_AGE_KEY = "minAge";
        private static final String MAX_AGE_KEY = "maxAge";

        @Override
        public Team deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return new Team(json.getAsString(), "", "", "", "", "", "", new Date(), new LatLng(0, 0), Sport.empty(), 0, 0, 0, 0);
            }

            JsonObject teamJson = json.getAsJsonObject();

            String id = ModelUtils.asString(UID_KEY, teamJson);
            String name = ModelUtils.asString(NAME_KEY, teamJson);
            String city = ModelUtils.asString(CITY_KEY, teamJson);
            String state = ModelUtils.asString(STATE_KEY, teamJson);
            String zip = ModelUtils.asString(ZIP_KEY, teamJson);
            String sportCode = ModelUtils.asString(SPORT_KEY, teamJson);
            String description = ModelUtils.asString(DESCRIPTION_KEY, teamJson);
            String imageUrl = ModelUtils.asString(IMAGE_URL_KEY, teamJson);
            Date created = ModelUtils.parseDate(ModelUtils.asString(CREATED_KEY, teamJson));
            LatLng location = ModelUtils.parseCoordinates(LOCATION_KEY, teamJson);
            Sport sport = Config.sportFromCode(sportCode);
            long storageUsed = (long) ModelUtils.asFloat(STORAGE_USED_KEY, teamJson);
            long maxStorage = (long) ModelUtils.asFloat(MAX_STORAGE_KEY, teamJson);
            int minAge = (int) ModelUtils.asFloat(MIN_AGE_KEY, teamJson);
            int maxAge = (int) ModelUtils.asFloat(MAX_AGE_KEY, teamJson);

            return new Team(id, imageUrl, city, state, zip, name, description, created, location, sport, storageUsed, maxStorage, minAge, maxAge);
        }

        @Override
        public JsonElement serialize(Team src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject team = new JsonObject();
            team.addProperty(NAME_KEY, src.name.toString());
            team.addProperty(CITY_KEY, src.city);
            team.addProperty(STATE_KEY, src.state);
            team.addProperty(ZIP_KEY, src.zip);
            team.addProperty(DESCRIPTION_KEY, src.description.toString());
            team.addProperty(MIN_AGE_KEY, src.minAge);
            team.addProperty(MAX_AGE_KEY, src.maxAge);

            String sportCode = src.sport != null ? src.sport.getCode() : "";
            if (!TextUtils.isEmpty(sportCode)) team.addProperty(SPORT_KEY, sportCode);

            if (src.location != null) {
                JsonArray coordinates = new JsonArray();
                coordinates.add(src.location.longitude);
                coordinates.add(src.location.latitude);
                team.add(LOCATION_KEY, coordinates);
            }

            return team;
        }
    }
}
