package com.mainstreetcode.teammate.viewmodel;

import com.mainstreetcode.teammate.model.Event;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamHost;
import com.mainstreetcode.teammate.repository.EventRepository;
import com.mainstreetcode.teammate.viewmodel.events.Alert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Flowable;

/**
 * ViewModel for {@link Event events}
 */

public class MyEventsViewModel extends MappedViewModel<Class<Event>, Event> {

    private final List<Differentiable> attending = new ArrayList<>();
    private final EventRepository repository;

    public MyEventsViewModel() {
        repository = EventRepository.getInstance();
    }

    @Override
    Class<Event> valueClass() { return Event.class; }

    @Override
    public List<Differentiable> getModelList(Class<Event> key) { return attending; }

    @Override
    void onModelAlert(Alert alert) {
        super.onModelAlert(alert);

        if (alert instanceof Alert.EventAbsentee) {
            Event event = ((Alert.EventAbsentee) alert).getModel();
            getModelList(Event.class).remove(event);
        }
        else if (alert instanceof Alert.UserBlocked || alert instanceof Alert.TeamDeletion) {
            TeamHost host = (TeamHost) alert.getModel();
            removeDeletedTeamEvents(host.getTeam());
        }
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
