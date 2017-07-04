package com.mainstreetcode.teammates.repository;


import android.webkit.MimeTypeMap;

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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

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

        String path = team.get(Team.LOGO_POSITION).getValue();
        File file = new File(path);

        if (file.exists()) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(path);
            if (extension != null) {
                String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (type != null) {
                    RequestBody requestFile = RequestBody.create(MediaType.parse(type), file);
                    MultipartBody.Part body = MultipartBody.Part.createFormData("logo", file.getName(), requestFile);
                    teamObservable = teamObservable.flatMap(put -> api.uploadTeamLogo(team.getId(), body));
                }
            }
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
                    List<User> users = team.getUsers();
                    List<Role> roles = new ArrayList<>(users.size());

                    for (User user : users) roles.add(new Role(user.getRole(), user, team));

                    roleDao.delete(roles);
                    teamDao.delete(team);

                    return just(team);
                });
    }

    private Observable<List<Team>> saveTeams(List<Team> teams) {
        teamDao.insert(teams);
        return just(teams);
    }

    /**
     * Used to save a team that has it's users populated
     */
    private Observable<Team> saveTeam(Team team) {
        List<User> users = team.getUsers();
        List<Role> roles = new ArrayList<>(users.size());

        for (User user : users) roles.add(new Role(user.getRole(), user, team));

        teamDao.insert(Collections.singletonList(team));
        userDao.insert(users);
        roleDao.insert(roles);

        return just(team);
    }

    public Observable<User> updateTeamUser(Team team, User user) {
        return api.updateTeamUser(team.getId(), user.getId(), user)
                .flatMap(userRepository::saveUser)
                .observeOn(mainThread());
    }

    public Observable<JoinRequest> joinTeam(Team team, String role) {
        return api.joinTeam(team.getId(), role).observeOn(mainThread());
    }

    public Observable<JoinRequest> approveUser(Team team, User user, boolean approve) {
        Observable<JoinRequest> observable = approve
                ? api.approveUser(team.getId(), user.getId())
                : api.declineUser(team.getId(), user.getId());
        return observable.observeOn(mainThread());
    }

    public Observable<User> dropUser(Team team, User user) {
        return api.dropUser(team.getId(), user.getId()).flatMap(droppedUser -> {
            roleDao.delete(Collections.singletonList(new Role(user.getRole(), user, team)));
            return just(user);
        });
    }
}
