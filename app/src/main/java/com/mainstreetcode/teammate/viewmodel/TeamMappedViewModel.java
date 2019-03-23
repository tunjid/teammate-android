package com.mainstreetcode.teammate.viewmodel;


import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamHost;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.mainstreetcode.teammate.viewmodel.events.Alert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

public abstract class TeamMappedViewModel<V extends Differentiable & TeamHost> extends MappedViewModel<Team, V> {

    final Map<Team, List<Differentiable>> modelListMap = new HashMap<>();

    @Override
    void onModelAlert(Alert alert) {
        super.onModelAlert(alert);
        //noinspection unchecked
        Alert.matches(alert, Alert.of(Alert.Deletion.class, Team.class, modelListMap::remove));
    }

    public List<Differentiable> getModelList(Team team) {
        return ModelUtils.get(team, modelListMap, ArrayList::new);
    }

    @Override
    void onErrorMessage(Message message, Team key, Differentiable invalid) {
        super.onErrorMessage(message, key, invalid);
        if (message.isIllegalTeamMember()) pushModelAlert(Alert.deletion(key));
    }

    Consumer<Throwable> onError(V model) {
        return throwable -> checkForInvalidObject(throwable, model, model.getTeam());
    }

    @Override
    void onInvalidKey(Team key) {
        super.onInvalidKey(key);
        pushModelAlert(Alert.deletion(key));
    }

    Flowable<Differentiable> getAllModels() {
        return Flowable.fromIterable(modelListMap.values()).flatMap(Flowable::fromIterable);
    }
}
