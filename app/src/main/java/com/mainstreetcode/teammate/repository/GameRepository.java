package com.mainstreetcode.teammate.repository;


import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.GameDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static io.reactivex.schedulers.Schedulers.io;

public class GameRepository extends TeamQueryRepository<Game> {

    private static GameRepository ourInstance;

    private final TeammateApi api;
    private final GameDao gameDao;

    private GameRepository() {
        api = TeammateService.getApiInstance();
        gameDao = AppDatabase.getInstance().gameDao();
    }

    public static GameRepository getInstance() {
        if (ourInstance == null) ourInstance = new GameRepository();
        return ourInstance;
    }

    @Override
    public EntityDao<? super Game> dao() {
        return gameDao;
    }

    @Override
    public Single<Game> createOrUpdate(Game game) {
        return game.isEmpty()
                ? Single.error(new TeammateException(""))
                : api.updateGame(game.getId(), game)
                .doOnError(throwable -> deleteInvalidModel(game, throwable))
                .map(getLocalUpdateFunction(game))
                .map(getSaveFunction());
    }

    @Override
    public Flowable<Game> get(String id) {
        Maybe<Game> local = gameDao.get(id).subscribeOn(io());
        Maybe<Game> remote = api.getGame(id).map(getSaveFunction()).toMaybe();

        return fetchThenGetModel(local, remote);
    }

    @Override
    public Single<Game> delete(Game game) {
        return api.deleteGame(game.getId());
    }

    @Override
    Maybe<List<Game>> localModelsBefore(Team team, @Nullable Date date) {
        if (date == null) date = getFutureDate();
        return gameDao.getGames(team.getId(), date).subscribeOn(io());
    }

    @Override
    Maybe<List<Game>> remoteModelsBefore(Team team, @Nullable Date date) {
        return api.getGames(team.getId(), date).map(getSaveManyFunction()).toMaybe();
    }

    @Override
    Function<List<Game>, List<Game>> provideSaveManyFunction() {
        return models -> {
            List<Team> teams = new ArrayList<>(models.size());
            List<User> users = new ArrayList<>(models.size());
            List<Tournament> tournaments = new ArrayList<>(models.size());
            List<Competitor> competitors = new ArrayList<>(models.size());

            for (Game game : models) {
                User referee = game.getReferee();
                Competitor home = game.getHome();
                Competitor away = game.getAway();
                Tournament tournament = game.getTournament();

                if (!referee.isEmpty()) users.add(referee);
                if (!tournament.isEmpty()) {
                    tournament.updateHost(game.getTeam());
                    tournaments.add(tournament);
                }

                addIfValid(home, users, teams);
                addIfValid(away, users, teams);
                competitors.add(home);
                competitors.add(away);
            }

            UserRepository.getInstance().saveAsNested().apply(users);
            TeamRepository.getInstance().saveAsNested().apply(teams);
            TournamentRepository.getInstance().saveAsNested().apply(tournaments);
            CompetitorRepository.getInstance().saveAsNested().apply(competitors);

            gameDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }

    private void addIfValid(Competitor competitor, List<User> users, List<Team> teams) {
        Competitive entity = competitor.getEntity();
        if (entity instanceof User) users.add((User) entity);
        if (entity instanceof Team) teams.add((Team) entity);
    }
}
