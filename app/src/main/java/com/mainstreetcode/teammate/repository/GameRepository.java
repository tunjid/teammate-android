package com.mainstreetcode.teammate.repository;


import android.support.annotation.Nullable;

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
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static io.reactivex.schedulers.Schedulers.io;

public class GameRepository extends QueryRepository<Game, Tournament, Integer> {

    private static GameRepository ourInstance;

    private final TeammateApi api;
    private final GameDao gameDao;
    private final ModelRepository<User> userRepository;
    private final ModelRepository<Team> teamRepository;

    private GameRepository() {
        api = TeammateService.getApiInstance();
        gameDao = AppDatabase.getInstance().gameDao();
        teamRepository = TeamRepository.getInstance();
        userRepository = UserRepository.getInstance();
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
        return Single.error(new TeammateException(""));
    }

    @Override
    public Flowable<Game> get(String id) {
        Maybe<Game> local = gameDao.get(id).subscribeOn(io());
        Maybe<Game> remote = api.getGame(id).map(getSaveFunction()).toMaybe();

        return fetchThenGetModel(local, remote);
    }

    @Override
    public Single<Game> delete(Game game) {
        return Single.error(new TeammateException(""));
    }

    @Override
    Maybe<List<Game>> localModelsBefore(Tournament tournament, @Nullable Integer round) {
        if (round == null) round = 0;
        return gameDao.getGames(tournament.getId(), round).subscribeOn(io());
    }

    @Override
    Maybe<List<Game>> remoteModelsBefore(Tournament tournament, @Nullable Integer round) {
        return api.getGames(tournament.getId(), round == null ? 0 : round).map(getSaveManyFunction()).toMaybe();
    }

    @Override
    Function<List<Game>, List<Game>> provideSaveManyFunction() {
        return models -> {
            List<Team> teams = new ArrayList<>(models.size());
            List<User> users = new ArrayList<>(models.size());

            for (Game game : models) {
                addIfValid(game.getHome(), users, teams);
                addIfValid(game.getAway(), users, teams);
                addIfValid(game.getWinner(), users, teams);
            }

            teamRepository.saveAsNested().apply(teams);
            userRepository.saveAsNested().apply(users);

            gameDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }

    private void addIfValid(Competitor competitor, List<User> users, List<Team> teams) {
        if (competitor instanceof User) users.add((User) competitor);
        if (competitor instanceof Team) teams.add((Team) competitor);
    }
}
