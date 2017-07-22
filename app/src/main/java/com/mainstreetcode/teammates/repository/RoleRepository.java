package com.mainstreetcode.teammates.repository;

import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.RoleDao;
import com.mainstreetcode.teammates.persistence.UserDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;

import static io.reactivex.Observable.fromCallable;
import static io.reactivex.Observable.just;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

public class RoleRepository extends CrudRespository<Role> {

    private final TeammateApi api;
    private final UserDao userDao;
    private final RoleDao roleDao;

    private static RoleRepository ourInstance;

    private RoleRepository() {
        api = TeammateService.getApiInstance();
        userDao = AppDatabase.getInstance().userDao();
        roleDao = AppDatabase.getInstance().roleDao();
    }

    public static RoleRepository getInstance() {
        if (ourInstance == null) ourInstance = new RoleRepository();
        return ourInstance;
    }

    @Override
    public Observable<Role> createOrUpdate(Role model) {
        Observable<Role> roleObservable = api.updateRole(model.getId(), model);

        MultipartBody.Part body = RepoUtils.getBody(model.get(Role.IMAGE_POSITION).getValue(), Role.PHOTO_UPLOAD_KEY);
        if (body != null) {
            roleObservable = roleObservable.flatMap(put -> api.uploadRolePhoto(model.getId(), body));
        }

        return roleObservable
                .flatMap(updated -> updateLocal(model, updated))
                .flatMap(this::save).observeOn(mainThread());
    }

    @Override
    public Observable<Role> get(String id) {
        return null;
    }

    @Override
    public Observable<Role> delete(Role model) {
        roleDao.delete(Collections.singletonList(model));
        return just(model);
    }

    @Override
    Observable<List<Role>> saveList(List<Role> models) {
        List<User> users = new ArrayList<>(models.size());

        for (Role role : models) users.add(role.getUser());

        userDao.insert(Collections.unmodifiableList(users));
        roleDao.insert(Collections.unmodifiableList(models));

        return just(models);
    }

    public Observable<Role> approveUser(JoinRequest request) {
        Observable<Role> observable = api.approveUser(request.getId())
                .flatMap(this::save);
        return observable.observeOn(mainThread());
    }

    public Observable<Role> dropRole(Role role) {
        return api.deleteRole(role.getId()).flatMap(this::delete);
    }

    public Observable<Role> getRoleInTeam(String userId, String teamId) {
        return fromCallable(() ->roleDao.getRoleInTeam(userId, teamId))
                .subscribeOn(io())
                .observeOn(mainThread());
    }
}
