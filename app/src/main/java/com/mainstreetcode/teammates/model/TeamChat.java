package com.mainstreetcode.teammates.model;


import android.annotation.SuppressLint;
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
import com.mainstreetcode.teammates.notifications.Notifiable;
import com.mainstreetcode.teammates.notifications.Notifier;
import com.mainstreetcode.teammates.notifications.TeamChatNotifier;
import com.mainstreetcode.teammates.persistence.entity.TeamEntity;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = "team_chats",
        foreignKeys = @ForeignKey(entity = TeamEntity.class, parentColumns = "team_id", childColumns = "team_chat_team", onDelete = CASCADE)
)
public class TeamChat implements
        Parcelable,
        Model<TeamChat>,
        Notifiable<TeamChat> {

    @SuppressLint("SimpleDateFormat")
    private static final DateFormat CHAT_DATE_FORMAT = new SimpleDateFormat("h:mm a");
    private static final String KIND_TEXT = "text";

    @Ignore
    private transient boolean isSuccessful = true;

    @NonNull @PrimaryKey
    @ColumnInfo(name = "team_chat_id") private String id;
    @ColumnInfo(name = "team_chat_kind") private String kind;
    @ColumnInfo(name = "team_chat_content") private String content;

    @ColumnInfo(name = "team_chat_user") private User user;
    @ColumnInfo(name = "team_chat_team") private Team team;

    @ColumnInfo(name = "team_chat_created") private Date created;

    public TeamChat(@NonNull String id, String content, String kind,
                    User user, Team team, Date created) {
        this.id = id;
        this.content = content;
        this.kind = kind;
        this.user = user;
        this.team = team;
        this.created = created;
    }

    private TeamChat(Parcel in) {
        id = in.readString();
        kind = in.readString();
        content = in.readString();
        user = (User) in.readValue(User.class.getClassLoader());
        team = (Team) in.readValue(Team.class.getClassLoader());
        long tmpCreated = in.readLong();
        created = tmpCreated != -1 ? new Date(tmpCreated) : null;
    }

    public static TeamChat chat(String content, User user, Team team) {
        return new TeamChat("", content, KIND_TEXT, user, team, new Date());
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
    public String getImageUrl() {
        return user == null ? null : user.getImageUrl();
    }

    @Override
    public void update(TeamChat updated) {
        id = updated.id;
        kind = updated.kind;
        content = updated.content;
        created = updated.created;
        isSuccessful = updated.isSuccessful;

        user.update(updated.user);
        team.update(updated.team);
    }

    @Override
    public int compareTo(@NonNull TeamChat o) {
        return created.compareTo(o.created);
    }

    @Override
    public Notifier<TeamChat> getNotifier() {
        return TeamChatNotifier.getInstance();
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

    public Team getTeam() {
        return team;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedDate() {
        if (created == null) return "";
        return CHAT_DATE_FORMAT.format(created);
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamChat)) return false;

        TeamChat chat = (TeamChat) o;

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
        dest.writeString(kind);
        dest.writeString(content);
        dest.writeValue(user);
        dest.writeValue(team);
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
        private static final String USER_KEY = "user";
        private static final String TEAM_KEY = "team";
        private static final String DATE_KEY = "created";

        @Override
        public TeamChat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject teamJson = json.getAsJsonObject();

            String id = ModelUtils.asString(UID_KEY, teamJson);
            String kind = ModelUtils.asString(KIND_KEY, teamJson);
            String content = ModelUtils.asString(CONTENT_KEY, teamJson);

            User user = context.deserialize(teamJson.get(USER_KEY), User.class);
            Team team = context.deserialize(teamJson.get(TEAM_KEY), Team.class);
            Date created = ModelUtils.parseDate(ModelUtils.asString(DATE_KEY, teamJson));

            if (user == null) user = User.empty();
            if (team == null) team = Team.empty();

            return new TeamChat(id, content, kind, user, team, created);
        }

        @Override
        public JsonElement serialize(TeamChat src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject team = new JsonObject();
            team.addProperty(KIND_KEY, src.kind);
            team.addProperty(CONTENT_KEY, src.content);
            team.addProperty(USER_KEY, src.user.getId());
            team.addProperty(TEAM_KEY, src.team.getId());

            return team;
        }
    }
}
