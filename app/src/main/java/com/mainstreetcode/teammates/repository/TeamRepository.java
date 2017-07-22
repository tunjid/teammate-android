package com.mainstreetcode.teammates.repository;


import com.mainstreetcode.teammates.model.Event;
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

import io.reactivex.Observable;
import okhttp3.MultipartBody;

import static com.mainstreetcode.teammates.repository.RepoUtils.getBody;
import static io.reactivex.Observable.fromCallable;
import static io.reactivex.Observable.just;
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
    public Observable<Team> createOrUpdate(Team model) {
        Observable<Team> eventObservable = model.isEmpty()
                ? api.createTeam(model).flatMap(created -> updateLocal(model, created))
                : api.updateTeam(model.getId(), model).flatMap(updated -> updateLocal(model, updated));

        MultipartBody.Part body = getBody(model.get(Team.LOGO_POSITION).getValue(), Event.PHOTO_UPLOAD_KEY);
        if (body != null) {
            eventObservable = eventObservable.flatMap(put -> api.uploadTeamLogo(model.getId(), body));
        }

        return eventObservable.flatMap(this::save).observeOn(mainThread());
    }

    @Override
    public Observable<Team> get(String id) {
        return api.getTeam(id)
                .flatMap(this::save)
                .observeOn(mainThread());
    }

    @Override
    Observable<Team> save(Team model) {
        return super.save(model).flatMap(team -> {
            joinRequestDao.insert(Collections.unmodifiableList(model.getJoinRequests()));
            return just(team);
        });
    }

    @Override
    public Observable<Team> delete(Team team) {
        return api.deleteTeam(team.getId())
                .flatMap(team1 -> {
                    roleDao.delete(Collections.unmodifiableList(team.getRoles()));
                    teamDao.delete(team);

                    return just(team);
                });
    }

    Observable<List<Team>> saveList(List<Team> models) {
        List<Role> roles = new ArrayList<>();
        List<User> users = new ArrayList<>();

        for (Team team : models) {
            List<Role> teamRoles = team.getRoles();
            roles.addAll(teamRoles);
            for (Role role : teamRoles) users.add(role.getUser());
        }

        teamDao.insert(Collections.unmodifiableList(models));
        userDao.insert(Collections.unmodifiableList(users));
        roleDao.insert(Collections.unmodifiableList(roles));

        return just(models);
    }

    public Observable<List<Team>> findTeams(String queryText) {
        return api.findTeam(queryText).observeOn(mainThread());
    }

    public Observable<List<Team>> getMyTeams() {
        User user = userRepository.getCurrentUser();

        Observable<List<Team>> local = fromCallable(() -> teamDao.myTeams(user.getId())).subscribeOn(io());
        Observable<List<Team>> remote = api.getMyTeams().flatMap(this::saveList);

        return Observable.concat(local, remote).observeOn(mainThread());
    }
}
