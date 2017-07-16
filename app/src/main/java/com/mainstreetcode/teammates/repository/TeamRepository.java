package com.mainstreetcode.teammates.repository;


import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;
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

import static io.reactivex.Observable.fromCallable;
import static io.reactivex.Observable.just;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

public class TeamRepository {
    private static TeamRepository ourInstance;

    private final TeammateApi api;
    private final UserDao userDao;
    private final TeamDao teamDao;
    private final RoleDao roleDao;
    private final UserRepository userRepository;

    private TeamRepository() {
        api = TeammateService.getApiInstance();
        userDao = AppDatabase.getInstance().userDao();
        teamDao = AppDatabase.getInstance().teamDao();
        roleDao = AppDatabase.getInstance().roleDao();
        userRepository = UserRepository.getInstance();
    }

    public static TeamRepository getInstance() {
        if (ourInstance == null) ourInstance = new TeamRepository();
        return ourInstance;
    }

    public Observable<Team> createTeam(Team team) {
        return api.createTeam(team)
                .flatMap(this::saveTeam)
                .observeOn(mainThread());
    }

    public Observable<Team> getTeam(Team team) {
        return api.getTeam(team.getId())
                .flatMap(this::saveTeam)
                .observeOn(mainThread());
    }

    public Observable<Team> updateTeam(Team team) {
        Observable<Team> teamObservable = api.updateTeam(team.getId(), team);

        MultipartBody.Part body = RepoUtils.getBody(team.get(Team.LOGO_POSITION).getValue(), Team.PHOTO_UPLOAD_KEY);
        if (body != null) {
            teamObservable = teamObservable.flatMap(put -> api.uploadTeamLogo(team.getId(), body));
        }

        return teamObservable.flatMap(this::saveTeam).observeOn(mainThread());
    }

    public Observable<List<Team>> findTeams(String queryText) {
        return api.findTeam(queryText).observeOn(mainThread());
    }

    public Observable<List<Team>> getMyTeams() {
        User user = userRepository.getCurrentUser();

        Observable<List<Team>> local = fromCallable(() -> teamDao.myTeams(user.getId())).subscribeOn(io());
        Observable<List<Team>> remote = api.getMyTeams().flatMap(this::saveTeams);

        return Observable.concat(local, remote).observeOn(mainThread());
    }

    public Observable<Team> deleteTeam(Team team) {
        return api.deleteTeam(team.getId())
                .flatMap(team1 -> {
                    roleDao.delete(Collections.unmodifiableList(team.getRoles()));
                    teamDao.delete(team);

                    return just(team);
                });
    }

    Observable<List<Team>> saveTeams(List<Team> teams) {
        teamDao.insert(Collections.unmodifiableList(teams));
        return just(teams);
    }

    /**
     * Used to save a team that has it's users populated
     */
    private Observable<Team> saveTeam(Team team) {
        List<Role> roles = team.getRoles();
        List<User> users = new ArrayList<>(roles.size());

        for (Role role : roles) users.add(role.getUser());

        teamDao.insert(Collections.singletonList(team));
        userDao.insert(Collections.unmodifiableList(users));
        roleDao.insert(Collections.unmodifiableList(roles));

        return just(team);
    }

    public Observable<User> updateTeamUser(Role role) {
        String teamId = role.getTeamId();
        User user = role.getUser();
        Observable<User> userObservable = api.updateTeamUser(teamId, user.getId(), user);

        MultipartBody.Part body = RepoUtils.getBody(role.get(Role.IMAGE_POSITION).getValue(), Role.PHOTO_UPLOAD_KEY);
        if (body != null) {
            userObservable = userObservable.flatMap(put -> api.uploadUserPhoto(teamId, user.getId(), body));
        }

        return userObservable.flatMap(userRepository::saveUser).observeOn(mainThread());
    }

    public Observable<JoinRequest> joinTeam(Team team, String role) {
        return api.joinTeam(team.getId(), role).observeOn(mainThread());
    }

    public Observable<Role> approveUser(JoinRequest request) {
        Observable<Role> observable = api.approveUser(request.getTeamId(), request.getUser().getId());
        return observable.observeOn(mainThread());
    }

    public Observable<JoinRequest> declineUser(JoinRequest request) {
        Observable<JoinRequest> observable = api.declineUser(request.getTeamId(), request.getUser().getId());
        return observable.observeOn(mainThread());
    }

    public Observable<User> dropUser(Role role) {
        return api.dropUser(role.getTeamId(), role.getUser().getId()).flatMap(droppedUser -> {
            roleDao.delete(Collections.singletonList(role));
            return just(role.getUser());
        });
    }

}
