package com.mainstreetcode.teammates.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.util.ListableBean;

import java.lang.annotation.Retention;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Teams
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

@Entity(tableName = "teams")
public class Team implements
        Parcelable,
        ListableBean<Team, Team.Item> {

    private static final int NAME_POSITION = 2;
    private static final int CITY_POSITION = 3;
    private static final int STATE_POSITION = 4;
    public static final int ZIP_POSITION = 5;
    public static final int ROLE_POSITION = 7;

    @Retention(SOURCE)
    @IntDef({HEADING, INPUT, IMAGE, ROLE})
    @interface ItemType {}

    public static final int HEADING = 1;
    public static final int INPUT = 2;
    public static final int IMAGE = 3;
    public static final int ROLE = 4;

    @PrimaryKey
    private String id;
    private String name;
    private String city;
    private String state;
    private String zip;

    // Cannot be flattened in SQL
    @Ignore List<User> users = new ArrayList<>();
    @Ignore List<User> pendingUsers = new ArrayList<>();

    @Ignore private final List<Item> items;

    public static Team empty() {
        return new Team();
    }

    private Team() {
        items = itemsFromTeam(this);
    }

    public Team(String id, String name, String city, String state, String zip) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.state = state;
        this.zip = zip;
        items = itemsFromTeam(this);
    }

    private Team(Team source) {
        this.id = source.id;
        this.name = source.get(NAME_POSITION).value;
        this.city = source.get(CITY_POSITION).value;
        this.state = source.get(STATE_POSITION).value;
        this.zip = source.get(ZIP_POSITION).value;

        this.items = itemsFromTeam(source);
        this.users.addAll(source.users);
    }

    private static List<Item> itemsFromTeam(Team team) {

        return Arrays.asList(
                new Item(IMAGE, R.string.team_logo, "", null),
                new Item(HEADING, R.string.team_info, "", null),
                new Item(INPUT, R.string.team_name, team.name == null ? "" : team.name, team::setName),
                new Item(INPUT, R.string.city, team.city == null ? "" : team.city, team::setCity),
                new Item(INPUT, R.string.state, team.state == null ? "" : team.state, team::setState),
                new Item(INPUT, R.string.zip, team.zip == null ? "" : team.zip, team::setZip),
                new Item(HEADING, R.string.team_role, "", null),
                new Item(ROLE, R.string.team_role, "", null)
        );
    }

    @Override
    public int size() {
        return 8;
    }

    @Override
    public Item get(int position) {
        return items.get(position);
    }

    @Override
    public Team toSource() {
        return new Team(this);
    }


    public static class JsonDeserializer implements com.google.gson.JsonDeserializer<Team> {

        private static final String UID_KEY = "_id";
        private static final String NAME_KEY = "name";
        private static final String CITY_KEY = "city";
        private static final String STATE_KEY = "state";
        private static final String ZIP_KEY = "zip";
        private static final String USERS_KEY = "users";
        private static final String PENDING_USERS_KEY = "pendingUsers";

        @Override
        public Team deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject teamJson = json.getAsJsonObject();

            String id = ModelUtils.asString(UID_KEY, teamJson);
            String name = ModelUtils.asString(NAME_KEY, teamJson);
            String city = ModelUtils.asString(CITY_KEY, teamJson);
            String state = ModelUtils.asString(STATE_KEY, teamJson);
            String zip = ModelUtils.asString(ZIP_KEY, teamJson);

            Team team = new Team(id, name, city, state, zip);

            ModelUtils.deserializeList(context, teamJson.get(USERS_KEY), team.users, User.class);
            ModelUtils.deserializeList(context, teamJson.get(PENDING_USERS_KEY), team.pendingUsers, User.class);

            return team;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team)) return false;

        Team team = (Team) o;

        //return uid.equals(team.uid);
        return id.equals(team.name);
    }

    @Override
    public int hashCode() {
        //return uid.hashCode();
        return name.hashCode();
    }

    public String getId() {return this.id;}

    public String getName() {return this.name;}

    public String getCity() {return this.city;}

    @SuppressWarnings("unused")
    public String getState() {
        return state;
    }

    @SuppressWarnings("unused")
    public String getZip() {
        return zip;
    }

    private void setName(String name) {this.name = name; }

    private void setCity(String city) {this.city = city; }

    private void setState(String state) {this.state = state; }

    private void setZip(String zip) {this.zip = zip; }

    public List<User> getUsers() {
        return users;
    }

    public List<User> getPendingUsers() {
        return pendingUsers;
    }

    private Team(Parcel in) {
        id = in.readString();
        name = in.readString();
        zip = in.readString();
        city = in.readString();
        state = in.readString();
        in.readList(users, String.class.getClassLoader());

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
        dest.writeList(users);
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

    public static class Item {
        @ItemType final int itemType;
        @StringRes final int stringRes;
        @Nullable final ValueChangeCallBack changeCallBack;

        String value;

        Item(int itemType, int stringRes, String value, @Nullable ValueChangeCallBack changeCallBack) {
            this.itemType = itemType;
            this.stringRes = stringRes;
            this.value = value;
            this.changeCallBack = changeCallBack;
        }

        public void setValue(String value) {
            this.value = value;
            if (changeCallBack != null) changeCallBack.onValueChanged(value);
        }

        public int getItemType() {return this.itemType;}

        public int getStringRes() {return this.stringRes;}

        public String getValue() {return this.value;}
    }

    // Used to change the value of the Team's fields
    interface ValueChangeCallBack {
        void onValueChanged(String value);
    }
}
