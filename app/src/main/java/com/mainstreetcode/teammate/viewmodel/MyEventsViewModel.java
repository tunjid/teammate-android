package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;

import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamHost;
import com.mainstreetcode.teammate.repository.EventRepository;
import com.mainstreetcode.teammate.viewmodel.events.Alert;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Flowable;

import static com.mainstreetcode.teammate.util.ModelUtils.findLast;

/**
 * ViewModel for {@link Event events}
 */

public class MyEventsViewModel extends MappedViewModel<Class<Event>, Event> {

    private final List<Identifiable> attending = new ArrayList<>();
    private final EventRepository repository;

    public MyEventsViewModel() {
        repository = EventRepository.getInstance();
    }

    @Override
    public List<Identifiable> getModelList(Class<Event> key) {
        return attending;
    }

    @Override
    @SuppressLint("CheckResult")
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
        return repository.attending(getQueryDate(fetchLatest));
    }

    private Date getQueryDate(boolean fetchLatest) {
        if (fetchLatest) return null;

        Event event = findLast(getModelList(Event.class), Event.class);
        return event == null ? null : event.getStartDate();
    }

    private void removeDeletedTeamEvents(Team matcher) {
        if (matcher == null) return;

        Iterator<Identifiable> iterator = getModelList(Event.class).iterator();

        while (iterator.hasNext()) {
            Identifiable identifiable = iterator.next();
            if (!(identifiable instanceof Event)) continue;
            if (((Event) identifiable).getTeam().equals(matcher)) iterator.remove();
        }
    }
}
