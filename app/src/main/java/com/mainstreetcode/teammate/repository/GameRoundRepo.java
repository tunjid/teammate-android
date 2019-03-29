package com.mainstreetcode.teammate.repository;


import androidx.annotation.Nullable;

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

public class GameRoundRepo extends QueryRepo<Game, Tournament, Integer> {

    private final TeammateApi api;
    private final GameDao gameDao;

    GameRoundRepo() {
        api = TeammateService.getApiInstance();
        gameDao = AppDatabase.getInstance().gameDao();
    }

    @Override
    public EntityDao<? super Game> dao() {
        return gameDao;
    }

    @Override
    public Single<Game> createOrUpdate(Game game) {
        return RepoProvider.forRepo(GameRepo.class).createOrUpdate(game);
    }

    @Override
    public Flowable<Game> get(String id) {
        return RepoProvider.forRepo(GameRepo.class).get(id);
    }

    @Override
    public Single<Game> delete(Game game) {
        return RepoProvider.forRepo(GameRepo.class).delete(game);
    }

    @Override
    Maybe<List<Game>> localModelsBefore(Tournament tournament, @Nullable Integer round) {
        if (round == null) round = 0;
        return gameDao.getGames(tournament.getId(), round, 30).subscribeOn(io());
    }

    @Override
    Maybe<List<Game>> remoteModelsBefore(Tournament tournament, @Nullable Integer round) {
        return api.getGamesForRound(tournament.getId(), round == null ? 0 : round, 30).map(getSaveManyFunction()).toMaybe();
    }

    @Override
    Function<List<Game>, List<Game>> provideSaveManyFunction() {
        return list -> RepoProvider.forRepo(GameRepo.class).provideSaveManyFunction().apply(list);
    }
}
