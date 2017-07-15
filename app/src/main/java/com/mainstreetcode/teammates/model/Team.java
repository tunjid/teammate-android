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
import com.mainstreetcode.teammates.persistence.entity.TeamEntity;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.ListableBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Teams
 */

public class Team extends TeamEntity implements
        Parcelable,
        ListableBean<Team, Item> {

    public static final int LOGO_POSITION = 0;
    private static final int NAME_POSITION = 1;
    private static final int CITY_POSITION = 2;
    private static final int STATE_POSITION = 3;
    public static final int ZIP_POSITION = 4;
    private static final int ROLE_POSITION = 5;

    public static final String PHOTO_UPLOAD_KEY = "team-photo";
    private static final String NEW_TEAM = "new.team";

    private static final Team EMPTY = new Team(NEW_TEAM, "", "", "", "", "");

    @Ignore private String role;

    // Cannot be flattened in SQL
    //@Relation(parentColumn = "id", entityColumn = "teamId", entity = Role.class)
    @Ignore List<User> users = new ArrayList<>();
    @Ignore List<User> pendingUsers = new ArrayList<>();

    @Ignore private final List<Item<Team>> items;

    public static Team empty() {
        return EMPTY;
    }

    public Team(String id, String name, String city, String state, String zip, String logoUrl) {
        super(id, name, city, state, zip, logoUrl);
        items = itemsFromTeam(this);
    }

    private Team(Team source) {
        super(source.id, source.get(NAME_POSITION).value,
                source.get(CITY_POSITION).value, source.get(STATE_POSITION).value,
                source.get(ZIP_POSITION).value, source.get(LOGO_POSITION).value);

        this.items = itemsFromTeam(source);
        this.users.addAll(source.users);
    }

    @SuppressWarnings("unchecked")
    private static List<Item<Team>> itemsFromTeam(Team team) {
        return Arrays.asList(
                new Item(Item.IMAGE, R.string.team_logo, team.logoUrl, team::setLogoUrl, Team.class),
                new Item(Item.INPUT, R.string.team_name, R.string.team_info, team.name == null ? "" : team.name, team::setName, Team.class),
                new Item(Item.INPUT, R.string.city, team.city == null ? "" : team.city, team::setCity, Team.class),
                new Item(Item.INPUT, R.string.state, team.state == null ? "" : team.state, team::setState, Team.class),
                new Item(Item.INPUT, R.string.zip, team.zip == null ? "" : team.zip, team::setZip, Team.class),
                new Item(Item.ROLE, R.string.team_role, R.string.team_role, team.role == null ? "" : team.role, team::setRole, Team.class)
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
    public Team toSource() {
        return new Team(this);
    }


    public static class GsonAdapter
            implements
            JsonSerializer<Team>,
            JsonDeserializer<Team> {

        private static final String UID_KEY = "_id";
        private static final String NAME_KEY = "name";
        private static final String CITY_KEY = "city";
        private static final String STATE_KEY = "state";
        private static final String ZIP_KEY = "zip";
        private static final String ROLE_KEY = "role";
        private static final String USERS_KEY = "users";
        private static final String LOGO_KEY = "imageUrl";
        private static final String PENDING_USERS_KEY = "pendingUsers";

        @Override
        public Team deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject teamJson = json.getAsJsonObject();

            String id = ModelUtils.asString(UID_KEY, teamJson);
            String name = ModelUtils.asString(NAME_KEY, teamJson);
            String city = ModelUtils.asString(CITY_KEY, teamJson);
            String state = ModelUtils.asString(STATE_KEY, teamJson);
            String zip = ModelUtils.asString(ZIP_KEY, teamJson);
            String role = ModelUtils.asString(ROLE_KEY, teamJson);
            String logoUrl = TeammateService.API_BASE_URL + ModelUtils.asString(LOGO_KEY, teamJson);

            Team team = new Team(id, name, city, state, zip, logoUrl);
            team.setRole(role);

            team.get(LOGO_POSITION).setValue(logoUrl);
            team.get(ROLE_POSITION).setValue(role);

            ModelUtils.deserializeList(context, teamJson.get(USERS_KEY), team.users, User.class);
            ModelUtils.deserializeList(context, teamJson.get(PENDING_USERS_KEY), team.pendingUsers, User.class);

            return team;
        }

        @Override
        public JsonElement serialize(Team src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject team = new JsonObject();
            //team.addProperty(UID_KEY, src.id);
            team.addProperty(NAME_KEY, src.name);
            team.addProperty(CITY_KEY, src.city);
            team.addProperty(STATE_KEY, src.state);
            team.addProperty(ZIP_KEY, src.zip);
            team.addProperty(ROLE_KEY, src.role);

            return team;
        }
    }

    public void update(Team updatedTeam) {
        int size = size();
        for (int i = 0; i < size; i++) get(i).setValue(updatedTeam.get(i).getValue());

        users.clear();
        pendingUsers.clear();

        users.addAll(updatedTeam.getUsers());
        pendingUsers.addAll(updatedTeam.getPendingUsers());
    }

    public boolean isNewTeam() {
        return NEW_TEAM.equals(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team)) return false;

        Team team = (Team) o;

        //return uid.equals(team.uid);
        return id.equals(team.id);
    }

    @Override
    public int hashCode() {
        //return uid.hashCode();
        return id.hashCode();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<User> getPendingUsers() {
        return pendingUsers;
    }

    private Team(Parcel in) {
        super(in.readString(), in.readString(), in.readString(),
                in.readString(), in.readString(), in.readString());
        role = in.readString();
        in.readList(users, User.class.getClassLoader());
        in.readList(pendingUsers, User.class.getClassLoader());

        items = itemsFromTeam(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(zip);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(logoUrl);
        dest.writeString(role);
        dest.writeList(users);
        dest.writeList(pendingUsers);
    }

    public static final Parcelable.Creator<Team> CREATOR = new Parcelable.Creator<Team>() {
        @Override
        public Team createFromParcel(Parcel in) {
            return new Team(in);
        }

        @Override
        public Team[] newArray(int size) {
            return new Team[size];
        }
    };

}
