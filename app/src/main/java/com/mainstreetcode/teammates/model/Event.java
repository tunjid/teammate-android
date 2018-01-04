package com.mainstreetcode.teammates.model;

import android.arch.persistence.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.persistence.entity.EventEntity;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Event events
 */

public class Event extends EventEntity
        implements
        Model<Event>,
        HeaderedModel<Event>,
        ItemListableBean<Event> {

    public static final String PHOTO_UPLOAD_KEY = "event-photo";
    private static final int LOCATION_POSITION = 4;

    @Ignore private List<Guest> guests = new ArrayList<>();
    @Ignore private final List<Item<Event>> items;

    public static Event empty() {
        Date date = new Date();
        return new Event("", "", "", "", "", date, date, Team.empty(), null);
    }

    public Event(String id, String name, String notes, String imageUrl, String locationName,
                 Date startDate, Date endDate, Team team, LatLng location) {
        super(id, name, notes, imageUrl, locationName, startDate, endDate, team, location);
        this.team = team;
        items = buildItems();
    }

    protected Event(Parcel in) {
        super(in);
        in.readList(guests, Guest.class.getClassLoader());
        items = buildItems();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Item<Event>> buildItems() {
        return Arrays.asList(
                new Item(Item.INPUT, R.string.event_name, name == null ? "" : name, this::setName, this),
                new Item(Item.INPUT, R.string.notes, notes == null ? "" : notes, this::setNotes, this),
                new Item(Item.DATE, R.string.start_date, prettyPrinter.format(startDate), this::setStartDate, this),
                new Item(Item.DATE, R.string.end_date, prettyPrinter.format(endDate), this::setEndDate, this),
                new Item(Item.LOCATION, R.string.location, locationName == null ? "" : locationName, this::setLocationName, this)
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
    public Item<Event> getHeaderItem() {
        return new Item<>(Item.IMAGE, R.string.team_logo, imageUrl, this::setImageUrl, this);
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
    public Object getChangePayload(Identifiable other) {
        return other;
    }

    @Override
    public boolean isEmpty() {
        return equals(empty());
    }

    @Override
    public void reset() {
        imageUrl = "";

        int size = size();
        for (int i = 0; i < size; i++) get(i).setValue("");

        guests.clear();
        team.reset();
    }

    @Override
    public void update(Event updatedEvent) {
        this.id = updatedEvent.getId();
        this.imageUrl = updatedEvent.imageUrl;

        int size = size();
        for (int i = 0; i < size; i++) get(i).setValue(updatedEvent.get(i).getValue());

        location = updatedEvent.location;
        ModelUtils.preserveList(guests, updatedEvent.guests);

        team.update(updatedEvent.team);
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

    public void setPlace(Place place) {
        items.get(LOCATION_POSITION).setValue(place.getName().toString());
        location = place.getLatLng();
    }

    public List<Guest> getGuests() {
        return guests;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeList(guests);
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
        private static final String NAME_KEY = "name";
        private static final String TEAM_KEY = "team";
        private static final String NOTES_KEY = "notes";
        private static final String IMAGE_KEY = "imageUrl";
        private static final String LOCATION_NAME_KEY = "locationName";
        private static final String START_DATE_KEY = "startDate";
        private static final String END_DATE_KEY = "endDate";
        private static final String LOCATION_KEY = "location";

        @Override
        public JsonElement serialize(Event src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(NAME_KEY, src.name);
            serialized.addProperty(NOTES_KEY, src.notes);
            serialized.addProperty(LOCATION_NAME_KEY, src.locationName);
            serialized.addProperty(TEAM_KEY, src.team.getId());
            serialized.addProperty(START_DATE_KEY, ModelUtils.dateFormatter.format(src.startDate));
            serialized.addProperty(END_DATE_KEY, ModelUtils.dateFormatter.format(src.endDate));

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
                return new Event(json.getAsString(), "", "", "", "", new Date(), new Date(), Team.empty(), null);
            }

            JsonObject eventJson = json.getAsJsonObject();

            String id = ModelUtils.asString(ID_KEY, eventJson);
            String name = ModelUtils.asString(NAME_KEY, eventJson);
            String notes = ModelUtils.asString(NOTES_KEY, eventJson);
            String imageUrl = ModelUtils.asString(IMAGE_KEY, eventJson);
            String locationName = ModelUtils.asString(LOCATION_NAME_KEY, eventJson);
            String startDate = ModelUtils.asString(START_DATE_KEY, eventJson);
            String endDate = ModelUtils.asString(END_DATE_KEY, eventJson);
            Team team = context.deserialize(eventJson.get(TEAM_KEY), Team.class);
            LatLng location = ModelUtils.parseCoordinates(LOCATION_KEY, eventJson);

            if (team == null) team = Team.empty();

            Event result = new Event(id, name, notes, imageUrl, locationName, ModelUtils.parseDate(startDate), ModelUtils.parseDate(endDate), team, location);

            ModelUtils.deserializeList(context, eventJson.get("guests"), result.guests, Guest.class);

            return result;
        }
    }
}
