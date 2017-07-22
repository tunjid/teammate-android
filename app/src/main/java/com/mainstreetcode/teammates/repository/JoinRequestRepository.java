package com.mainstreetcode.teammates.repository;

import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.JoinRequestDao;
import com.mainstreetcode.teammates.persistence.UserDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

import static io.reactivex.Observable.just;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class JoinRequestRepository extends CrudRespository<JoinRequest> {

    private final TeammateApi api;
    private final UserDao userDao;
    private final JoinRequestDao joinRequestDao;

    private static JoinRequestRepository ourInstance;

    private JoinRequestRepository() {
        api = TeammateService.getApiInstance();
        userDao = AppDatabase.getInstance().userDao();
        joinRequestDao = AppDatabase.getInstance().joinRequestDao();
    }

    public static JoinRequestRepository getInstance() {
        if (ourInstance == null) ourInstance = new JoinRequestRepository();
        return ourInstance;
    }

    @Override
    public Observable<JoinRequest> createOrUpdate(JoinRequest model) {
        return api.joinTeam(model)
                .flatMap(updated -> updateLocal(model, updated))
                .flatMap(this::save).observeOn(mainThread());
    }

    @Override
    public Observable<JoinRequest> get(String id) {
        return null;
    }

    @Override
    public Observable<JoinRequest> delete(JoinRequest model) {
        joinRequestDao.delete(Collections.singletonList(model));
        return just(model);
    }

    @Override
    Observable<List<JoinRequest>> saveList(List<JoinRequest> models) {
        List<User> users = new ArrayList<>(models.size());

        for (JoinRequest role : models) users.add(role.getUser());

        userDao.insert(Collections.unmodifiableList(users));
        joinRequestDao.insert(Collections.unmodifiableList(models));

        return just(models);
    }

    public Observable<JoinRequest> dropJoinRequest(JoinRequest joinRequest) {
        return api.declineUser(joinRequest.getId()).flatMap(this::delete);
    }
}
