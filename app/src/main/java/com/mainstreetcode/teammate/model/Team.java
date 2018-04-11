package com.mainstreetcode.teammate.model;

import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Relation;
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
import com.mainstreetcode.teammate.persistence.entity.JoinRequestEntity;
import com.mainstreetcode.teammate.persistence.entity.RoleEntity;
import com.mainstreetcode.teammate.persistence.entity.TeamEntity;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.mainstreetcode.teammate.util.ModelUtils.deserializeList;

/**
 * Teams
 */

public class Team extends TeamEntity
        implements
        Model<Team>,
        HeaderedModel<Team>,
        ItemListableBean<Team> {

    private static final int SPORT_POSITION = 1;
    private static final int CITY_POSITION = 2;
    private static final int STATE_POSITION = 3;
    private static final int ZIP_POSITION = 4;
    public static final int ROLE_POSITION = 9;

    public static final String PHOTO_UPLOAD_KEY = "team-photo";
    private static final String NEW_TEAM = "new.team";

    // Room fetches roles after setRoles is called. Since the reference of roles can't be changed,
    // store the delayed roles here and update after Room is done.
    @Ignore private List<Role> delayedRoles = new ArrayList<>();
    @Ignore private List<JoinRequest> delayedRequests = new ArrayList<>();

    @Relation(parentColumn = "team_id", entityColumn = "role_team", entity = RoleEntity.class)
    private List<Role> roles = new ArrayList<>();

    @Relation(parentColumn = "team_id", entityColumn = "join_request_team", entity = JoinRequestEntity.class)
    private List<JoinRequest> joinRequests = new ArrayList<>();

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
        in.readList(roles, Role.class.getClassLoader());
        in.readList(joinRequests, JoinRequest.class.getClassLoader());
        items = buildItems();
    }

    public static Team empty() {
        return new Team(NEW_TEAM, "", "", "", "", "", Config.getDefaultTeamLogo(), new Date(), null, Sport.empty(), 0, 0, 0, 0);
    }

    public static Team updateDelayedModels(Team team) {
        team.roles.clear();
        team.roles.addAll(team.delayedRoles);
        team.joinRequests.clear();
        team.joinRequests.addAll(team.delayedRequests);
        return team;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Item<Team>> buildItems() {
        return Arrays.asList(
                Item.text(Item.INPUT, R.string.team_name, name == null ? "" : name, this::setName, this),
                Item.text(Item.SPORT, R.string.team_sport, sport == null ? "" : sport.getName(), this::setSport, this),
                Item.text(Item.CITY, R.string.city, city == null ? "" : city, this::setCity, this),
                Item.text(Item.STATE, R.string.state, state == null ? "" : state, this::setState, this),
                Item.text(Item.ZIP, R.string.zip, zip == null ? "" : zip, this::setZip, this),
                Item.text(Item.INPUT, R.string.team_description, description == null ? "" : description, this::setDescription, this),
                Item.number(Item.NUMBER, R.string.team_min_age, String.valueOf(minAge), this::setMinAge, this),
                Item.number(Item.NUMBER, R.string.team_max_age, String.valueOf(maxAge), this::setMaxAge, this),
                Item.text(Item.INFO, R.string.team_storage_used, storageUsed + "/" + maxStorage + " MB", null, this),
                Item.text(Item.ROLE, R.string.team_role, "", null, this)
        );
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public Item get(int position) {
        return items.get(position);
    }

    @Override
    public Item<Team> getHeaderItem() {
        return Item.text(Item.IMAGE, R.string.team_logo, imageUrl, this::setImageUrl, this);
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
        name = "";
        city = "";
        state = "";
        zip = "";
        imageUrl = "";

        int size = size();
        for (int i = 0; i < size; i++) if (i != SPORT_POSITION) get(i).setValue("");

        sport.reset();
        roles.clear();
        joinRequests.clear();
    }

    @Override
    public void update(Team updatedTeam) {
        this.id = updatedTeam.id;
        this.imageUrl = updatedTeam.imageUrl;
        this.storageUsed = updatedTeam.storageUsed;

        int size = size();
        for (int i = 0; i < size; i++)
            if (i != SPORT_POSITION) get(i).setValue(updatedTeam.get(i).getValue());

        location = updatedTeam.location;
        sport.update(updatedTeam.sport);

        ModelUtils.preserveAscending(roles, updatedTeam.roles);
        ModelUtils.preserveAscending(joinRequests, updatedTeam.joinRequests);
    }

    @Override
    public int compareTo(@NonNull Team o) {
        int nameComparision = name.compareTo(o.name);
        return nameComparision != 0 ? nameComparision : id.compareTo(o.id);
    }

    public List<Role> getRoles() {
        return roles;
    }

    public List<JoinRequest> getJoinRequests() {
        return joinRequests;
    }

    public void setRoles(List<Role> roles) {
        delayedRoles = roles;
    }

    public void setJoinRequests(List<JoinRequest> requests) {
        delayedRequests = requests;
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeList(roles);
        dest.writeList(joinRequests);
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
        private static final String ROLES_KEY = "roles";
        private static final String LOCATION_KEY = "location";
        private static final String JOIN_REQUEST_KEY = "joinRequests";
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

            Team team = new Team(id, name, city, state, zip, description, logoUrl, created, location, sport, storageUsed, maxStorage, minAge, maxAge);

            if (teamJson.has(ROLES_KEY)) {
                deserializeList(context, teamJson.get(ROLES_KEY), team.roles, Role.class);
            }
            if (teamJson.has(JOIN_REQUEST_KEY)) {
                deserializeList(context, teamJson.get(JOIN_REQUEST_KEY), team.joinRequests, JoinRequest.class);
            }

            return team;
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
