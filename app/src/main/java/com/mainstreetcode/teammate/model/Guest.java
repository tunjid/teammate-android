package com.mainstreetcode.teammate.model;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
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
import com.mainstreetcode.teammate.persistence.entity.EventEntity;
import com.mainstreetcode.teammate.persistence.entity.UserEntity;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = "event_guest",
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class, parentColumns = "user_id", childColumns = "guest_user", onDelete = CASCADE),
                @ForeignKey(entity = EventEntity.class, parentColumns = "event_id", childColumns = "guest_event", onDelete = CASCADE)
        }
)
public class Guest implements
        Parcelable,
        Model<Guest>,
        HeaderedModel<Guest>,
        ListableModel<Guest> {

    @NonNull @PrimaryKey
    @ColumnInfo(name = "guest_id") private String id;
    @ColumnInfo(name = "guest_user") private User user;
    @ColumnInfo(name = "guest_event") private Event event;
    @ColumnInfo(name = "guest_created") private Date created;
    @ColumnInfo(name = "guest_attending") private boolean attending;

    @Ignore private final List<Item<Guest>> items;

    Guest(@NonNull String id, User user, Event event, Date created, boolean attending) {
        this.id = id;
        this.user = user;
        this.event = event;
        this.created = created;
        this.attending = attending;
        items = buildItems();
    }

    private Guest(Parcel in) {
        id = in.readString();
        user = (User) in.readValue(User.class.getClassLoader());
        event = (Event) in.readValue(User.class.getClassLoader());
        long tmpCreated = in.readLong();
        created = tmpCreated != -1 ? new Date(tmpCreated) : null;
        attending = in.readByte() != 0x00;
        items = buildItems();
    }


    public static Guest forEvent(Event event, boolean attending) {
        return new Guest("", User.empty(), event, new Date(), attending);
    }

    private List<Item<Guest>> buildItems() {
        User user = getUser();
        return Arrays.asList(
                Item.text(0, Item.INPUT, R.string.first_name, user::getFirstName, ignored -> {}, this),
                Item.text(1, Item.INPUT, R.string.last_name, user::getLastName, ignored -> {}, this),
                Item.email(2, Item.INPUT, R.string.user_about, user::getAbout, ignored -> {}, this)
        );
    }

    @Override
    public Item<Guest> getHeaderItem() {
        return Item.text(0, Item.IMAGE, R.string.profile_picture, user::getImageUrl, imageUrl -> {}, this);
    }

    @Override
    public List<Item<Guest>> asItems() {
        return items;
    }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(id);
    }

    @NonNull
    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean areContentsTheSame(Identifiable other) {
        if (!(other instanceof Guest)) return id.equals(other.getId());
        Guest casted = (Guest) other;
        return attending == casted.attending;
    }

    @Override
    public Object getChangePayload(Identifiable other) {
        return other;
    }

    @Override
    public void reset() {
        attending = false;
        user.reset();
    }

    @Override
    public void update(Guest updated) {
        id = updated.id;
        attending = updated.attending;
        created = updated.created;
        user.update(updated.user);
    }

    @Override
    public int compareTo(@NonNull Guest o) {
        return created.compareTo(o.created);
    }

    public User getUser() {
        return user;
    }

    public Event getEvent() {
        return event;
    }

    public Date getCreated() {
        return created;
    }

    public boolean isAttending() {
        return attending;
    }

    @Override
    public String getImageUrl() {
        return user.getImageUrl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guest)) return false;

        Guest guest = (Guest) o;
        return id.equals(guest.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeValue(user);
        dest.writeValue(event);
        dest.writeLong(created != null ? created.getTime() : -1L);
        dest.writeByte((byte) (attending ? 0x01 : 0x00));
    }

    public static final Creator<Guest> CREATOR = new Creator<Guest>() {
        @Override
        public Guest createFromParcel(Parcel in) {
            return new Guest(in);
        }

        @Override
        public Guest[] newArray(int size) {
            return new Guest[size];
        }
    };

    public static class GsonAdapter
            implements
            JsonSerializer<Guest>,
            JsonDeserializer<Guest> {

        private static final String ID_KEY = "_id";
        private static final String USER_KEY = "user";
        private static final String EVENT_KEY = "event";
        private static final String DATE_KEY = "created";
        private static final String ATTENDING_KEY = "attending";

        @Override
        public Guest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject teamJson = json.getAsJsonObject();

            String id = ModelUtils.asString(ID_KEY, teamJson);
            User user = context.deserialize(teamJson.get(USER_KEY), User.class);
            Event event = context.deserialize(teamJson.get(EVENT_KEY), Event.class);
            Date created = ModelUtils.parseDate(ModelUtils.asString(DATE_KEY, teamJson));
            boolean attending = teamJson.get(ATTENDING_KEY).getAsBoolean();

            if (user == null) user = User.empty();

            return new Guest(id, user, event, created, attending);
        }

        @Override
        public JsonElement serialize(Guest src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject guest = new JsonObject();
            guest.addProperty(USER_KEY, src.user.getId());
            guest.addProperty(EVENT_KEY, src.event.getId());
            guest.addProperty(ATTENDING_KEY, src.isAttending());

            return guest;
        }
    }
}
