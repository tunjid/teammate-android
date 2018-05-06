package com.mainstreetcode.teammate.repository;


import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class BlockedUserRepository extends TeamQueryRepository<BlockedUser> {

    private static BlockedUserRepository ourInstance;

    private final TeammateApi api;

    private BlockedUserRepository() {
        api = TeammateService.getApiInstance();
    }

    public static BlockedUserRepository getInstance() {
        if (ourInstance == null) ourInstance = new BlockedUserRepository();
        return ourInstance;
    }

    @Override
    public EntityDao<? super BlockedUser> dao() {
        return EntityDao.daDont();
    }

    @Override
    public Single<BlockedUser> createOrUpdate(BlockedUser model) {
        return api.blockUser(model.getTeam().getId(), model)
                .doOnSuccess(ignored -> deleteBlockedUser(model.getUser(), model.getTeam()));
    }

    @Override
    public Flowable<BlockedUser> get(String id) {
        return Flowable.empty();
    }

    @Override
    public Single<BlockedUser> delete(BlockedUser blockedUser) {
        return api.unblockUser(blockedUser.getTeam().getId(), blockedUser);
    }

    @Override
    Maybe<List<BlockedUser>> localModelsBefore(Team key, @Nullable Date date) {
        return Maybe.empty();
    }

    @Override
    Maybe<List<BlockedUser>> remoteModelsBefore(Team key, @Nullable Date date) {
        return api.blockedUsers(key.getId(), date).map(getSaveManyFunction()).toMaybe();
    }

    @Override
    Function<List<BlockedUser>, List<BlockedUser>> provideSaveManyFunction() {
        return models -> models;
    }

    private void deleteBlockedUser(User user, Team team) {
        String userId = user.getId();
        String teamId = team.getId();
        AppDatabase database = AppDatabase.getInstance();
        database.roleDao().deleteUsers(userId, teamId);
        database.guestDao().deleteUsers(userId, teamId);
        database.joinRequestDao().deleteUsers(userId, teamId);
    }
}
