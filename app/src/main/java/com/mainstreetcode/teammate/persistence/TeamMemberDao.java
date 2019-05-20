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

package com.mainstreetcode.teammate.persistence;

import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.TeamMember;
import com.mainstreetcode.teammate.persistence.entity.JoinRequestEntity;
import com.mainstreetcode.teammate.persistence.entity.RoleEntity;
import com.mainstreetcode.teammate.util.Logger;

import java.util.Collections;
import java.util.List;

import io.reactivex.functions.BiConsumer;

public class TeamMemberDao extends EntityDao<TeamMember> {
    @Override
    protected String getTableName() {
        return "INVALID";
    }

    @Override
    public void insert(List<TeamMember> models) {
        daos((roleDao, requestDao) -> TeamMember.split(models, (roles, requests) -> {
            roleDao.insert(Collections.unmodifiableList(roles));
            requestDao.insert(Collections.unmodifiableList(requests));
        }));
    }

    @Override
    protected void update(List<TeamMember> models) {
        daos((roleDao, requestDao) -> TeamMember.split(models, (roles, requests) -> {
            roleDao.update(Collections.unmodifiableList(roles));
            requestDao.update(Collections.unmodifiableList(requests));
        }));
    }

    @Override
    public void delete(TeamMember model) {
        daos((roleDao, requestDao) -> {
            Model wrapped = model.getWrappedModel();
            if (wrapped instanceof Role) roleDao.delete((Role) wrapped);
            if (wrapped instanceof JoinRequest) requestDao.delete((JoinRequest) wrapped);
        });
    }

    @Override
    public void delete(List<TeamMember> models) {
        daos((roleDao, requestDao) -> TeamMember.split(models, (roles, requests) -> {
            roleDao.delete(Collections.unmodifiableList(roles));
            requestDao.delete(Collections.unmodifiableList(requests));
        }));
    }

    private void daos(BiConsumer<EntityDao<RoleEntity>, EntityDao<JoinRequestEntity>> daoBiConsumer) {
        AppDatabase appDatabase = AppDatabase.getInstance();
        try {daoBiConsumer.accept(appDatabase.roleDao(), appDatabase.joinRequestDao());}
        catch (Exception e) { Logger.log("TeamMemberDao", "Error getting implementation daos", e);}
    }
}
