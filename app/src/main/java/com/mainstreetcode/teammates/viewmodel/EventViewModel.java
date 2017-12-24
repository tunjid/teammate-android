package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.repository.EventRepository;
import com.mainstreetcode.teammates.repository.UserRepository;
import com.mainstreetcode.teammates.util.ModelDiffCallback;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static android.support.v7.util.DiffUtil.calculateDiff;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.computation;

/**
 * ViewModel for {@link Event events}
 */

public class EventViewModel extends ViewModel {

    private final User signedInUser;
    private final EventRepository repository;

    public EventViewModel() {
        signedInUser = UserRepository.getInstance().getCurrentUser();
        repository = EventRepository.getInstance();
    }

    public Flowable<DiffUtil.DiffResult> getEvents(List<Event> source, String teamId) {
        final List<Event> updated = new ArrayList<>(source);

        return repository.getEvents(teamId).concatMapDelayError(fetchedEvents -> Flowable.fromCallable(() -> {
            List<Event> stale = new ArrayList<>(source);
            ModelUtils.preserveList(updated, fetchedEvents);

            return calculateDiff(new ModelDiffCallback(updated, stale));
        })
                .subscribeOn(computation())
                .observeOn(mainThread()))
                .doOnNext(diffResult -> {
                    source.clear();
                    source.addAll(updated);
                });
    }

    public Flowable<DiffUtil.DiffResult> getEvent(Event event, List<Identifiable> eventItems) {
        return repository.get(event).concatMapDelayError(sameEvent -> Flowable.fromCallable(() -> {
            List<Identifiable> stale = new ArrayList<>(eventItems);
            List<Identifiable> updated = eventListFunction.apply(sameEvent);

            return calculateDiff(new ModelDiffCallback(updated, stale));
        })
                .subscribeOn(computation())
                .observeOn(mainThread())
                .doOnNext(diffResult -> {
                    eventItems.clear();
                    eventItems.addAll(eventListFunction.apply(sameEvent));
                }));
    }

    public Single<DiffUtil.DiffResult> updateEvent(final Event event, List<Identifiable> eventItems) {
        return repository.createOrUpdate(event).flatMap(sameEvent -> Single.fromCallable(() -> {
            List<Identifiable> stale = new ArrayList<>(eventItems);
            List<Identifiable> updated = eventListFunction.apply(sameEvent);

            return calculateDiff(new ModelDiffCallback(updated, stale));
        })
                .subscribeOn(computation())
                .observeOn(mainThread())
                .doOnSuccess(diffResult -> {
                    eventItems.clear();
                    eventItems.addAll(eventListFunction.apply(sameEvent));
                }));
    }

    public Single<DiffUtil.DiffResult> rsvpEvent(final Event event, List<Identifiable> eventItems, boolean attending) {
        return repository.rsvpEvent(event, attending).map(sameEvent -> {
            if (attending) sameEvent.getAbsentees().remove(signedInUser);
            else sameEvent.getAttendees().remove(signedInUser);
            return sameEvent;
        }).flatMap(sameEvent -> Single.fromCallable(() -> {
            List<Identifiable> stale = new ArrayList<>(eventItems);
            List<Identifiable> updated = eventListFunction.apply(sameEvent);

            return calculateDiff(new ModelDiffCallback(updated, stale));
        })
                .subscribeOn(computation())
                .observeOn(mainThread())
                .doOnSuccess(diffResult -> {
                    eventItems.clear();
                    eventItems.addAll(eventListFunction.apply(sameEvent));
                }));
    }

    public Single<Event> delete(final Event event) {
        return repository.delete(event);
    }

    private Function<Event, List<Identifiable>> eventListFunction = event -> {
        List<Identifiable> result = new ArrayList<>();
        int eventSize = event.size();

        for (int i = 0; i < eventSize; i++) result.add(event.get(i));
        result.add(event.getTeam());
        result.addAll(event.getAttendees());
        result.addAll(event.getAbsentees());

        return result;
    };
}
