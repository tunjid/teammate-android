package com.mainstreetcode.teammates.repository;

import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.EntityDao;
import com.mainstreetcode.teammates.persistence.JoinRequestDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.TeammateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static io.reactivex.Single.just;

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
        return Flowable.error(new TeammateException(""));
    }

    @Override
    public Single<JoinRequest> delete(JoinRequest model) {
        joinRequestDao.delete(model);
        return just(model);
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

            if (!teams.isEmpty()) TeamRepository.getInstance().getSaveManyFunction().apply(teams);
            if (!users.isEmpty()) UserRepository.getInstance().getSaveManyFunction().apply(users);

            joinRequestDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }

    public Single<JoinRequest> dropJoinRequest(JoinRequest joinRequest) {
        return api.declineUser(joinRequest.getId())
                .doOnError(throwable -> deleteInvalidModel(joinRequest, throwable))
                .flatMap(this::delete);
    }
}
