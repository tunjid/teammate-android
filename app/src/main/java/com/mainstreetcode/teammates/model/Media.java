package com.mainstreetcode.teammates.model;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammates.notifications.MediaNotifier;
import com.mainstreetcode.teammates.notifications.Notifiable;
import com.mainstreetcode.teammates.notifications.Notifier;
import com.mainstreetcode.teammates.persistence.entity.TeamEntity;
import com.mainstreetcode.teammates.persistence.entity.UserEntity;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = "team_media",
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class, parentColumns = "user_id", childColumns = "media_user", onDelete = CASCADE),
                @ForeignKey(entity = TeamEntity.class, parentColumns = "team_id", childColumns = "media_team", onDelete = CASCADE)
        }
)
public class Media implements
        Parcelable,
        Model<Media>,
        Notifiable<Media> {

    public static final String UPLOAD_KEY = "team-media";
    public static final String IMAGE = "image";

    @PrimaryKey
    @ColumnInfo(name = "media_id") private String id;
    @ColumnInfo(name = "media_url") private String url;
    @ColumnInfo(name = "media_mime_type") private String mimeType;
    @ColumnInfo(name = "media_thumbnail") private String thumbnail;

    @ColumnInfo(name = "media_user") private User user;
    @ColumnInfo(name = "media_team") private Team team;
    @ColumnInfo(name = "media_created") private Date created;

    public Media(String id, String url, String mimeType, String thumbnail, User user, Team team, Date created) {
        this.id = id;
        this.url = url;
        this.mimeType = mimeType;
        this.thumbnail = thumbnail;
        this.user = user;
        this.team = team;
        this.created = created;
    }

    private Media(Parcel in) {
        id = in.readString();
        url = in.readString();
        mimeType = in.readString();
        thumbnail = in.readString();
        user = (User) in.readValue(User.class.getClassLoader());
        team = (Team) in.readValue(Team.class.getClassLoader());
        long tmpCreated = in.readLong();
        created = tmpCreated != -1 ? new Date(tmpCreated) : null;
    }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(id);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getImageUrl() {
        return url;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void update(Media updated) {
        id = updated.id;
        url = updated.url;
        mimeType = updated.mimeType;
        created = updated.created;

        team.update(updated.team);
        user.update(updated.user);
    }

    @Override
    public Notifier<Media> getNotifier() {
        return MediaNotifier.getInstance();
    }

    public User getUser() {
        return user;
    }

    public Team getTeam() {
        return team;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Date getCreated() {
        return created;
    }

    public boolean isImage() {
        return mimeType.startsWith(IMAGE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Media)) return false;

        Media chat = (Media) o;

        return id.equals(chat.id);
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
        dest.writeString(url);
        dest.writeString(mimeType);
        dest.writeValue(user);
        dest.writeValue(team);
        dest.writeLong(created != null ? created.getTime() : -1L);
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };

    public static class GsonAdapter
            implements
            JsonSerializer<Media>,
            JsonDeserializer<Media> {

        private static final String UID_KEY = "_id";
        private static final String URL_KEY = "url";
        private static final String MIME_TYPE_KEY = "mimetype";
        private static final String THUMBNAIL_KEY = "thumbnail";
        private static final String USER_KEY = "user";
        private static final String TEAM_KEY = "team";
        private static final String DATE_KEY = "created";

        @Override
        public Media deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject teamJson = json.getAsJsonObject();

            String id = ModelUtils.asString(UID_KEY, teamJson);
            String url = ModelUtils.asString(URL_KEY, teamJson);
            String mimeType = ModelUtils.asString(MIME_TYPE_KEY, teamJson);
            String thumbnail = ModelUtils.asString(THUMBNAIL_KEY, teamJson);

            User user = context.deserialize(teamJson.get(USER_KEY), User.class);
            Team team = context.deserialize(teamJson.get(TEAM_KEY), Team.class);

            Date created = ModelUtils.parseDate(ModelUtils.asString(DATE_KEY, teamJson));

            if (user == null) user = User.empty();

            return new Media(id, url, mimeType, thumbnail, user, team, created);
        }

        @Override
        public JsonElement serialize(Media src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject media = new JsonObject();
            media.addProperty(MIME_TYPE_KEY, src.mimeType);
            media.addProperty(URL_KEY, src.url);
            media.addProperty(TEAM_KEY, src.team.getId());
            media.addProperty(USER_KEY, src.user.getId());

            return media;
        }
    }
}
