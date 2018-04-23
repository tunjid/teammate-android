package com.mainstreetcode.teammate.repository;

import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.RoleDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.TeammateException;

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
                .map(getLocalUpdateFunction(model))
                .doOnError(throwable -> deleteInvalidModel(model, throwable));

        MultipartBody.Part body = getBody(model.getHeaderItem().getValue(), Role.PHOTO_UPLOAD_KEY);
        if (body != null) {
            roleSingle = roleSingle.flatMap(put -> api.uploadRolePhoto(model.getId(), body).map(getLocalUpdateFunction(model)));
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
            int size = models.size();
            List<Team> teams = new ArrayList<>(size);
            List<User> users = new ArrayList<>(size);
            String[] userIds = new String[size];

            for (int i = 0; i < models.size(); i++) {
                Role role = models.get(i);
                User user = role.getUser();
                users.add(user);
                userIds[i] = user.getId();
                teams.add(role.getTeam());
            }

            if (!teams.isEmpty()) TeamRepository.getInstance().getSaveManyFunction().apply(teams);
            if (!users.isEmpty()) UserRepository.getInstance().getSaveManyFunction().apply(users);

            roleDao.upsert(Collections.unmodifiableList(models));

            if (teams.size() == 1) {
                String teamId = teams.get(0).getId();
                AppDatabase.getInstance().joinRequestDao().deleteRequestsFromTeam(teamId, userIds);
            }

            return models;
        };
    }

    public Maybe<Role> getRoleInTeam(String userId, String teamId) {
        return roleDao.getRoleInTeam(userId, teamId).subscribeOn(io());
    }

    public Single<Role> acceptInvite(JoinRequest request) {
        return apply(request, api.acceptInvite(request.getId()));
    }

    public Single<Role> approveUser(JoinRequest request) {
        return apply(request, api.approveUser(request.getId()));
    }

    private Single<Role> apply(JoinRequest request, Single<Role> apiSingle) {
        return apiSingle.map(getSaveFunction())
                .doOnSuccess(role -> AppDatabase.getInstance().joinRequestDao().delete(request))
                .doOnError(throwable -> JoinRequestRepository.getInstance().deleteInvalidModel(request, throwable));
    }

    public Flowable<List<Role>> getMyRoles() {
        String userId = UserRepository.getInstance().getCurrentUser().getId();
        Maybe<List<Role>> local = roleDao.userRoles(userId).subscribeOn(io());
        Maybe<List<Role>> remote = api.getMyRoles().map(getSaveManyFunction()).toMaybe();

        return fetchThenGet(local, remote);
    }
}
