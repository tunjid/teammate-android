package com.mainstreetcode.teammate.repository;


import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.TournamentDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import okhttp3.MultipartBody;

import static io.reactivex.schedulers.Schedulers.io;

public class TournamentRepository extends TeamQueryRepository<Tournament> {

    private static TournamentRepository ourInstance;

    private final TeammateApi api;
    private final TournamentDao tournamentDao;
    private final ModelRepository<Team> teamRepository;

    private TournamentRepository() {
        api = TeammateService.getApiInstance();
        tournamentDao = AppDatabase.getInstance().tournamentDao();
        teamRepository = TeamRepository.getInstance();
    }

    public static TournamentRepository getInstance() {
        if (ourInstance == null) ourInstance = new TournamentRepository();
        return ourInstance;
    }

    @Override
    public EntityDao<? super Tournament> dao() {
        return tournamentDao;
    }

    public Single<Tournament> addCompetitors(Tournament tournament, List<Competitor> competitors) {
        return api.addCompetitors(tournament.getId(), competitors)
                .map(getLocalUpdateFunction(tournament))
                .map(getSaveFunction());
    }

    @Override
    public Single<Tournament> createOrUpdate(Tournament tournament) {
        Single<Tournament> tournamentSingle = tournament.isEmpty()
                ? api.createTournament(tournament.getHost().getId(), tournament).map(getLocalUpdateFunction(tournament))
                : api.updateTournament(tournament.getId(), tournament)
                .map(getLocalUpdateFunction(tournament))
                .doOnError(throwable -> deleteInvalidModel(tournament, throwable));

        MultipartBody.Part body = getBody(tournament.getHeaderItem().getValue(), Tournament.PHOTO_UPLOAD_KEY);
        if (body != null) {
            tournamentSingle = tournamentSingle.flatMap(put -> api.uploadTournamentPhoto(tournament.getId(), body));
        }

        return tournamentSingle.map(getSaveFunction());
    }

    @Override
    public Flowable<Tournament> get(String id) {
        Maybe<Tournament> local = tournamentDao.get(id).subscribeOn(io());
        Maybe<Tournament> remote = api.getTournament(id).map(getSaveFunction()).toMaybe();

        return fetchThenGetModel(local, remote);
    }

    @Override
    public Single<Tournament> delete(Tournament tournament) {
        return api.deleteTournament(tournament.getId())
                .map(this::deleteLocally)
                .doOnError(throwable -> deleteInvalidModel(tournament, throwable));
    }

    @Override
    Maybe<List<Tournament>> localModelsBefore(Team team, @Nullable Date date) {
        if (date == null) date = getFutureDate();
        return tournamentDao.getTournaments(team.getId(), date).subscribeOn(io());
    }

    @Override
    Maybe<List<Tournament>> remoteModelsBefore(Team team, @Nullable Date date) {
        return api.getTournaments(team.getId(), date).map(getSaveManyFunction()).toMaybe();
    }

    @Override
    Function<List<Tournament>, List<Tournament>> provideSaveManyFunction() {
        return models -> {
            List<Team> teams = new ArrayList<>(models.size());
            for (Tournament tournament : models) teams.add(tournament.getTeam());

            teamRepository.saveAsNested().apply(teams);
            tournamentDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }
}
