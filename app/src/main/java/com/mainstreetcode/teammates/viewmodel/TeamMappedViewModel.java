package com.mainstreetcode.teammates.viewmodel;


import com.mainstreetcode.teammates.model.Message;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamMappedViewModel<V extends Model> extends MappedViewModel<Team, V> {

    private final Map<Team, List<V>> modelListMap = new HashMap<>();

    public List<V> getModelList(Team team) {
        List<V> modelList = modelListMap.get(team);
        if (!modelListMap.containsKey(team)) modelListMap.put(team, modelList = new ArrayList<>());

        return modelList;
    }

    @Override
    void onErrorMessage(Message message, Team key, V invalid) {
        super.onErrorMessage(message, key, invalid);
        if (message.isIllegalTeamMember()) TeamViewModel.teams.remove(key);
    }
}
