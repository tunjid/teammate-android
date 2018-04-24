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
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.mainstreetcode.teammate.util.ModelUtils.areNotEmpty;

/**
 * Teams
 */

public class Team extends TeamEntity
        implements
        Model<Team>,
        HeaderedModel<Team>,
        ListableModel<Team> {

    //    private static final int SPORT_POSITION = 1;
    private static final int CITY_POSITION = 2;
    private static final int STATE_POSITION = 3;
    private static final int ZIP_POSITION = 4;
    public static final int ROLE_POSITION = 9;

    public static final String PHOTO_UPLOAD_KEY = "team-photo";
    private static final String NEW_TEAM = "new.team";

    @Ignore private final List<Item<Team>> items;

    public Team(@NonNull String id, String name, String city, String state, String zip,
                String description, String imageUrl,
                Date created, LatLng location, Sport sport,
                long storageUsed, long maxStorage, int minAge, int maxAge) {
        super(id, name, city, state, zip, description, imageUrl, created, location, sport, storageUsed, maxStorage, minAge, maxAge);
        items = buildItems();
    }

    private Team(Parcel in) {
        super(in);
        items = buildItems();
    }

    public static Team empty() {
        return new Team(NEW_TEAM, "", "", "", "", "", Config.getDefaultTeamLogo(), new Date(), null, Sport.empty(), 0, 0, 0, 0);
    }

    @SuppressWarnings("unchecked")
    private List<Item<Team>> buildItems() {
        return Arrays.asList(
                Item.text(0, Item.INPUT, R.string.team_name, Item.nullToEmpty(name), this::setName, this),
                Item.text(1, Item.SPORT, R.string.team_sport, sport::getCode, this::setSport, this)
                        .textTransformer(value -> Config.sportFromCode(value.toString()).getName()),
                Item.text(CITY_POSITION, Item.CITY, R.string.city, Item.nullToEmpty(city), this::setCity, this),
                Item.text(STATE_POSITION, Item.STATE, R.string.state, Item.nullToEmpty(state), this::setState, this),
                Item.text(ZIP_POSITION, Item.ZIP, R.string.zip, Item.nullToEmpty(zip), this::setZip, this),
                Item.text(5, Item.DESCRIPTION, R.string.team_description, Item.nullToEmpty(description), this::setDescription, this),
                Item.number(6, Item.NUMBER, R.string.team_min_age, () -> String.valueOf(minAge), this::setMinAge, this),
                Item.number(7, Item.NUMBER, R.string.team_max_age, () -> String.valueOf(maxAge), this::setMaxAge, this),
                Item.text(8, Item.INFO, R.string.team_storage_used, () -> storageUsed + "/" + maxStorage + " MB", null, this),
                Item.text(ROLE_POSITION, Item.ROLE, R.string.team_role, Item.nullToEmpty(""), null, this)
        );
    }

    @Override
    public List<Item<Team>> asItems() { return items; }

    @Override
    public Item<Team> getHeaderItem() {
        return Item.text(0, Item.IMAGE, R.string.team_logo, Item.nullToEmpty(imageUrl), this::setImageUrl, this);
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
    public Object getChangePayload(Identifiable other) {
        return other;
    }

    @Override
    public boolean isEmpty() {
        return this.equals(empty());
    }

    @Override
    public void reset() {
        name = city = state = zip = imageUrl = "";
        sport.reset();
        restItemList();
    }

    @Override
    public void update(Team updatedTeam) {
        this.id = updatedTeam.id;
        this.imageUrl = updatedTeam.imageUrl;
        this.storageUsed = updatedTeam.storageUsed;

        location = updatedTeam.location;
        sport.update(updatedTeam.sport);

        updateItemList(updatedTeam);
    }

    @Override
    public int compareTo(@NonNull Team o) {
        int nameComparision = name.compareTo(o.name);
        return nameComparision != 0 ? nameComparision : id.compareTo(o.id);
    }

    @Override
    public boolean hasMajorFields() {
        return areNotEmpty(id, name, city, state);
    }

    public void setAddress(Address address) {
        items.get(CITY_POSITION).setValue(address.getLocality());
        items.get(STATE_POSITION).setValue(address.getAdminArea());
        items.get(ZIP_POSITION).setValue(address.getPostalCode());

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
        private static final String LOGO_KEY = "imageUrl";
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
            String logoUrl = ModelUtils.asString(LOGO_KEY, teamJson);
            Date created = ModelUtils.parseDate(ModelUtils.asString(CREATED_KEY, teamJson));
            LatLng location = ModelUtils.parseCoordinates(LOCATION_KEY, teamJson);
            Sport sport = Config.sportFromCode(sportCode);
            long storageUsed = (long) ModelUtils.asFloat(STORAGE_USED_KEY, teamJson);
            long maxStorage = (long) ModelUtils.asFloat(MAX_STORAGE_KEY, teamJson);
            int minAge = (int) ModelUtils.asFloat(MIN_AGE_KEY, teamJson);
            int maxAge = (int) ModelUtils.asFloat(MAX_AGE_KEY, teamJson);

            return new Team(id, name, city, state, zip, description, logoUrl, created, location, sport, storageUsed, maxStorage, minAge, maxAge);
        }

        @Override
        public JsonElement serialize(Team src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject team = new JsonObject();
            team.addProperty(NAME_KEY, src.name);
            team.addProperty(CITY_KEY, src.city);
            team.addProperty(STATE_KEY, src.state);
            team.addProperty(ZIP_KEY, src.zip);
            team.addProperty(DESCRIPTION_KEY, src.description);
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
