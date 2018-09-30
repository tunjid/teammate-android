package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.HeadToHeadRequest;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.persistence.entity.RoleEntity;
import com.mainstreetcode.teammate.repository.GameRepository;
import com.mainstreetcode.teammate.repository.GameRoundRepository;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.mainstreetcode.teammate.viewmodel.gofers.GameGofer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import static com.mainstreetcode.teammate.util.ModelUtils.findLast;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class GameViewModel extends TeamMappedViewModel<Game> {

    private final TeammateApi api = TeammateService.getApiInstance();

    private final Map<Tournament, Map<Integer, List<Identifiable>>> gameRoundMap = new HashMap<>();
    private final List<Identifiable> headToHeadMatchUps = new ArrayList<>();

    private final GameRoundRepository gameRoundRepository = GameRoundRepository.getInstance();
    private final GameRepository gameRepository = GameRepository.getInstance();

    public GameGofer gofer(Game game) {
        Consumer<Throwable> onError = throwable -> {};
        android.arch.core.util.Function<Game, Flowable<Team>> eligibleTeamSource = sourceStat -> getEligibleTeamsForGame(game);
        return new GameGofer(game, onError, this::getGame, this::updateGame, eligibleTeamSource);
    }

    @Override
    Flowable<List<Game>> fetch(Team key, boolean fetchLatest) {
        return gameRepository.modelsBefore(key, getQueryDate(key, fetchLatest))
                .doOnError(throwable -> checkForInvalidTeam(throwable, key));
    }

    @SuppressLint("UseSparseArrays")
    public List<Identifiable> getGamesForRound(Tournament tournament, int round) {
        Map<Integer, List<Identifiable>> roundMap = ModelUtils.get(tournament, gameRoundMap, HashMap::new);
        return ModelUtils.get(round, roundMap, ArrayList::new);
    }

    public List<Identifiable> getHeadToHeadMatchUps() {
        return headToHeadMatchUps;
    }

    public Flowable<DiffUtil.DiffResult> fetchGamesInRound(Tournament tournament, int round) {
        Flowable<List<Game>> flowable = gameRoundRepository.modelsBefore(tournament, round);
        Function<List<Game>, List<Identifiable>> listMapper = ArrayList<Identifiable>::new;
        return Identifiable.diff(flowable.map(listMapper), () -> getGamesForRound(tournament, round), this::preserveList);
    }


    public Single<DiffUtil.DiffResult> getMatchUps(HeadToHeadRequest request) {
        Single<List<Identifiable>> sourceSingle = api.matchUps(request).map(ArrayList<Identifiable>::new);
        return Identifiable.diff(sourceSingle, () -> headToHeadMatchUps, ModelUtils::replaceList).observeOn(mainThread());
    }

    public Flowable<Game> getGame(Game game) {
        return gameRoundRepository.get(game).observeOn(mainThread());
    }

    public Single<Game> endGame(Game game) {
        game.setEnded(true);
        return updateGame(game);
    }

    public Single<Game> updateGame(Game game) {
        return gameRoundRepository.createOrUpdate(game)
                .doOnError(throwable -> game.setEnded(false)).observeOn(mainThread());
    }

    static Flowable<Team> getEligibleTeamsForGame(Game game) {
        if (game.betweenUsers()) return Flowable.just(game.getTournament().getHost());
        return Flowable.fromIterable(RoleViewModel.roles)
                .filter(identifiable -> identifiable instanceof Role).cast(Role.class)
                .filter(Role::isPrivilegedRole).map(RoleEntity::getTeam)
                .filter(team -> isParticipant(game, team));
    }

    private Date getQueryDate(Team team, boolean fetchLatest) {
        if (fetchLatest) return null;

        Game game = findLast(getModelList(team), Game.class);
        return game == null ? null : game.getCreated();
    }

    private static boolean isParticipant(Game game, Team team) {
        return game.getHome().getEntity().equals(team) || game.getAway().getEntity().equals(team);
    }
}
