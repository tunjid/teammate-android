package com.mainstreetcode.teammate.viewmodel;

import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.repository.CompetitorRepository;
import com.mainstreetcode.teammate.repository.TournamentRepository;
import com.mainstreetcode.teammate.viewmodel.gofers.TournamentGofer;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

import static com.mainstreetcode.teammate.util.ModelUtils.findLast;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for {@link Tournament tournaments}
 */

public class TournamentViewModel extends TeamMappedViewModel<Tournament> {

    private final TournamentRepository repository;

    public TournamentViewModel() {
        repository = TournamentRepository.getInstance();
    }

    public TournamentGofer gofer(Tournament tournament) {
        return new TournamentGofer(tournament, onError(tournament), this::getTournament, this::createOrUpdateTournament, this::delete,
                ignored -> CompetitorRepository.getInstance().modelsBefore(tournament, 0));
    }

    public Single<Tournament> addCompetitors(final Tournament tournament, List<Competitor> competitors) {
        return repository.addCompetitors(tournament, competitors).observeOn(mainThread());
    }

    @Override
    Flowable<List<Tournament>> fetch(Team key, boolean fetchLatest) {
        return repository.modelsBefore(key, getQueryDate(key, fetchLatest))
                .doOnError(throwable -> checkForInvalidTeam(throwable, key));
    }

    @Override
    void onErrorMessage(Message message, Team key, Identifiable invalid) {
        super.onErrorMessage(message, key, invalid);
        boolean shouldRemove = message.isInvalidObject() && invalid instanceof Tournament;
        if (shouldRemove) removeTournament((Tournament) invalid);
    }

    private Flowable<Tournament> getTournament(Tournament tournament) {
        return tournament.isEmpty() ? Flowable.empty() : repository.get(tournament);
    }

    private Single<Tournament> createOrUpdateTournament(final Tournament tournament) {
        return repository.createOrUpdate(tournament);
    }

    public Single<Tournament> delete(final Tournament tournament) {
        return repository.delete(tournament).doOnSuccess(this::removeTournament);
    }

    private Date getQueryDate(Team team, boolean fetchLatest) {
        if (fetchLatest) return null;

        Tournament tournament = findLast(getModelList(team), Tournament.class);
        return tournament == null ? null : tournament.getCreated();
    }

    private void removeTournament(Tournament tournament) {
        for (List<Identifiable> list : modelListMap.values()) list.remove(tournament);
    }
}
