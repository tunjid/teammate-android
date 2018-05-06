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
