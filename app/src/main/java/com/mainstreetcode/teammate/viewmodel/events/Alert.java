package com.mainstreetcode.teammate.viewmodel.events;

import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;

public abstract class Alert<T extends Model<T>> {

    private T model;

    private Alert(T model) { this.model = model; }

    public T getModel() { return model; }

    public static Alert<Team> teamDeletion(Team team) {return new TeamDeletion(team);}

    public static Alert<User> userBlocked(User user) {return new UserBlocked(user);}

    public static class TeamDeletion extends Alert<Team> {
        private TeamDeletion(Team model) { super(model); }
    }

    public static class UserBlocked extends Alert<User> {
        private UserBlocked(User model) { super(model); }
    }
}
