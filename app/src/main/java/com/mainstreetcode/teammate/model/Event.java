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

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.enums.Visibility;
import com.mainstreetcode.teammate.persistence.entity.EventEntity;
import com.mainstreetcode.teammate.util.IdCache;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.mainstreetcode.teammate.util.TextBitmapUtil;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Ignore;

import static com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING;
import static com.mainstreetcode.teammate.util.ModelUtils.areNotEmpty;
import static com.mainstreetcode.teammate.util.ModelUtils.nameAddress;

/**
 * Event events
 */

public class Event extends EventEntity
        implements
        TeamHost,
        Model<Event>,
        HeaderedModel<Event>,
        ListableModel<Event> {

    public static final String PHOTO_UPLOAD_KEY = "event-photo";
    public static final int DEFAULT_NUM_SPOTS = 12;

    @Ignore private static final IdCache holder = IdCache.cache(7);

    public static Event empty() {
        Date date = new Date();
        return new Event("", "", Config.getDefaultEventLogo(), "", "", "", date, date, Team.empty(), null, Visibility.empty(), DEFAULT_NUM_SPOTS);
    }

    public Event(String id, String gameId, String imageUrl, CharSequence name, CharSequence notes, CharSequence locationName,
                 Date startDate, Date endDate, Team team, LatLng location, Visibility visibility, int spots) {
        super(id, gameId, imageUrl, name, notes, locationName, startDate, endDate, team, location, visibility, spots);
    }

    protected Event(Parcel in) {
        super(in);
    }

    public void setName(Game game) {
        setName(game.getHome().getName() + " Vs. " + game.getAway().getName());
    }

    @Override
    public List<Item<Event>> asItems() {
        return Arrays.asList(
                Item.Companion.text(holder.get(0), 0, Item.INPUT, R.string.event_name, Item.Companion.nullToEmpty(getName()), this::setName, this),
                Item.Companion.text(holder.get(1), 1, Item.VISIBILITY, R.string.event_visibility, getVisibility()::getCode, this::setVisibility, this)
                        .textTransformer(value -> Config.visibilityFromCode(value.toString()).getName()),
                Item.Companion.number(holder.get(2), 2, Item.NUMBER, R.string.event_spots, () -> String.valueOf(getSpots()), this::setSpots, this),
                Item.Companion.text(holder.get(3), 3, Item.LOCATION, R.string.location, Item.Companion.nullToEmpty(getLocationName()), this::setLocationName, this),
                Item.Companion.text(holder.get(4), 4, Item.DATE, R.string.start_date, () -> ModelUtils.prettyPrinter.format(getStartDate()), this::setStartDate, this),
                Item.Companion.text(holder.get(5), 5, Item.DATE, R.string.end_date, () -> ModelUtils.prettyPrinter.format(getEndDate()), this::setEndDate, this),
                Item.Companion.text(holder.get(6), 6, Item.TEXT, R.string.notes, Item.Companion.nullToEmpty(getNotes()), this::setNotes, this)
        );
    }

    @Override
    public Item<Event> getHeaderItem() {
        return Item.Companion.text(EMPTY_STRING, 0, Item.IMAGE, R.string.team_logo, Item.Companion.nullToEmpty(getImageUrl()), this::setImageUrl, this);
    }

    @Override
    public boolean areContentsTheSame(Differentiable other) {
        if (!(other instanceof Event)) return getId().equals(other.getId());
        Event casted = (Event) other;
        return getName().equals(casted.getName())
                && getStartDate().equals(casted.getStartDate()) && getEndDate().equals(casted.getEndDate())
                && getLocationName().equals(casted.getLocationName()) && getImageUrl().equals(casted.getImageUrl());
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
        return equals(empty());
    }

    @Override
    public void update(Event updatedEvent) {
        this.setId(updatedEvent.getId());
        this.setName(updatedEvent.getName());
        this.setNotes(updatedEvent.getNotes());
        this.setSpots(updatedEvent.getSpots());
        this.setGameId(updatedEvent.getGameId());
        this.setImageUrl(updatedEvent.getImageUrl());
        this.setEndDate(updatedEvent.getEndDate());
        this.setStartDate(updatedEvent.getStartDate());
        this.setLocation(updatedEvent.getLocation());
        this.setLocationName(updatedEvent.getLocationName());
        this.getVisibility().update(updatedEvent.getVisibility());
        if (updatedEvent.getTeam().hasMajorFields()) this.getTeam().update(updatedEvent.getTeam());
    }

    @Override
    public int compareTo(@NonNull Event o) {
        int startDateComparison = getStartDate().compareTo(o.getStartDate());
        int endDateComparison = getEndDate().compareTo(o.getEndDate());

        return startDateComparison != 0
                ? startDateComparison
                : endDateComparison != 0
                ? endDateComparison
                : getId().compareTo(o.getId());
    }

    public void updateTeam(Team team) {
        this.getTeam().update(team);
    }

    public void setAddress(Address address) {
        setLocationName(nameAddress(address));
        setLocation(new LatLng(address.getLatitude(), address.getLongitude()));
    }

    void setGame(Game game) {
        this.setGameId(game.getId());
    }

    public MarkerOptions getMarkerOptions() {
        return new MarkerOptions()
                .title(getName().toString())
                .position(getLocation())
                .snippet(getLocationName().toString())
                .icon(BitmapDescriptorFactory.fromBitmap(TextBitmapUtil.getBitmapMarker(getTeam().getSport().getEmoji())));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public static class GsonAdapter
            implements
            JsonSerializer<Event>,
            JsonDeserializer<Event> {

        private static final String ID_KEY = "_id";
        private static final String GAME = "game";
        private static final String NAME_KEY = "name";
        private static final String TEAM_KEY = "team";
        private static final String NOTES_KEY = "notes";
        private static final String IMAGE_KEY = "imageUrl";
        private static final String VISIBILITY_KEY = "visibility";
        private static final String LOCATION_NAME_KEY = "locationName";
        private static final String START_DATE_KEY = "startDate";
        private static final String END_DATE_KEY = "endDate";
        private static final String LOCATION_KEY = "location";
        private static final String SPOTS_KEY = "spots";

        @Override
        public JsonElement serialize(Event src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(NAME_KEY, src.getName().toString());
            serialized.addProperty(NOTES_KEY, src.getNotes().toString());
            serialized.addProperty(LOCATION_NAME_KEY, src.getLocationName().toString());
            serialized.addProperty(SPOTS_KEY, src.getSpots());
            serialized.addProperty(TEAM_KEY, src.getTeam().getId());
            serialized.addProperty(START_DATE_KEY, ModelUtils.dateFormatter.format(src.getStartDate()));
            serialized.addProperty(END_DATE_KEY, ModelUtils.dateFormatter.format(src.getEndDate()));
            if (!TextUtils.isEmpty(src.getGameId())) serialized.addProperty(GAME, src.getGameId());

            String visibilityCode = src.getVisibility().getCode();
            if (!TextUtils.isEmpty(visibilityCode))
                serialized.addProperty(VISIBILITY_KEY, visibilityCode);

            if (src.getLocation() != null) {
                JsonArray coordinates = new JsonArray();
                coordinates.add(src.getLocation().longitude);
                coordinates.add(src.getLocation().latitude);
                serialized.add(LOCATION_KEY, coordinates);
            }

            return serialized;
        }

        @Override
        public Event deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return new Event(json.getAsString(), "", "", "", "", "", new Date(), new Date(), Team.empty(), null, Visibility.empty(), DEFAULT_NUM_SPOTS);
            }

            JsonObject eventJson = json.getAsJsonObject();

            String id = ModelUtils.asString(ID_KEY, eventJson);
            String gameId = ModelUtils.asString(GAME, eventJson);
            String name = ModelUtils.asString(NAME_KEY, eventJson);
            String notes = ModelUtils.asString(NOTES_KEY, eventJson);
            String imageUrl = ModelUtils.asString(IMAGE_KEY, eventJson);
            String visibilityCode = ModelUtils.asString(VISIBILITY_KEY, eventJson);
            String locationName = ModelUtils.asString(LOCATION_NAME_KEY, eventJson);
            String startDate = ModelUtils.asString(START_DATE_KEY, eventJson);
            String endDate = ModelUtils.asString(END_DATE_KEY, eventJson);
            int spots = (int) ModelUtils.asFloat(SPOTS_KEY, eventJson);

            if (spots == 0) spots = DEFAULT_NUM_SPOTS;

            Team team = context.deserialize(eventJson.get(TEAM_KEY), Team.class);
            LatLng location = ModelUtils.parseCoordinates(LOCATION_KEY, eventJson);
            Visibility visibility = Config.visibilityFromCode(visibilityCode);

            if (team == null) team = Team.empty();

            return new Event(id, gameId, imageUrl,
                    ModelUtils.processString(name), ModelUtils.processString(notes), ModelUtils.processString(locationName),
                    ModelUtils.parseDate(startDate), ModelUtils.parseDate(endDate), team, location, visibility, spots);
        }
    }
}
