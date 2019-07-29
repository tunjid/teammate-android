/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.repository;


import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;

import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class BlockedUserRepo extends TeamQueryRepo<BlockedUser> {

    private final TeammateApi api;

    BlockedUserRepo() {
        api = TeammateService.getApiInstance();
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
        return api.blockedUsers(key.getId(), date, DEF_QUERY_LIMIT).map(getSaveManyFunction()).toMaybe();
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
