package com.mainstreetcode.teammates.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Teams
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

@Getter
@Setter
@NoArgsConstructor
public class Team implements Parcelable {

    public static final String DB_NAME = "teams";
    public static final String SEARCH_INDEX_KEY = "nameLowercased";

    String uid;
    String name;
    String zip;
    String city;
    String state;

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
}
