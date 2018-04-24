package com.mainstreetcode.teammate.repository;

import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.JoinRequestDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class JoinRequestRepository extends ModelRepository<JoinRequest> {

    private final TeammateApi api;
    private final JoinRequestDao joinRequestDao;

    private static JoinRequestRepository ourInstance;

    private JoinRequestRepository() {
        api = TeammateService.getApiInstance();
        joinRequestDao = AppDatabase.getInstance().joinRequestDao();
    }

    public static JoinRequestRepository getInstance() {
        if (ourInstance == null) ourInstance = new JoinRequestRepository();
        return ourInstance;
    }

    @Override
    public EntityDao<? super JoinRequest> dao() {
        return joinRequestDao;
    }

    @Override
    public Single<JoinRequest> createOrUpdate(JoinRequest model) {
        Single<JoinRequest> call = model.isUserApproved() ? api.joinTeam(model) : api.inviteUser(model);
        return call.map(getLocalUpdateFunction(model)).map(getSaveFunction());
    }

    @Override
    public Flowable<JoinRequest> get(String id) {
        return api.getJoinRequest(id).toFlowable();
    }

    @Override
    public Single<JoinRequest> delete(JoinRequest model) {
        return api.deleteJoinRequest(model.getId())
                .map(this::deleteLocally)
                .doOnError(throwable -> deleteInvalidModel(model, throwable));
    }

    @Override
    Function<List<JoinRequest>, List<JoinRequest>> provideSaveManyFunction() {
        return models -> {
            List<Team> teams = new ArrayList<>(models.size());
            List<User> users = new ArrayList<>(models.size());

            for (JoinRequest request : models) {
                teams.add(request.getTeam());
                users.add(request.getUser());
            }

            if (!teams.isEmpty()) TeamRepository.getInstance().saveAsNested().apply(teams);
            if (!users.isEmpty()) UserRepository.getInstance().saveAsNested().apply(users);

            joinRequestDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }
}
