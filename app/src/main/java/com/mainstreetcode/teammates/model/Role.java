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

    public static final String PHOTO_UPLOAD_KEY = "role-photo";
    private static final String ADMIN = "Admin";

    private String name;
    private String userId;
    private String teamId;
    private String imageUrl;

    @SuppressWarnings("unused")
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

    public String getImageUrl() {
        return imageUrl;
    }

    public static boolean isTeamAdmin(Team team, User user) {
        int index = team.getUsers().indexOf(user);
        if(index == -1) return false;
        User fromTeam = team.getUsers().get(index);
        return index != -1 && ADMIN.equals(team.getUsers().get(index).getRole());
    }
}
