package com.mainstreetcode.teammates.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Teams
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

@Getter
@Setter
public class Team {

    public static final String DB_NAME = "teams";
    public static final String SEARCH_INDEX_KEY = "nameLowercased";

    String uid;
    String name;
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
}
