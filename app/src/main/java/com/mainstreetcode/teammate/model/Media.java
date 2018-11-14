package com.mainstreetcode.teammate.model;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.persistence.entity.TeamEntity;
import com.mainstreetcode.teammate.persistence.entity.UserEntity;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.mainstreetcode.teammate.util.ObjectId;

import java.lang.reflect.Type;
import java.util.Date;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "team_media",
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class, parentColumns = "user_id", childColumns = "media_user", onDelete = CASCADE),
                @ForeignKey(entity = TeamEntity.class, parentColumns = "team_id", childColumns = "media_team", onDelete = CASCADE)
        }
)
public class Media implements
        TeamHost,
        Parcelable,
        Model<Media> {

    public static final String UPLOAD_KEY = "team-media";
    private static final String IMAGE = "image";

    @NonNull @PrimaryKey
    @ColumnInfo(name = "media_id") private String id;
    @ColumnInfo(name = "media_url") private String url;
    @ColumnInfo(name = "media_mime_type") private String mimeType;
    @ColumnInfo(name = "media_thumbnail") private String thumbnail;
    @ColumnInfo(name = "media_user") private User user;

    @ColumnInfo(name = "media_team") private Team team;
    @ColumnInfo(name = "media_created") private Date created;

    @ColumnInfo(name = "media_flagged") private boolean flagged;

    public Media(@NonNull String id, String url, String mimeType, String thumbnail,
                 User user, Team team, Date created, boolean flagged) {
        this.id = id;
        this.url = url;
        this.mimeType = mimeType;
        this.thumbnail = thumbnail;
        this.user = user;
        this.team = team;
        this.created = created;
        this.flagged = flagged;
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
        flagged = in.readByte() != 0x00;
    }

    public static Media fromUri(User user, Team team, Uri uri) {
        return new Media(new ObjectId().toHexString(), uri.toString(), "", "", user, team, new Date(), false);
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
        if (!(other instanceof Media)) return id.equals(other.getId());
        Media casted = (Media) other;
        return thumbnail.equals(casted.thumbnail) && url.equals(casted.getUrl());
    }

    @Override
    public Object getChangePayload(Identifiable other) {
        return other;
    }

    @Override
    public String getImageUrl() {
        return thumbnail;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getUrl() {
        return url;
    }

    public boolean isFlagged() {
        return flagged;
    }

    @Override
    public void update(Media updated) {
        id = updated.id;
        url = updated.url;
        thumbnail = updated.thumbnail;
        mimeType = updated.mimeType;
        created = updated.created;

        team.update(updated.team);
        user.update(updated.user);
        flagged = updated.flagged;
    }

    @Override
    public int compareTo(@NonNull Media o) {
        return created.compareTo(o.created);
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
        dest.writeString(thumbnail);
        dest.writeValue(user);
        dest.writeValue(team);
        dest.writeLong(created != null ? created.getTime() : -1L);
        dest.writeByte((byte) (flagged ? 0x01 : 0x00));
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
        private static final String FLAGGED_KEY = "flagged";

        @Override
        public Media deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject mediaJson = json.getAsJsonObject();

            String id = ModelUtils.asString(UID_KEY, mediaJson);
            String url = ModelUtils.asString(URL_KEY, mediaJson);
            String mimeType = ModelUtils.asString(MIME_TYPE_KEY, mediaJson);
            String thumbnail = ModelUtils.asString(THUMBNAIL_KEY, mediaJson);

            User user = context.deserialize(mediaJson.get(USER_KEY), User.class);
            Team team = context.deserialize(mediaJson.get(TEAM_KEY), Team.class);

            Date created = ModelUtils.parseDate(ModelUtils.asString(DATE_KEY, mediaJson));
            boolean flagged = ModelUtils.asBoolean(FLAGGED_KEY, mediaJson);

            if (user == null) user = User.empty();

            return new Media(id, url, mimeType, thumbnail, user, team, created, flagged);
        }

        @Override
        public JsonElement serialize(Media src, Type typeOfSrc, JsonSerializationContext context) {
//            JsonObject media = new JsonObject();
//            media.addProperty(MIME_TYPE_KEY, src.mimeType);
//            media.addProperty(URL_KEY, src.url);
//            media.addProperty(TEAM_KEY, src.team.getId());
//            media.addProperty(USER_KEY, src.user.getId());

            return new JsonPrimitive(src.getId());
        }
    }
}
