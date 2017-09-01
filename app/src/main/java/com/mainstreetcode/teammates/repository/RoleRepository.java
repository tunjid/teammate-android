package com.mainstreetcode.teammates.repository;

import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.RoleDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import okhttp3.MultipartBody;

import static io.reactivex.Single.just;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

public class RoleRepository extends ModelRespository<Role> {

    private final TeammateApi api;
    private final RoleDao roleDao;
    private final ModelRespository<User> userRepository;

    private static RoleRepository ourInstance;

    private RoleRepository() {
        api = TeammateService.getApiInstance();
        roleDao = AppDatabase.getInstance().roleDao();
        userRepository = UserRepository.getInstance();
    }

    public static RoleRepository getInstance() {
        if (ourInstance == null) ourInstance = new RoleRepository();
        return ourInstance;
    }

    @Override
    public Single<Role> createOrUpdate(Role model) {
        Single<Role> roleSingle = api.updateRole(model.getId(), model);

        MultipartBody.Part body = ModelRespository.getBody(model.get(Role.IMAGE_POSITION).getValue(), Role.PHOTO_UPLOAD_KEY);
        if (body != null) {
            roleSingle = roleSingle.flatMap(put -> api.uploadRolePhoto(model.getId(), body));
        }

        return roleSingle.map(localMapper(model)).map(getSaveFunction()).observeOn(mainThread());
    }

    @Override
    public Flowable<Role> get(String id) {
        return null;
    }

    @Override
    public Single<Role> delete(Role model) {
        roleDao.delete(Collections.singletonList(model));
        return just(model);
    }

    @Override
    Function<List<Role>, List<Role>> provideSaveManyFunction() {
        return models -> {
            List<User> users = new ArrayList<>(models.size());

            for (Role role : models) users.add(role.getUser());

            userRepository.getSaveManyFunction().apply(users);
            roleDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }

    public Single<Role> approveUser(JoinRequest request) {
        Single<Role> observable = api.approveUser(request.getId()).map(getSaveFunction());
        return observable.observeOn(mainThread());
    }

    public Single<Role> dropRole(Role role) {
        return api.deleteRole(role.getId()).flatMap(this::delete);
    }

    public Maybe<Role> getRoleInTeam(String userId, String teamId) {
        return roleDao.getRoleInTeam(userId, teamId).subscribeOn(io()).observeOn(mainThread());
    }
}
