package com.mainstreetcode.teammates.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.util.ListableBean;

import java.lang.annotation.Retention;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Teams
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

@Getter
@Setter
public class Team implements Parcelable, ListableBean<Team, Team.Item> {

    private static final int NAME_POSITION = 2;
    private static final int CITY_POSITION = 3;
    private static final int STATE_POSITION = 4;
    private static final int ZIP_POSITION = 5;
    public static final int ROLE_POSITION = 5;

    private static final String NAME_KEY = "name";
    private static final String CITY_KEY = "city";
    private static final String STATE_KEY = "state";
    private static final String ZIP_KEY = "zip";

    @Retention(SOURCE)
    @IntDef({HEADING, INPUT, IMAGE, ROLE})
    @interface ItemType {}

    public static final int HEADING = 1;
    public static final int INPUT = 2;
    public static final int IMAGE = 3;
    public static final int ROLE = 4;

    public static final String DB_NAME = "teams";
    public static final String SEARCH_INDEX_KEY = "nameLowercased";

    String uid;
    String name;
    String city;
    String state;
    String zip;

    List<String> memberIds;
    final List<Item> items;

    public Team() {
        items = itemsFromTeam(null);
    }

    public Team(String key, DataSnapshot snapshot) {
        this.uid = key;
        Map<String, Object> data = snapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {
        });

        this.name = (String) data.get(NAME_KEY);
        this.city = (String) data.get(CITY_KEY);
        this.state = (String) data.get(STATE_KEY);
        this.zip = (String) data.get(ZIP_KEY);

        items = itemsFromTeam(this);
    }

    private Team(Team source) {
        this.uid = source.uid;
        this.name = source.get(NAME_POSITION).value;
        this.city = source.get(CITY_POSITION).value;
        this.state = source.get(STATE_POSITION).value;
        this.zip = source.get(ZIP_POSITION).value;

        items = itemsFromTeam(source);
    }

    private static List<Item> itemsFromTeam(@Nullable Team team) {
        return Arrays.asList(
                new Item(IMAGE, R.string.team_logo, ""),
                new Item(HEADING, R.string.team_info, ""),
                new Item(INPUT, R.string.team_name, team == null ? "" : team.name),
                new Item(INPUT, R.string.city, team == null ? "" : team.city),
                new Item(INPUT, R.string.state, team == null ? "" : team.state),
                new Item(INPUT, R.string.zip, team == null ? "" : team.zip),
                new Item(HEADING, R.string.team_role, ""),
                new Item(ROLE, R.string.team_role, "")
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team)) return false;

        Team team = (Team) o;

        //return uid.equals(team.uid);
        return uid.equals(team.name);
    }

    @Override
    public int hashCode() {
        //return uid.hashCode();
        return name.hashCode();
    }

    private Team(Parcel in) {
        uid = in.readString();
        name = in.readString();
        zip = in.readString();
        city = in.readString();
        state = in.readString();

        items = itemsFromTeam(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(name);
        dest.writeString(zip);
        dest.writeString(city);
        dest.writeString(state);
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

    @Getter
    public static class Item {
        @ItemType
        int itemType;
        @StringRes
        int stringRes;
        String value;

        Item(int itemType, int stringRes, String value) {
            this.itemType = itemType;
            this.stringRes = stringRes;
            this.value = value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
