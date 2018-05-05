package com.mainstreetcode.teammate.viewmodel.events;

import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Team;

public abstract class Alert<T extends Model<T>> {

    private T model;

    private Alert(T model) { this.model = model; }

    public T getModel() { return model; }

    public static Alert<Team> teamDeletion(Team team) {return new TeamDeletion(team);}

    public static Alert<BlockedUser> userBlocked(BlockedUser blockedUser) {return new UserBlocked(blockedUser);}

    public static Alert<JoinRequest> requestProcessed(JoinRequest joinRequest) {return new JoinRequestProcessed(joinRequest);}

    public static class TeamDeletion extends Alert<Team> {
        private TeamDeletion(Team model) { super(model); }
    }

    public static class UserBlocked extends Alert<BlockedUser> {
        private UserBlocked(BlockedUser model) { super(model); }
    }

    public static class JoinRequestProcessed extends Alert<JoinRequest> {
        private JoinRequestProcessed(JoinRequest model) { super(model); }
    }
}
