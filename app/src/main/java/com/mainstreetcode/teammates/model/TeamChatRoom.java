package com.mainstreetcode.teammates.model;


import android.arch.persistence.room.Relation;
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
import com.mainstreetcode.teammates.repository.TeamChatRoomRepository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.mainstreetcode.teammates.model.TeamChat.KIND_TEXT;

public class TeamChatRoom extends TeamChatRoomEntity
        implements
        Model<TeamChatRoom> {

    @Relation(parentColumn = "team_chat_room_id", entityColumn = "team_chat_id", entity = TeamChat.class)
    private List<TeamChat> chats = new ArrayList<>();

    public TeamChatRoom(String id, Team team) {
        super(id, team);
    }

    protected TeamChatRoom(Parcel in) {
        super(in);
        chats = new ArrayList<>();
        in.readList(chats, TeamChat.class.getClassLoader());
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
        return team.getImageUrl();
    }

    @Override
    public void update(TeamChatRoom updated) {
        this.id = updated.id;
        team.update(updated.team);
        chats.clear();
        chats.addAll(updated.chats);
    }

    @Override
    public ModelRespository<TeamChatRoom> getRepository() {
        return TeamChatRoomRepository.getInstance();
    }

    public List<TeamChat> getChats() {
        return chats;
    }

    public void setChats(List<TeamChat> chats) {
        chats.clear();
        this.chats.addAll(chats);
    }

    public void add(TeamChat chat) {
        chats.add(chat);
    }

    public TeamChat chat(String content, User user) {
        return new TeamChat(null, content, KIND_TEXT, id, user, null);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeList(chats);
    }

    public static final Parcelable.Creator<TeamChatRoom> CREATOR = new Parcelable.Creator<TeamChatRoom>() {
        @Override
        public TeamChatRoom createFromParcel(Parcel in) {
            return new TeamChatRoom(in);
        }

        @Override
        public TeamChatRoom[] newArray(int size) {
            return new TeamChatRoom[size];
        }
    };

    public static class GsonAdapter
            implements
            JsonSerializer<TeamChatRoom>,
            JsonDeserializer<TeamChatRoom> {

        private static final String UID_KEY = "_id";
        private static final String TEAM_KEY = "team";
        private static final String CHAT_KEY = "chats";

        @Override
        public TeamChatRoom deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject chatRoomJson = json.getAsJsonObject();

            String id = ModelUtils.asString(UID_KEY, chatRoomJson);
            Team team = context.deserialize(chatRoomJson.get(TEAM_KEY), Team.class);
            List<TeamChat> chats = new ArrayList<>();

            ModelUtils.deserializeList(context, chatRoomJson.get(CHAT_KEY), chats, TeamChat.class);

            if (team == null) team = Team.empty();

            TeamChatRoom chatRoom = new TeamChatRoom(id, team);
            chatRoom.chats.addAll(chats);

            return chatRoom;
        }

        @Override
        public JsonElement serialize(TeamChatRoom src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject chatRoomJson = new JsonObject();

            chatRoomJson.addProperty(UID_KEY, src.id);
            chatRoomJson.addProperty(TEAM_KEY, src.team.getId());

            return chatRoomJson;
        }
    }
}
