package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;
import android.arch.core.util.Function;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.HeadToHead;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.repository.GameRepository;
import com.mainstreetcode.teammate.repository.GameRoundRepository;
import com.mainstreetcode.teammate.repository.UserRepository;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.mainstreetcode.teammate.viewmodel.events.Alert;
import com.mainstreetcode.teammate.viewmodel.gofers.GameGofer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Single;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class GameViewModel extends TeamMappedViewModel<Game> {

    private final TeammateApi api = TeammateService.getApiInstance();

    private final Map<Tournament, Map<Integer, List<Identifiable>>> gameRoundMap = new HashMap<>();
    private final List<Identifiable> headToHeadMatchUps = new ArrayList<>();

    private final GameRoundRepository gameRoundRepository = GameRoundRepository.getInstance();
    private final GameRepository gameRepository = GameRepository.getInstance();

    public GameGofer gofer(Game game) {
        return new GameGofer(game, onError(game), this::getGame, this::updateGame, this::delete, GameViewModel::getEligibleTeamsForGame);
    }

    @Override
    Class<Game> valueClass() { return Game.class; }

    @Override
    @SuppressLint("CheckResult")
    void onModelAlert(Alert alert) {
        super.onModelAlert(alert);
        if (alert instanceof Alert.GameDeletion) onGameDeleted((Alert.GameDeletion) alert);
        else if (alert instanceof Alert.TournamentDeletion)
            onTournamentDeleted((Alert.TournamentDeletion) alert);
    }

    @Override
    void onErrorMessage(Message message, Team key, Identifiable invalid) {
        super.onErrorMessage(message, key, invalid);
        if (message.isInvalidObject()) headToHeadMatchUps.remove(invalid);
        if (invalid instanceof Game) ((Game) invalid).setEnded(false);
    }

    @Override
    Flowable<List<Game>> fetch(Team key, boolean fetchLatest) {
        return gameRepository.modelsBefore(key, getQueryDate(fetchLatest, key, Game::getCreated))
                .map(games -> filterDeclinedGamed(key, games));
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
        return Identifiable.diff(flowable.map(listMapper::apply), () -> getGamesForRound(tournament, round), this::preserveList);
    }

    public Single<HeadToHead.Summary> headToHead(HeadToHead.Request request) {
        return api.headToHead(request).map(result -> result.getSummary(request)).observeOn(mainThread());
    }

    public Single<DiffUtil.DiffResult> getMatchUps(HeadToHead.Request request) {
        Single<List<Identifiable>> sourceSingle = api.matchUps(request).map(games -> {
            Collections.sort(games, Identifiable.COMPARATOR);
            return new ArrayList<>(games);
        });
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
                .doOnError(onError(game)).observeOn(mainThread());
    }

    private Single<Game> delete(final Game game) {
        return gameRepository.delete(game)
                .doOnSuccess(getModelList(game.getTeam())::remove)
                .doOnSuccess(deleted -> pushModelAlert(Alert.gameDeletion(deleted)));
    }

    static Flowable<Team> getEligibleTeamsForGame(Game game) {
        if (game.betweenUsers()) return Flowable.just(game.getHost());
        return Flowable.fromIterable(RoleViewModel.roles)
                .filter(identifiable -> identifiable instanceof Role).cast(Role.class)
                .filter(Role::isPrivilegedRole).map(Role::getTeam)
                .filter(team -> isParticipant(game, team));
    }

    private static boolean isParticipant(Game game, Team team) {
        return game.getHome().getEntity().equals(team) || game.getAway().getEntity().equals(team);
    }

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void onTournamentDeleted(Alert.TournamentDeletion alert) {
        Tournament tournament = alert.getModel();

        Map<?, List<Identifiable>> tournamentMap = ModelUtils.get(tournament, gameRoundMap, Collections::emptyMap);
        Function<Map<?, List<Identifiable>>, Flowable<Identifiable>> mapListFunction = listMap ->
                Flowable.fromIterable(listMap.values())
                        .flatMap(Flowable::fromIterable);

        Flowable<Identifiable> teamMapped = mapListFunction.apply(modelListMap);
        Flowable<Identifiable> tournamentMapped = mapListFunction.apply(tournamentMap);
        Flowable.concat(teamMapped, tournamentMapped)
                .distinct()
                .filter(item -> item instanceof Game)
                .cast(Game.class)
                .filter(game -> tournament.equals(game.getTournament()))
                .subscribe(game -> pushModelAlert(Alert.gameDeletion(game)), ErrorHandler.EMPTY);
    }

    private void onGameDeleted(Alert.GameDeletion alert) {
        Game game = alert.getModel();

        headToHeadMatchUps.remove(game);
        getModelList(game.getHost()).remove(game);

        Competitive home = game.getHome().getEntity();
        Competitive away = game.getAway().getEntity();
        if (home instanceof Team) getModelList((Team) home).remove(game);
        if (away instanceof Team) getModelList((Team) away).remove(game);

        gameRepository.queueForLocalDeletion(game);
    }

    @NonNull
    private List<Game> filterDeclinedGamed(Team key, List<Game> games) {
        Iterator<Game> iterator = games.iterator();
        while (iterator.hasNext()) {
            Game game = iterator.next();
            Competitive entity = game.betweenUsers() ? UserRepository.getInstance().getCurrentUser() : key;
            boolean isAway = entity.equals(game.getAway().getEntity());
            if (isAway && game.getAway().isDeclined()) iterator.remove();
        }

        return games;
    }
}
