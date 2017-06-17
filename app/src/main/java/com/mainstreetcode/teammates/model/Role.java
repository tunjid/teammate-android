package com.mainstreetcode.teammates.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import lombok.Getter;
import lombok.Setter;

/**
 * Roles on a team
 * <p>
 * Created by Shemanigans on 6/6/17.
 */
@Entity(tableName = "roles",
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user"))
@Getter
@Setter
public class Role {

    public static final String DB_NAME = "roles";
    public static final String SEARCH_INDEX_KEY = "name";

    boolean isEditor;

    @PrimaryKey
    String id;
    String name;
    String team;

    public Role(String id, String name, String team, String user) {
        this.id = id;
        this.name = name;
        this.team = team;
        this.user = user;
    }

    String user;
}
