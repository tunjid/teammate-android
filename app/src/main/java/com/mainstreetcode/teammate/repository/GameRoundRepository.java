package com.mainstreetcode.teammate.repository;


import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.GameDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static io.reactivex.schedulers.Schedulers.io;

public class GameRoundRepository extends QueryRepository<Game, Tournament, Integer> {

    private static GameRoundRepository ourInstance;

    private final TeammateApi api;
    private final GameDao gameDao;

    private GameRoundRepository() {
        api = TeammateService.getApiInstance();
        gameDao = AppDatabase.getInstance().gameDao();
    }

    public static GameRoundRepository getInstance() {
        if (ourInstance == null) ourInstance = new GameRoundRepository();
        return ourInstance;
    }

    @Override
    public EntityDao<? super Game> dao() {
        return gameDao;
    }

    @Override
    public Single<Game> createOrUpdate(Game game) {
        return GameRepository.getInstance().createOrUpdate(game);
    }

    @Override
    public Flowable<Game> get(String id) {
        return GameRepository.getInstance().get(id);
    }

    @Override
    public Single<Game> delete(Game game) {
        return GameRepository.getInstance().delete(game);
    }

    @Override
    Maybe<List<Game>> localModelsBefore(Tournament tournament, @Nullable Integer round) {
        if (round == null) round = 0;
        return gameDao.getGames(tournament.getId(), round).subscribeOn(io());
    }

    @Override
    Maybe<List<Game>> remoteModelsBefore(Tournament tournament, @Nullable Integer round) {
        return api.getGamesForRound(tournament.getId(), round == null ? 0 : round).map(getSaveManyFunction()).toMaybe();
    }

    @Override
    Function<List<Game>, List<Game>> provideSaveManyFunction() {
        return GameRepository.getInstance().provideSaveManyFunction();
    }
}
