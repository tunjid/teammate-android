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

package com.mainstreetcode.teammate.persistence

import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.TeamMember
import com.mainstreetcode.teammate.persistence.entity.JoinRequestEntity
import com.mainstreetcode.teammate.persistence.entity.RoleEntity
import com.mainstreetcode.teammate.model.split
import java.util.*

class TeamMemberDao : EntityDao<TeamMember>() {

    override val tableName: String
        get() = "INVALID"

    override fun insert(models: List<TeamMember>) = daos { roleDao, requestDao ->
        models.split { roles, requests ->
            roleDao.insert(Collections.unmodifiableList<RoleEntity>(roles))
            requestDao.insert(Collections.unmodifiableList<JoinRequestEntity>(requests))
        }
    }

    override fun update(models: List<TeamMember>) = daos { roleDao, requestDao ->
        models.split { roles, requests ->
            roleDao.update(roles)
            requestDao.update(requests)
        }
    }

    override fun delete(model: TeamMember) = daos { roleDao, requestDao ->
        val wrapped = model.wrappedModel
        if (wrapped is Role) roleDao.delete(wrapped)
        else if (wrapped is JoinRequest) requestDao.delete(wrapped)
    }

    override fun delete(models: List<TeamMember>) = daos { roleDao, requestDao ->
        models.split { roles, requests ->
            roleDao.delete(Collections.unmodifiableList<RoleEntity>(roles))
            requestDao.delete(Collections.unmodifiableList<JoinRequestEntity>(requests))
        }
    }

    private fun daos(daoBiConsumer: (EntityDao<RoleEntity>, EntityDao<JoinRequestEntity>) -> Unit) {
        val appDatabase = AppDatabase.instance
        daoBiConsumer.invoke(appDatabase.roleDao(), appDatabase.joinRequestDao())
    }
}
