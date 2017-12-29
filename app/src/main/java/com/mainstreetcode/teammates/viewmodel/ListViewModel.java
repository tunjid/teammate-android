package com.mainstreetcode.teammates.viewmodel;


import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListViewModel<T extends Model<T>> extends ViewModel {

    private final Map<Team, List<T>> teamModelListMap = new HashMap<>();

    public List<T> getModelList(Team team) {
        List<T> model = teamModelListMap.get(team);
        if (!teamModelListMap.containsKey(team))
            teamModelListMap.put(team, model = new ArrayList<>());

        return model;
    }
}
