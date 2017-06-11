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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
public class Team implements
        Parcelable,
        ListableBean<Team, Team.Item> {

    private static final int NAME_POSITION = 2;
    private static final int CITY_POSITION = 3;
    private static final int STATE_POSITION = 4;
    public static final int ZIP_POSITION = 5;
    public static final int ROLE_POSITION = 5;

    private static final String UID_KEY = "uid";
    private static final String NAME_KEY = "name";
    private static final String CITY_KEY = "city";
    private static final String STATE_KEY = "state";
    private static final String ZIP_KEY = "zip";
    private static final String MEMBER_IDS_KEY = "memberIds";
    public static final String NAME_LOWERCASED_KEY = "nameLowercased";

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

    final List<Item> items;
    List<String> memberIds = new ArrayList<>();

    public static Team empty() {
        return new Team();
    }

    public static Team fromSnapshot(String key, DataSnapshot snapshot) {
        return new Team(key, snapshot);
    }


    private Team() {
        items = itemsFromTeam(this);
    }

    private Team(String key, DataSnapshot snapshot) {
        this.uid = key;
        Map<String, Object> data = snapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {
        });

        this.name = (String) data.get(NAME_KEY);
        this.city = (String) data.get(CITY_KEY);
        this.state = (String) data.get(STATE_KEY);
        this.zip = (String) data.get(ZIP_KEY);

        @SuppressWarnings("unchecked")
        List<String> memberIdList = (List<String>) data.get(MEMBER_IDS_KEY);
        if (memberIdList != null) memberIds.addAll(memberIdList);

        items = itemsFromTeam(this);
    }

    private Team(Team source) {
        this.uid = source.uid;
        this.name = source.get(NAME_POSITION).value;
        this.city = source.get(CITY_POSITION).value;
        this.state = source.get(STATE_POSITION).value;
        this.zip = source.get(ZIP_POSITION).value;

        this.items = itemsFromTeam(source);
        this.memberIds.addAll(source.memberIds);
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

    public Map toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put(UID_KEY, uid);
        result.put(NAME_KEY, name);
        result.put(NAME_LOWERCASED_KEY, name.toLowerCase());
        result.put(CITY_KEY, city);
        result.put(STATE_KEY, state);
        result.put(ZIP_KEY, zip);
        result.put(MEMBER_IDS_KEY, memberIds);

        return result;
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
        in.readList(memberIds, String.class.getClassLoader());

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
        dest.writeList(memberIds);
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
    }

    // Used to change the value of the Team's fields
    interface ValueChangeCallBack {
        void onValueChanged(String value);
    }
}
