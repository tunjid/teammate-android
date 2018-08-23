package com.mainstreetcode.teammate.viewmodel;

import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.persistence.entity.RoleEntity;
import com.mainstreetcode.teammate.repository.StatRepository;
import com.mainstreetcode.teammate.viewmodel.gofers.StatGofer;

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

/**
 * ViewModel for {@link Stat tournaments}
 */

public class StatViewModel extends MappedViewModel<Game, Stat> {

    private final StatRepository repository;
    private final Map<Game, List<Identifiable>> modelListMap = new HashMap<>();

    public StatViewModel() {
        repository = StatRepository.getInstance();
    }

    public StatGofer gofer(Stat stat) {
        Consumer<Throwable> onError = throwable -> checkForInvalidObject(throwable, stat, stat.getGame());
        Function<Stat, Flowable<Team>> eligibleTeamSource = sourceStat -> getEligibleTeamsForGame(stat.getGame());
        return new StatGofer(stat, onError, this::createOrUpdateStat, this::delete, eligibleTeamSource);
    }

    @Override
    Flowable<List<Stat>> fetch(Game key, boolean fetchLatest) {
        return repository.modelsBefore(key, getQueryDate(key, fetchLatest));
    }

    public List<Identifiable> getModelList(Game game) {
        List<Identifiable> modelList = modelListMap.get(game);
        if (!modelListMap.containsKey(game)) modelListMap.put(game, modelList = new ArrayList<>());

        return modelList;
    }

    private Flowable<Stat> getStat(Stat stat) {
        return stat.isEmpty() ? Flowable.empty() : repository.get(stat);
    }

    private Single<Stat> createOrUpdateStat(final Stat tournament) {
        return repository.createOrUpdate(tournament);
    }

    public Single<Stat> delete(final Stat stat) {
        return repository.delete(stat).doOnSuccess(deleted -> {
            getModelList(stat.getGame()).remove(deleted);
        });
    }

    public Single<Boolean> canEditGameStats(Game game) {
        return getEligibleTeamsForGame(game).count().map(value -> value > 0);
    }

    private Flowable<Team> getEligibleTeamsForGame(Game game) {
        return Flowable.fromIterable(RoleViewModel.roles)
                .filter(identifiable -> identifiable instanceof Role).cast(Role.class)
                .filter(Role::isPrivilegedRole).map(RoleEntity::getTeam)
                .filter(team -> isParticipant(game, team));
    }

    private boolean isParticipant(Game game, Team team) {
        return game.getHome().getEntity().equals(team) || game.getAway().getEntity().equals(team);
    }

    private Date getQueryDate(Game game, boolean fetchLatest) {
        if (fetchLatest) return null;

        Stat stat = findLast(getModelList(game), Stat.class);
        return stat == null ? null : stat.getCreated();
    }
}
