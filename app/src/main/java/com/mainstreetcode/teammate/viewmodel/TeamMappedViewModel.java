package com.mainstreetcode.teammate.viewmodel;


import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamHost;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.mainstreetcode.teammate.viewmodel.events.Alert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.Consumer;

import static com.mainstreetcode.teammate.model.Message.fromThrowable;

public abstract class TeamMappedViewModel<V extends Identifiable & TeamHost> extends MappedViewModel<Team, V> {

    final Map<Team, List<Identifiable>> modelListMap = new HashMap<>();

    @Override
    void onModelAlert(Alert alert) {
        super.onModelAlert(alert);
        if (!(alert instanceof Alert.TeamDeletion)) return;

        Team deleted = ((Alert.TeamDeletion) alert).getModel();
        modelListMap.remove(deleted);
    }

    public List<Identifiable> getModelList(Team team) {
        return ModelUtils.get(team, modelListMap, ArrayList::new);
    }

    @Override
    void onErrorMessage(Message message, Team key, Identifiable invalid) {
        super.onErrorMessage(message, key, invalid);
        if (message.isIllegalTeamMember()) pushModelAlert(Alert.teamDeletion(key));
    }

    boolean checkForInvalidTeam(Throwable throwable, Team team) {
        Message message = fromThrowable(throwable);
        boolean isValidModel = message == null || message.isValidModel();

        if (isValidModel) return false;

        pushModelAlert(Alert.teamDeletion(team));
        return true;
    }

    Consumer<Throwable> onError(V model) {
        return throwable -> checkForInvalidObject(throwable, model, model.getTeam());
    }
}
