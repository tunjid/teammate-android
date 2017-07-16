package com.mainstreetcode.teammates.model;

import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Relation;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.persistence.entity.RoleEntity;
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
        ListableBean<Team, Item> {

    public static final int LOGO_POSITION = 0;
    private static final int NAME_POSITION = 1;
    private static final int CITY_POSITION = 2;
    private static final int STATE_POSITION = 3;
    public static final int ZIP_POSITION = 4;
    public static final int ROLE_POSITION = 5;

    public static final String PHOTO_UPLOAD_KEY = "team-photo";
    private static final String NEW_TEAM = "new.team";

    private static final Team EMPTY = new Team(NEW_TEAM, "", "", "", "", "");

    @Relation(parentColumn = "team_id", entityColumn = "role_team_id", entity = RoleEntity.class)
    private List<Role> roles = new ArrayList<>();

    @Ignore List<JoinRequest> joinRequests = new ArrayList<>();

    @Ignore private final List<Item<Team>> items;

    public static Team empty() {
        return EMPTY;
    }

    public Team(String id, String name, String city, String state, String zip, String imageUrl) {
        super(id, name, city, state, zip, imageUrl);

        items = itemsFromTeam(this);
    }

    private Team(Team source) {
        super(source.id, source.get(NAME_POSITION).value,
                source.get(CITY_POSITION).value, source.get(STATE_POSITION).value,
                source.get(ZIP_POSITION).value, source.get(LOGO_POSITION).value);

        this.items = itemsFromTeam(source);
        this.roles.addAll(source.roles);
    }

    @SuppressWarnings("unchecked")
    private static List<Item<Team>> itemsFromTeam(Team team) {
        return Arrays.asList(
                new Item(Item.IMAGE, R.string.team_logo, team.imageUrl, team::setImageUrl, Team.class),
                new Item(Item.INPUT, R.string.team_name, R.string.team_info, team.name == null ? "" : team.name, team::setName, Team.class),
                new Item(Item.INPUT, R.string.city, team.city == null ? "" : team.city, team::setCity, Team.class),
                new Item(Item.INPUT, R.string.state, team.state == null ? "" : team.state, team::setState, Team.class),
                new Item(Item.INPUT, R.string.zip, team.zip == null ? "" : team.zip, team::setZip, Team.class),
                new Item(Item.ROLE, R.string.team_role, R.string.team_role, "", null, Team.class)
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
        private static final String LOGO_KEY = "imageUrl";
        private static final String ROLES_KEY = "roles";
        private static final String JOIN_REQUEST_KEY = "joinRequests";

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

            team.get(LOGO_POSITION).setValue(logoUrl);
            team.get(ROLE_POSITION).setValue(role);

            ModelUtils.deserializeList(context, teamJson.get(ROLES_KEY), team.roles, Role.class);
            ModelUtils.deserializeList(context, teamJson.get(JOIN_REQUEST_KEY), team.joinRequests, JoinRequest.class);

            return team;
        }

        @Override
        public JsonElement serialize(Team src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject team = new JsonObject();
            team.addProperty(NAME_KEY, src.name);
            team.addProperty(CITY_KEY, src.city);
            team.addProperty(STATE_KEY, src.state);
            team.addProperty(ZIP_KEY, src.zip);

            return team;
        }
    }

    // This because of a room bug
    public void setRoles(List<Role> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }

    public void update(Team updatedTeam) {
        int size = size();
        for (int i = 0; i < size; i++) get(i).setValue(updatedTeam.get(i).getValue());

        roles.clear();
        joinRequests.clear();

        roles.addAll(updatedTeam.getRoles());
        joinRequests.addAll(updatedTeam.getJoinRequests());
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

    @Nullable
    public Role getRoleForUser(User user) {
        for (Role role : roles) if (role.getUser().equals(user)) return role;
        return null;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public List<JoinRequest> getJoinRequests() {
        return joinRequests;
    }

    private Team(Parcel in) {
        super(in);
        in.readList(joinRequests, User.class.getClassLoader());

        items = itemsFromTeam(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeList(roles);
        dest.writeList(joinRequests);
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
