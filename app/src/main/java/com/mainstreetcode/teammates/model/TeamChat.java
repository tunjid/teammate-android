package com.mainstreetcode.teammates.model;


import android.annotation.SuppressLint;
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
import com.mainstreetcode.teammates.persistence.entity.TeamChatRoomEntity;
import com.mainstreetcode.teammates.repository.ModelRespository;
import com.mainstreetcode.teammates.repository.TeamChatRepository;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = "team_chats",
        foreignKeys = @ForeignKey(entity = TeamChatRoomEntity.class, parentColumns = "team_chat_room_id", childColumns = "parent_chat_room_id", onDelete = CASCADE)
)
public class TeamChat implements
        Parcelable,
        Model<TeamChat> {

    @SuppressLint("SimpleDateFormat")
    private static final DateFormat CHAT_DATE_FORMAT = new SimpleDateFormat("h:mm a");
    static final String KIND_TEXT = "text";

    @PrimaryKey
    @ColumnInfo(name = "team_chat_id") private String id;
    @ColumnInfo(name = "team_chat_kind") private String kind;
    @ColumnInfo(name = "team_chat_content") private String content;
    @ColumnInfo(name = "parent_chat_room_id") private String teamRoomId;

    @ColumnInfo(name = "team_chat_user") private User user;
    @ColumnInfo(name = "team_chat_created") private Date created;

    public TeamChat(String id, String content, String kind, String teamRoomId, User user, Date created) {
        this.id = id;
        this.content = content;
        this.kind = kind;
        this.teamRoomId = teamRoomId;
        this.user = user;
        this.created = created;
    }

    private TeamChat(Parcel in) {
        id = in.readString();
        kind = in.readString();
        content = in.readString();
        teamRoomId = in.readString();
        user = (User) in.readValue(User.class.getClassLoader());
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
        return user == null ? null : user.getImageUrl();
    }

    @Override
    public void update(TeamChat updated) {
        id = updated.id;
        kind = updated.kind;
        content = updated.content;
        teamRoomId = updated.teamRoomId;
        created = updated.created;

        user.update(updated.user);
    }

    @Override
    public ModelRespository<TeamChat> getRepository() {
        return TeamChatRepository.getInstance();
    }

    public String getTeamRoomId() {
        return teamRoomId;
    }

    public String getKind() {
        return kind;
    }

    public Date getCreated() {
        return created;
    }

    public User getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedDate() {
        if (created == null) return "";
        return CHAT_DATE_FORMAT.format(created);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(kind);
        dest.writeString(content);
        dest.writeString(teamRoomId);
        dest.writeValue(user);
        dest.writeLong(created != null ? created.getTime() : -1L);
    }

    public static final Parcelable.Creator<TeamChat> CREATOR = new Parcelable.Creator<TeamChat>() {
        @Override
        public TeamChat createFromParcel(Parcel in) {
            return new TeamChat(in);
        }

        @Override
        public TeamChat[] newArray(int size) {
            return new TeamChat[size];
        }
    };

    public static class GsonAdapter
            implements
            JsonSerializer<TeamChat>,
            JsonDeserializer<TeamChat> {

        private static final String UID_KEY = "_id";
        private static final String KIND_KEY = "kind";
        private static final String CONTENT_KEY = "content";
        private static final String TEAM_ROOM_ID_KEY = "teamChatRoom";
        private static final String USER_KEY = "user";
        private static final String DATE_KEY = "created";

        @Override
        public TeamChat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject teamJson = json.getAsJsonObject();

            String id = ModelUtils.asString(UID_KEY, teamJson);
            String kind = ModelUtils.asString(KIND_KEY, teamJson);
            String content = ModelUtils.asString(CONTENT_KEY, teamJson);
            String teamRoomId = ModelUtils.asString(TEAM_ROOM_ID_KEY, teamJson);

            User user = context.deserialize(teamJson.get(USER_KEY), User.class);
            Date created = ModelUtils.parseDate(ModelUtils.asString(DATE_KEY, teamJson));

            if (user == null) user = User.empty();

            return new TeamChat(id, content, kind, teamRoomId, user, created);
        }

        @Override
        public JsonElement serialize(TeamChat src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject team = new JsonObject();
            team.addProperty(KIND_KEY, src.kind);
            team.addProperty(CONTENT_KEY, src.content);
            team.addProperty(TEAM_ROOM_ID_KEY, src.teamRoomId);
            team.addProperty(USER_KEY, src.user.getId());

            return team;
        }
    }
}
