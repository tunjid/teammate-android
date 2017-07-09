package com.mainstreetcode.teammates.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.ListableBean;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Event events
 */
@Entity(
        tableName = "events",
        foreignKeys = @ForeignKey(entity = Event.class, parentColumns = "id", childColumns = "teamId")
)
public class Event implements
        Parcelable,
        ListableBean<Event, Item> {

    public static final SimpleDateFormat prettyPrinter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.US);

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
    private static final SimpleDateFormat timePrinter = new SimpleDateFormat("HH:mm", Locale.US);

    @PrimaryKey
    private String id;
    private String name;
    private String notes;
    private String imageUrl;
    private Team team;
    private Date startDate;
    private Date endDate;

    @Ignore private List<User> attendees = new ArrayList<>();
    @Ignore private List<User> absentees = new ArrayList<>();
    @Ignore private final List<Item<Event>> items;

    public static Event empty(Team team) {
        Date now = new Date();
        return new Event("*", "*", "*", "*", Team.empty(), now, now);
    }

    public Event(String id, String name, String notes, String imageUrl, Team team, Date startDate, Date endDate) {
        this.id = id;
        this.name = name;
        this.team = team;
        this.notes = notes;
        this.imageUrl = imageUrl;
        this.startDate = startDate;
        this.endDate = endDate;

        items = itemsFromEvent(this);
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
    public Event toSource() {
        return null;
    }

    public static class GsonAdapter
            implements
            JsonDeserializer<Event> {

        private static final String ID_KEY = "_id";
        private static final String NAME_KEY = "name";
        private static final String TEAM_KEY = "team";
        private static final String NOTES_KEY = "notes";
        private static final String IMAGE_KEY = "imageUrl";
        private static final String START_DATE_KEY = "startDate";
        private static final String END_DATE_KEY = "endDate";

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

            return new Event(id, name, notes, imageUrl, team, parseDate(startDate), parseDate(endDate));
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Item<Event>> itemsFromEvent(Event event) {
        return Arrays.asList(
                new Item(Item.IMAGE, R.string.team_logo, event.imageUrl, event::setImageUrl, Event.class),
                new Item(Item.INPUT, R.string.event_name, event.name == null ? "" : event.name, event::setName, Event.class),
                new Item(Item.INPUT, R.string.notes, event.notes == null ? "" : event.notes, event::setNotes, Event.class),
                new Item(Item.DATE, R.string.start_date, prettyPrinter.format(event.startDate), event::setStartDate, Event.class),
                new Item(Item.DATE, R.string.end_date, prettyPrinter.format(event.endDate), event::setEndDate, Event.class)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;

        Event event = (Event) o;

        return id.equals(event.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        String time = prettyPrinter.format(startDate) + " - ";
        time += endsSameDay() ? timePrinter.format(startDate) : prettyPrinter.format(endDate);
        return time;
    }

    public Team getTeam() {
        return team;
    }

    private boolean endsSameDay() {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        start.setTime(startDate);
        end.setTime(endDate);
        return start.get(Calendar.YEAR) == end.get(Calendar.YEAR)
                && start.get(Calendar.MONTH) == end.get(Calendar.MONTH)
                && start.get(Calendar.DATE) == end.get(Calendar.DATE);
    }

    private static Date parseDate(String date) {
        return parseDate(date, dateFormatter);
    }

    public static Date parseDate(String date, SimpleDateFormat formatter) {
        try {
            return formatter.parse(date);
        }
        catch (ParseException e) {
            return new Date();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setStartDate(String startDate) {
        this.startDate = parseDate(startDate, prettyPrinter);
    }

    public void setEndDate(String endDate) {
        this.endDate = parseDate(endDate, prettyPrinter);
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    protected Event(Parcel in) {
        id = in.readString();
        name = in.readString();
        notes = in.readString();
        imageUrl = in.readString();
        startDate = new Date(in.readLong());
        endDate = new Date(in.readLong());
        team = (Team) in.readValue(Team.class.getClassLoader());
        in.readList(attendees, User.class.getClassLoader());
        in.readList(absentees, User.class.getClassLoader());

        items = itemsFromEvent(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(notes);
        dest.writeString(imageUrl);
        dest.writeLong(startDate != null ? startDate.getTime() : -1L);
        dest.writeLong(endDate != null ? endDate.getTime() : -1L);
        dest.writeValue(team);
        dest.writeList(attendees);
        dest.writeList(absentees);
    }

    @SuppressWarnings("unused")
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
}
