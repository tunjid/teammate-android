package com.mainstreetcode.teammates.repository;

import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.EntityDao;
import com.mainstreetcode.teammates.persistence.RoleDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.TeammateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import okhttp3.MultipartBody;

import static io.reactivex.schedulers.Schedulers.io;

public class RoleRepository extends ModelRepository<Role> {

    private final TeammateApi api;
    private final RoleDao roleDao;

    private static RoleRepository ourInstance;

    private RoleRepository() {
        api = TeammateService.getApiInstance();
        roleDao = AppDatabase.getInstance().roleDao();
    }

    public static RoleRepository getInstance() {
        if (ourInstance == null) ourInstance = new RoleRepository();
        return ourInstance;
    }

    @Override
    public EntityDao<? super Role> dao() {
        return roleDao;
    }

    @Override
    public Single<Role> createOrUpdate(Role model) {
        Single<Role> roleSingle = api.updateRole(model.getId(), model)
                .doOnError(throwable -> deleteInvalidModel(model, throwable));

        MultipartBody.Part body = getBody(model.getHeaderItem().getValue(), Role.PHOTO_UPLOAD_KEY);
        if (body != null) {
            roleSingle = roleSingle.flatMap(put -> api.uploadRolePhoto(model.getId(), body));
        }

        return roleSingle.map(getLocalUpdateFunction(model)).map(getSaveFunction());
    }

    @Override
    public Flowable<Role> get(String id) {
        return Flowable.error(new TeammateException(""));
    }

    @Override
    public Single<Role> delete(Role model) {
        return api.deleteRole(model.getId())
                .map(this::deleteLocally)
                .doOnError(throwable -> deleteInvalidModel(model, throwable));
    }

    @Override
    Function<List<Role>, List<Role>> provideSaveManyFunction() {
        return models -> {
            List<Team> teams = new ArrayList<>(models.size());
            List<User> users = new ArrayList<>(models.size());

            for (Role role : models) {
                teams.add(role.getTeam());
                users.add(role.getUser());
            }

            if (!teams.isEmpty()) TeamRepository.getInstance().getSaveManyFunction().apply(teams);
            if (!users.isEmpty()) UserRepository.getInstance().getSaveManyFunction().apply(users);

            roleDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }

    public Single<Role> approveUser(JoinRequest request) {
        JoinRequestRepository joinRequestRepository = JoinRequestRepository.getInstance();

        return api.approveUser(request.getId())
                .map(getSaveFunction())
                .doOnSuccess(role -> joinRequestRepository.delete(request))
                .doOnError(throwable -> joinRequestRepository.deleteInvalidModel(request, throwable));
    }

    public Maybe<Role> getRoleInTeam(String userId, String teamId) {
        return roleDao.getRoleInTeam(userId, teamId).subscribeOn(io());
    }
}
