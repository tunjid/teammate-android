package com.mainstreetcode.teammate.viewmodel.events;

import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;

public abstract class Alert<T extends Model<T>> {

    private T model;

    private Alert(T model) { this.model = model; }

    public T getModel() { return model; }

    public static Alert<Team> teamDeletion(Team team) {return new TeamDeletion(team);}

    public static Alert<Game> gameDeletion(Game game) {return new GameDeletion(game);}

    public static Alert<Event> eventAbsentee(Event event) {return new EventAbsentee(event);}

    public static Alert<BlockedUser> userBlocked(BlockedUser blockedUser) {return new UserBlocked(blockedUser);}

    public static Alert<Tournament> tournamentDeletion(Tournament tournament) {return new TournamentDeletion(tournament);}

    public static Alert<JoinRequest> requestProcessed(JoinRequest joinRequest) {return new JoinRequestProcessed(joinRequest);}

    public static class TeamDeletion extends Alert<Team> {
        private TeamDeletion(Team model) { super(model); }
    }

    public static class GameDeletion extends Alert<Game> {
        private GameDeletion(Game model) { super(model); }
    }

    public static class EventAbsentee extends Alert<Event> {
        private EventAbsentee(Event model) { super(model); }
    }

    public static class UserBlocked extends Alert<BlockedUser> {
        private UserBlocked(BlockedUser model) { super(model); }
    }

    public static class TournamentDeletion extends Alert<Tournament> {
        private TournamentDeletion(Tournament model) { super(model); }
    }

    public static class JoinRequestProcessed extends Alert<JoinRequest> {
        private JoinRequestProcessed(JoinRequest model) { super(model); }
    }
}
