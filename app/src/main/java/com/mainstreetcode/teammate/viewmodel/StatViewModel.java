package com.mainstreetcode.teammate.viewmodel;

import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.repository.StatRepository;
import com.mainstreetcode.teammate.viewmodel.gofers.StatGofer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Single;

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
        return new StatGofer(stat, throwable -> checkForInvalidObject(throwable, stat, stat.getGame()), this::createOrUpdateStat, this::delete);
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

    private Flowable<Stat> getStat(Stat tournament) {
        return tournament.isEmpty() ? Flowable.empty() : repository.get(tournament);
    }

    private Single<Stat> createOrUpdateStat(final Stat tournament) {
        return repository.createOrUpdate(tournament);
    }

    public Single<Stat> delete(final Stat stat) {
        return repository.delete(stat).doOnSuccess(deleted -> {
            getModelList(stat.getGame()).remove(deleted);
        });
    }

    private Date getQueryDate(Game game, boolean fetchLatest) {
        if (fetchLatest) return null;

        Stat stat = findLast(getModelList(game), Stat.class);
        return stat == null ? null : stat.getCreated();
    }
}
