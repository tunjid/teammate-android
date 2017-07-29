package com.mainstreetcode.teammates.model;

import android.arch.persistence.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.persistence.entity.EventEntity;
import com.mainstreetcode.teammates.rest.TeammateService;

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
        ItemListableBean<Event> {

    public static final int LOGO_POSITION = 0;
    public static final String PHOTO_UPLOAD_KEY = "event-photo";

    @Ignore private List<User> attendees = new ArrayList<>();
    @Ignore private List<User> absentees = new ArrayList<>();
    @Ignore private final List<Item<Event>> items;

    public static Event empty() {
        Date date = new Date();
        return new Event("", "", "", "", date, date, Team.empty());
    }

    public Event(String id, String name, String notes, String imageUrl, Date startDate, Date endDate, Team team) {
        super(id, name, notes, imageUrl, startDate, endDate, team);
        this.team = team;
        items = buildItems();
    }

    protected Event(Parcel in) {
        super(in);
        in.readList(attendees, User.class.getClassLoader());
        in.readList(absentees, User.class.getClassLoader());
        items = buildItems();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Item<Event>> buildItems() {
        return Arrays.asList(
                new Item(Item.IMAGE, R.string.team_logo, imageUrl, this::setImageUrl, Event.class),
                new Item(Item.INPUT, R.string.event_name, name == null ? "" : name, this::setName, Event.class),
                new Item(Item.INPUT, R.string.notes, notes == null ? "" : notes, this::setNotes, Event.class),
                new Item(Item.DATE, R.string.start_date, prettyPrinter.format(startDate), this::setStartDate, Event.class),
                new Item(Item.DATE, R.string.end_date, prettyPrinter.format(endDate), this::setEndDate, Event.class)
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
    public boolean isEmpty() {
        return equals(empty());
    }

    @Override
    public void update(Event updatedEvent) {
        this.id = updatedEvent.getId();

        int size = size();
        for (int i = 0; i < size; i++) get(i).setValue(updatedEvent.get(i).getValue());

        attendees.clear();
        absentees.clear();

        attendees.addAll(updatedEvent.attendees);
        absentees.addAll(updatedEvent.absentees);

        team.update(updatedEvent.team);
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public List<User> getAttendees() {
        return attendees;
    }

    public List<User> getAbsentees() {
        return absentees;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeList(attendees);
        dest.writeList(absentees);
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
        private static final String START_DATE_KEY = "startDate";
        private static final String END_DATE_KEY = "endDate";

        @Override
        public JsonElement serialize(Event src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(NAME_KEY, src.name);
            serialized.addProperty(NOTES_KEY, src.notes);
            serialized.addProperty(TEAM_KEY, src.team.getId());
            serialized.addProperty(START_DATE_KEY, dateFormatter.format(src.startDate));
            serialized.addProperty(END_DATE_KEY, dateFormatter.format(src.endDate));

            return serialized;
        }

        @Override
        public Event deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject roleJson = json.getAsJsonObject();

            String id = ModelUtils.asString(ID_KEY, roleJson);
            String name = ModelUtils.asString(NAME_KEY, roleJson);
            String notes = ModelUtils.asString(NOTES_KEY, roleJson);
            String imageUrl = TeammateService.API_BASE_URL + ModelUtils.asString(IMAGE_KEY, roleJson);
            String startDate = ModelUtils.asString(START_DATE_KEY, roleJson);
            String endDate = ModelUtils.asString(END_DATE_KEY, roleJson);
            Team team = context.deserialize(roleJson.get(TEAM_KEY), Team.class);

            Event result = new Event(id, name, notes, imageUrl, parseDate(startDate), parseDate(endDate), team);

            ModelUtils.deserializeList(context, roleJson.get("attendees"), result.attendees, User.class);
            ModelUtils.deserializeList(context, roleJson.get("absentees"), result.absentees, User.class);

            return result;
        }
    }
}
