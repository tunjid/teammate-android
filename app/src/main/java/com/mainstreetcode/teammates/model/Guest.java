package com.mainstreetcode.teammates.model;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
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
import com.mainstreetcode.teammates.persistence.entity.EventEntity;
import com.mainstreetcode.teammates.persistence.entity.UserEntity;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Date;

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
        Model<Guest> {

    @NonNull @PrimaryKey
    @ColumnInfo(name = "guest_id") private String id;
    @ColumnInfo(name = "guest_user") private User user;
    @ColumnInfo(name = "guest_event") private Event event;
    @ColumnInfo(name = "guest_created") private Date created;
    @ColumnInfo(name = "guest_attending") private boolean attending;

    Guest(@NonNull String id, User user, Event event, Date created, boolean attending) {
        this.id = id;
        this.user = user;
        this.event = event;
        this.created = created;
        this.attending = attending;
    }

    private Guest(Parcel in) {
        id = in.readString();
        user = (User) in.readValue(User.class.getClassLoader());
        event = (Event) in.readValue(User.class.getClassLoader());
        long tmpCreated = in.readLong();
        created = tmpCreated != -1 ? new Date(tmpCreated) : null;
        attending = in.readByte() != 0x00;
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
    public void reset() {
        attending = false;
        user.reset();
    }

    @Override
    public void update(Guest updated) {
        id = updated.id;
        created = updated.created;
        user.update(updated.user);
        attending = updated.attending;
    }

    @Override
    public int compareTo(@NonNull Guest o) {
        return created.compareTo(o.created);
    }

    public User getUser() {
        return user;
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
    public boolean areContentsTheSame(Identifiable other) {
        if (other instanceof Guest) return attending == ((Guest) other).attending;
        return id.equals(other.getId());
    }

    @Override
    public Object getChangePayload(Identifiable other) {
        return attending;
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
