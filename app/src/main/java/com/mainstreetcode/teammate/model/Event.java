package com.mainstreetcode.teammate.model;

import android.annotation.SuppressLint;
import androidx.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING;
import static com.mainstreetcode.teammate.util.ModelUtils.areNotEmpty;

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
        name = game.getHome().getName() + " Vs. " + game.getAway().getName();
    }

    @Override
    public List<Item<Event>> asItems() {
        return Arrays.asList(
                Item.text(holder.get(0), 0, Item.INPUT, R.string.event_name, Item.nullToEmpty(name), this::setName, this),
                Item.text(holder.get(1), 1, Item.VISIBILITY, R.string.event_visibility, visibility::getCode, this::setVisibility, this)
                        .textTransformer(value -> Config.visibilityFromCode(value.toString()).getName()),
                Item.number(holder.get(2), 2, Item.NUMBER, R.string.event_spots, () -> String.valueOf(spots), this::setSpots, this),
                Item.text(holder.get(3), 3, Item.LOCATION, R.string.location, Item.nullToEmpty(locationName), this::setLocationName, this),
                Item.text(holder.get(4), 4, Item.DATE, R.string.start_date, () -> ModelUtils.prettyPrinter.format(startDate), this::setStartDate, this),
                Item.text(holder.get(5), 5, Item.DATE, R.string.end_date, () -> ModelUtils.prettyPrinter.format(endDate), this::setEndDate, this),
                Item.text(holder.get(6), 6, Item.TEXT, R.string.notes, Item.nullToEmpty(notes), this::setNotes, this)
                );
    }

    @Override
    public Item<Event> getHeaderItem() {
        return Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.team_logo, Item.nullToEmpty(imageUrl), this::setImageUrl, this);
    }

    @Override
    public boolean areContentsTheSame(Identifiable other) {
        if (!(other instanceof Event)) return id.equals(other.getId());
        Event casted = (Event) other;
        return name.equals(casted.name)
                && startDate.equals(casted.getStartDate()) && endDate.equals(casted.getEndDate())
                && locationName.equals(casted.getLocationName()) && imageUrl.equals(casted.getImageUrl());
    }

    @Override
    public boolean hasMajorFields() {
        return areNotEmpty(id, name);
    }

    @Override
    public Object getChangePayload(Identifiable other) {
        return other;
    }

    @Override
    public boolean isEmpty() {
        return equals(empty());
    }

    @Override
    public void update(Event updatedEvent) {
        this.id = updatedEvent.id;
        this.name = updatedEvent.name;
        this.notes = updatedEvent.notes;
        this.spots = updatedEvent.spots;
        this.gameId = updatedEvent.gameId;
        this.imageUrl = updatedEvent.imageUrl;
        this.endDate = updatedEvent.endDate;
        this.startDate = updatedEvent.startDate;
        this.location = updatedEvent.location;
        this.locationName = updatedEvent.locationName;
        this.visibility.update(updatedEvent.visibility);
        if (updatedEvent.team.hasMajorFields()) this.team.update(updatedEvent.team);
    }

    @Override
    public int compareTo(@NonNull Event o) {
        int startDateComparison = startDate.compareTo(o.startDate);
        int endDateComparison = endDate.compareTo(o.endDate);

        return startDateComparison != 0
                ? startDateComparison
                : endDateComparison != 0
                ? endDateComparison
                : id.compareTo(o.id);
    }

    public void setTeam(Team team) {
        this.team.update(team);
    }

    @SuppressLint("CheckResult")
    public void setPlace(Place place) {
        locationName = place.getName();
        location = place.getLatLng();
    }

    void setGame(Game game) {
        this.gameId = game.getId();
    }

    public MarkerOptions getMarkerOptions() {
        return new MarkerOptions()
                .title(name.toString())
                .position(location)
                .snippet(locationName.toString())
                .icon(BitmapDescriptorFactory.fromBitmap(TextBitmapUtil.getBitmapMarker(team.getSport().getEmoji())));
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

            serialized.addProperty(NAME_KEY, src.name.toString());
            serialized.addProperty(NOTES_KEY, src.notes.toString());
            serialized.addProperty(LOCATION_NAME_KEY, src.locationName.toString());
            serialized.addProperty(SPOTS_KEY, src.getSpots());
            serialized.addProperty(TEAM_KEY, src.team.getId());
            serialized.addProperty(START_DATE_KEY, ModelUtils.dateFormatter.format(src.startDate));
            serialized.addProperty(END_DATE_KEY, ModelUtils.dateFormatter.format(src.endDate));
            if (!TextUtils.isEmpty(src.getGameId())) serialized.addProperty(GAME, src.gameId);

            String visibilityCode = src.visibility != null ? src.visibility.getCode() : "";
            if (!TextUtils.isEmpty(visibilityCode))
                serialized.addProperty(VISIBILITY_KEY, visibilityCode);

            if (src.location != null) {
                JsonArray coordinates = new JsonArray();
                coordinates.add(src.location.longitude);
                coordinates.add(src.location.latitude);
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
