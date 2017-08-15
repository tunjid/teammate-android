package com.mainstreetcode.teammates.repository;

import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.JoinRequestDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static io.reactivex.Single.just;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class JoinRequestRepository extends ModelRespository<JoinRequest> {

    private final TeammateApi api;
    private final UserRepository userRepository;
    private final JoinRequestDao joinRequestDao;

    private static JoinRequestRepository ourInstance;

    private JoinRequestRepository() {
        api = TeammateService.getApiInstance();
        userRepository = UserRepository.getInstance();
        joinRequestDao = AppDatabase.getInstance().joinRequestDao();
    }

    public static JoinRequestRepository getInstance() {
        if (ourInstance == null) ourInstance = new JoinRequestRepository();
        return ourInstance;
    }

    @Override
    public Single<JoinRequest> createOrUpdate(JoinRequest model) {
        return api.joinTeam(model)
                .map(localMapper(model))
                .map(getSaveFunction()).observeOn(mainThread());
    }

    @Override
    public Flowable<JoinRequest> get(String id) {
        return null;
    }

    @Override
    public Single<JoinRequest> delete(JoinRequest model) {
        joinRequestDao.delete(Collections.singletonList(model));
        return just(model);
    }

    @Override
    Function<List<JoinRequest>, List<JoinRequest>> provideSaveManyFunction() {
        return models -> {
            List<User> users = new ArrayList<>(models.size());

            for (JoinRequest role : models) users.add(role.getUser());

            userRepository.getSaveManyFunction().apply(Collections.unmodifiableList(users));

            joinRequestDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }

    public Single<JoinRequest> dropJoinRequest(JoinRequest joinRequest) {
        return api.declineUser(joinRequest.getId()).flatMap(this::delete);
    }
}
