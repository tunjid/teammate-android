package com.mainstreetcode.teammates.viewmodel;


import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Message;
import com.mainstreetcode.teammates.model.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamMappedViewModel<V extends Identifiable> extends MappedViewModel<Team, V> {

    private final Map<Team, List<Identifiable>> modelListMap = new HashMap<>();

    public List<Identifiable> getModelList(Team team) {
        List<Identifiable> modelList = modelListMap.get(team);
        if (!modelListMap.containsKey(team)) modelListMap.put(team, modelList = new ArrayList<>());

        return modelList;
    }

    @Override
    void onErrorMessage(Message message, Team key, Identifiable invalid) {
        super.onErrorMessage(message, key, invalid);
        if (message.isIllegalTeamMember()) TeamViewModel.onTeamDeleted(key);
    }
}
