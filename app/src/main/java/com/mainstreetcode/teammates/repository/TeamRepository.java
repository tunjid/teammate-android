package com.mainstreetcode.teammates.repository;


import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.TeamDao;
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

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

public class TeamRepository extends ModelRespository<Team> {

    private static TeamRepository ourInstance;

    private final TeammateApi api;
    private final TeamDao teamDao;
    private final ModelRespository<User> userRepository;
    private final ModelRespository<Role> roleRespository;
    private final ModelRespository<JoinRequest> joinRequestRespository;

    private TeamRepository() {
        api = TeammateService.getApiInstance();
        teamDao = AppDatabase.getInstance().teamDao();
        userRepository = UserRepository.getInstance();
        roleRespository = RoleRepository.getInstance();
        joinRequestRespository = JoinRequestRepository.getInstance();
    }

    public static TeamRepository getInstance() {
        if (ourInstance == null) ourInstance = new TeamRepository();
        return ourInstance;
    }

    @Override
    public Single<Team> createOrUpdate(Team model) {
        Single<Team> eventSingle = model.isEmpty()
                ? api.createTeam(model).map(localMapper(model))
                : api.updateTeam(model.getId(), model).map(localMapper(model));

        MultipartBody.Part body = getBody(model.get(Team.LOGO_POSITION).getValue(), Team.PHOTO_UPLOAD_KEY);
        if (body != null) {
            eventSingle = eventSingle.flatMap(put -> api.uploadTeamLogo(model.getId(), body));
        }

        return eventSingle.map(getSaveFunction()).observeOn(mainThread());
    }

    @Override
    public Flowable<Team> get(String id) {
        Maybe<Team> local = teamDao.get(id).subscribeOn(io());
        Maybe<Team> remote = api.getTeam(id).map(getSaveFunction()).toMaybe();

        return cacheThenRemote(local, remote);
    }

    @Override
    public Single<Team> delete(Team team) {
        return api.deleteTeam(team.getId())
                .map(team1 -> {
                    teamDao.delete(team);
                    return team;
                });
    }

    @Override
    Function<List<Team>, List<Team>> provideSaveManyFunction() {
        return models -> {
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

            teamDao.upsert(Collections.unmodifiableList(models));
            userRepository.getSaveManyFunction().apply(users);
            roleRespository.getSaveManyFunction().apply(roles);
            joinRequestRespository.getSaveManyFunction().apply(joinRequests);

            return models;
        };
    }

    public Single<List<Team>> findTeams(String queryText) {
        return api.findTeam(queryText).observeOn(mainThread());
    }

    public Flowable<List<Team>> getMyTeams(String userId) {
        Maybe<List<Team>> local = teamDao.myTeams(userId).subscribeOn(io());
        Maybe<List<Team>> remote = api.getMyTeams().map(getSaveManyFunction()).toMaybe();

        return cacheThenRemote(local, remote);
    }
}
