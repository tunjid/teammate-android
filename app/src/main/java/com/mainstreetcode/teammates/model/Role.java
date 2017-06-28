package com.mainstreetcode.teammates.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;

/**
 * Roles on a team
 * <p>
 * Created by Shemanigans on 6/6/17.
 */
@Entity(
        tableName = "roles",
        primaryKeys = {"userId", "teamId"},
        foreignKeys = {
                @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "userId"),
                @ForeignKey(entity = Team.class, parentColumns = "id", childColumns = "teamId")
        }
)
public class Role {

    private String name;
    private String userId;
    private String teamId;

    public Role(String name, String userId, String teamId) {
        this.name = name;
        this.teamId = teamId;
        this.userId = userId;
    }

    public Role(String name, User user, Team team) {
        this.name = name;
        this.teamId = team.getId();
        this.userId = user.getId();
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public String getTeamId() {
        return teamId;
    }
}
