package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.repository.GameRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class GameViewModel extends BaseViewModel {

    private final Map<Tournament, Map<Integer, List<Identifiable>>> gameRoundMap = new HashMap<>();
    private final GameRepository gameRepository = GameRepository.getInstance();

    @SuppressLint("UseSparseArrays")
    public List<Identifiable> getGamesForRound(Tournament tournament, int round) {

        Map<Integer, List<Identifiable>> roundMap = gameRoundMap.get(tournament);
        if (!gameRoundMap.containsKey(tournament))
            gameRoundMap.put(tournament, roundMap = new HashMap<>());

        List<Identifiable> games = roundMap.get(round);
        if (!roundMap.containsKey(round)) roundMap.put(round, games = new ArrayList<>());

        return games;
    }

    public Flowable<DiffUtil.DiffResult> fetchGamesInRound(Tournament tournament, int round) {
        Flowable<List<Game>> flowable = gameRepository.modelsBefore(tournament, round);
        Function<List<Game>, List<Identifiable>> listMapper = items -> new ArrayList<>(items);
        return Identifiable.diff(flowable.map(listMapper), () -> getGamesForRound(tournament, round), this::preserveList);

    }

    public Completable getGame(Game game) {
        return gameRepository.get(game).ignoreElements().observeOn(mainThread());
    }
}
