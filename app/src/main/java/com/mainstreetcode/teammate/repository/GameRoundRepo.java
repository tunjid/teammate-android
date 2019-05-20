/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
