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

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

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
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING;
import static com.mainstreetcode.teammate.util.ModelUtils.areNotEmpty;
import static com.mainstreetcode.teammate.util.ModelUtils.asString;

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

    @Ignore private static final IdCache holder = IdCache.cache(10);

    public Team(@NonNull String id, String imageUrl, String screenName,
                String city, String state, String zip,
                CharSequence name, CharSequence description,
                Date created, LatLng location, Sport sport,
                long storageUsed, long maxStorage, int minAge, int maxAge) {
        super(id, imageUrl, screenName, city, state, zip, name, description, created, location, sport, storageUsed, maxStorage, minAge, maxAge);
    }

    private Team(Parcel in) {
        super(in);
    }

    public static Team empty() {
        return new Team(NEW_TEAM, Config.getDefaultTeamLogo(), "", "Detroit️", "", "", "My Team", "", new Date(), null, Sport.empty(), 0, 0, 0, 0);
    }

    @Override
    public List<Item<Team>> asItems() {
        return Arrays.asList(
                Item.Companion.text(holder.get(0), 0, Item.INPUT, R.string.team_name, Item.Companion.nullToEmpty(name), this::setName, this),
                Item.Companion.text(holder.get(1), 1, Item.SPORT, R.string.team_sport, sport::getCode, this::setSport, this)
                        .textTransformer(value -> Config.sportFromCode(value.toString()).getName()),
                Item.Companion.text(holder.get(2), 2, Item.INFO, R.string.screen_name, Item.Companion.nullToEmpty(screenName), this::setScreenName, this),
                Item.Companion.text(holder.get(3), 3, Item.CITY, R.string.city, Item.Companion.nullToEmpty(city), this::setCity, this),
                Item.Companion.text(holder.get(4), 4, Item.STATE, R.string.state, Item.Companion.nullToEmpty(state), this::setState, this),
                Item.Companion.text(holder.get(5), 5, Item.ZIP, R.string.zip, Item.Companion.nullToEmpty(zip), this::setZip, this),
                Item.Companion.text(holder.get(6), 6, Item.DESCRIPTION, R.string.team_description, Item.Companion.nullToEmpty(description), this::setDescription, this),
                Item.Companion.number(holder.get(7), 7, Item.NUMBER, R.string.team_min_age, () -> String.valueOf(minAge), this::setMinAge, this),
                Item.Companion.number(holder.get(8), 8, Item.NUMBER, R.string.team_max_age, () -> String.valueOf(maxAge), this::setMaxAge, this),
                Item.Companion.text(holder.get(9), 9, Item.ABOUT, R.string.team_storage_used, () -> storageUsed + "/" + maxStorage + " MB", null, this)
        );
    }

    @Override
    public Item<Team> getHeaderItem() {
        return Item.Companion.text(EMPTY_STRING, 0, Item.IMAGE, R.string.team_logo, Item.Companion.nullToEmpty(imageUrl), this::setImageUrl, this);
    }

    @Override
    public boolean areContentsTheSame(Differentiable other) {
        if (!(other instanceof Team)) return id.equals(other.getId());
        Team casted = (Team) other;
        boolean same = name.equals(casted.name) && city.equals(casted.getCity())
                && imageUrl.equals(casted.getImageUrl());

        return same && (sport == null || sport.equals(casted.sport));
    }

    @Override
    public String getRefType() { return COMPETITOR_TYPE; }

    public String getImageUrl() { return TextUtils.isEmpty(imageUrl) ? Config.getDefaultTeamLogo() : imageUrl; }

    @Override
    public Object getChangePayload(Differentiable other) {
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
        this.screenName = updatedTeam.screenName;
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
    public boolean update(Competitive other) {
        if (!(other instanceof Team)) return false;
        update((Team) other);
        return true;
    }

    @Override
    public Competitive makeCopy() {
        Team copy = Team.empty();
        copy.update(this);
        return copy;
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
        if (city == null) city = address.getSubLocality();

        state = address.getAdminArea();
        zip = address.getPostalCode();

        if (city == null) city = "N/A";
        if (state == null) state = "N/A";

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
        private static final String SCREEN_NAME = "screenName";
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
                return new Team(json.getAsString(), "", "", "", "", "", "", "", new Date(), new LatLng(0, 0), Sport.empty(), 0, 0, 0, 0);
            }

            JsonObject teamJson = json.getAsJsonObject();

            String id = ModelUtils.asString(UID_KEY, teamJson);
            String name = ModelUtils.asString(NAME_KEY, teamJson);
            String screenName = asString(SCREEN_NAME, teamJson);
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

            return new Team(id, imageUrl, screenName, city, state, zip,
                    ModelUtils.processString(name), ModelUtils.processString(description),
                    created, location, sport, storageUsed, maxStorage, minAge, maxAge);
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
            if (!TextUtils.isEmpty(src.screenName)) team.addProperty(SCREEN_NAME, src.screenName);

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
