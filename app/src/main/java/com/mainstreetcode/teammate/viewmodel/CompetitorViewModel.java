package com.mainstreetcode.teammate.viewmodel;

import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.CompetitorRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;

/**
 * View model for User and Auth
 */

public class CompetitorViewModel extends MappedViewModel<Class<User>, Competitor> {

    private final CompetitorRepository repository;
    private final List<Identifiable> declined = new ArrayList<>();

    public CompetitorViewModel() { repository = CompetitorRepository.getInstance(); }

    @Override
    Class<Competitor> valueClass() { return Competitor.class; }

    @Override
    public List<Identifiable> getModelList(Class<User> key) { return declined; }

    @Override
    Flowable<List<Competitor>> fetch(Class<User> key, boolean fetchLatest) {
        return repository.getDeclined(getQueryDate(fetchLatest, key, Competitor::getCreated)).toFlowable();
    }
}
