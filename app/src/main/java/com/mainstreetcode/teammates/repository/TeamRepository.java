package com.mainstreetcode.teammates.repository;


import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.JoinRequestDao;
import com.mainstreetcode.teammates.persistence.RoleDao;
import com.mainstreetcode.teammates.persistence.TeamDao;
import com.mainstreetcode.teammates.persistence.UserDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import okhttp3.MultipartBody;

import static com.mainstreetcode.teammates.repository.RepoUtils.getBody;
import static io.reactivex.Maybe.concat;
import static io.reactivex.Single.just;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

public class TeamRepository extends CrudRespository<Team> {

    private static TeamRepository ourInstance;

    private final TeammateApi api;
    private final UserDao userDao;
    private final TeamDao teamDao;
    private final RoleDao roleDao;
    private final JoinRequestDao joinRequestDao;
    private final UserRepository userRepository;

    private TeamRepository() {
        api = TeammateService.getApiInstance();
        userDao = AppDatabase.getInstance().userDao();
        teamDao = AppDatabase.getInstance().teamDao();
        roleDao = AppDatabase.getInstance().roleDao();
        joinRequestDao = AppDatabase.getInstance().joinRequestDao();
        userRepository = UserRepository.getInstance();
    }

    public static TeamRepository getInstance() {
        if (ourInstance == null) ourInstance = new TeamRepository();
        return ourInstance;
    }

    @Override
    public Single<Team> createOrUpdate(Team model) {
        Single<Team> eventSingle = model.isEmpty()
                ? api.createTeam(model).flatMap(created -> updateLocal(model, created))
                : api.updateTeam(model.getId(), model).flatMap(updated -> updateLocal(model, updated));

        MultipartBody.Part body = getBody(model.get(Team.LOGO_POSITION).getValue(), Team.PHOTO_UPLOAD_KEY);
        if (body != null) {
            eventSingle = eventSingle.flatMap(put -> api.uploadTeamLogo(model.getId(), body));
        }

        return eventSingle.flatMap(this::save).observeOn(mainThread());
    }

    @Override
    public Flowable<Team> get(String id) {
        Maybe<Team> local = teamDao.get(id).subscribeOn(io());
        Maybe<Team> remote = api.getTeam(id).flatMap(this::save).toMaybe();

        return concat(local, remote).observeOn(mainThread());
    }

    @Override
    public Single<Team> delete(Team team) {
        return api.deleteTeam(team.getId())
                .flatMap(team1 -> {
                    roleDao.delete(Collections.unmodifiableList(team.getRoles()));
                    teamDao.delete(team);

                    return just(team);
                });
    }

    Single<List<Team>> saveList(List<Team> models) {
        List<User> users = new ArrayList<>();
        List<Role> roles = new ArrayList<>();
        List<JoinRequest> joinRequests = new ArrayList<>();

        for (Team team : models) {
            List<Role> teamRoles = team.getRoles();
            List<JoinRequest> teamRequests = team.getJoinRequests();

            roles.addAll(teamRoles);
            joinRequests.addAll(teamRequests);

            for (Role role : teamRoles) users.add(role.getUser());
            for (JoinRequest request : teamRequests) users.add(request.getUser());
        }

        teamDao.insert(Collections.unmodifiableList(models));
        userDao.insert(Collections.unmodifiableList(users));
        roleDao.insert(Collections.unmodifiableList(roles));
        joinRequestDao.insert(Collections.unmodifiableList(joinRequests));

        return just(models);
    }

    public Single<List<Team>> findTeams(String queryText) {
        return api.findTeam(queryText).observeOn(mainThread());
    }

    public Flowable<List<Team>> getMyTeams() {
        User user = userRepository.getCurrentUser();

        Maybe<List<Team>> local = teamDao.myTeams(user.getId()).subscribeOn(io());
        Maybe<List<Team>> remote = api.getMyTeams().flatMap(this::saveList).toMaybe();

        return concat(local, remote).observeOn(mainThread());
    }
}
