package com.mainstreetcode.teammate.viewmodel;

import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.repository.EventRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.viewmodel.events.Alert;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Flowable;

/**
 * ViewModel for {@link Event events}
 */

public class MyEventsViewModel extends MappedViewModel<Class<Event>, Event> {

    private final List<Differentiable> attending = new ArrayList<>();
    private final EventRepo repository;

    public MyEventsViewModel() {
        repository = RepoProvider.forRepo(EventRepo.class);
    }

    @Override
    Class<Event> valueClass() { return Event.class; }

    @Override
    public List<Differentiable> getModelList(Class<Event> key) { return attending; }

    @Override
    void onModelAlert(Alert alert) {
        super.onModelAlert(alert);

        //noinspection unchecked
        Alert.matches(alert,
                Alert.of(Alert.Deletion.class, Team.class, this::removeDeletedTeamEvents),
                Alert.of(Alert.EventAbsentee.class, Event.class, getModelList(Event.class)::remove),
                Alert.of(Alert.Creation.class, BlockedUser.class, blockedUser -> removeDeletedTeamEvents(blockedUser.getTeam()))
        );
    }

    @Override
    Flowable<List<Event>> fetch(Class<Event> key, boolean fetchLatest) {
        return repository.attending(getQueryDate(fetchLatest, key, Event::getStartDate));
    }

    private void removeDeletedTeamEvents(Team matcher) {
        if (matcher == null) return;

        Iterator<Differentiable> iterator = getModelList(Event.class).iterator();

        while (iterator.hasNext()) {
            Differentiable identifiable = iterator.next();
            if (!(identifiable instanceof Event)) continue;
            if (((Event) identifiable).getTeam().equals(matcher)) iterator.remove();
        }
    }
}
