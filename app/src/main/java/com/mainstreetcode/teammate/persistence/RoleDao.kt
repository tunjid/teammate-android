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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.persistence.entity.RoleEntity

import java.util.Date

import io.reactivex.Maybe

/**
 * DAO for [User]
 *
 *
 * Created by Shemanigans on 6/12/17.
 */

@Dao
abstract class RoleDao : EntityDao<RoleEntity>() {

    override val tableName: String
        get() = "roles"

    @Query("SELECT * FROM roles" + " WHERE :id = role_id")
    abstract operator fun get(id: String): Maybe<Role>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun insert(models: List<RoleEntity>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun update(models: List<RoleEntity>)

    @Delete
    abstract override fun delete(models: List<RoleEntity>)

    @Query("SELECT *" +
            " FROM roles" +
            " WHERE :teamId = role_team" +
            " AND :userId = role_user")
    abstract fun getRoleInTeam(userId: String, teamId: String): Maybe<Role>

    @Query("SELECT *" +
            " FROM roles" +
            " WHERE :userId = role_user")
    abstract fun userRoles(userId: String): Maybe<List<Role>>

    @Query("DELETE FROM roles WHERE role_team = :teamId")
    abstract fun deleteByTeam(teamId: String)

    @Query("DELETE FROM roles WHERE role_user = :userId AND role_team = :teamId")
    abstract fun deleteUsers(userId: String, teamId: String)

    @Query("SELECT * FROM roles as role" +
            " WHERE :teamId = role_team" +
            " AND role_created < :date" +
            " ORDER BY role_created DESC" +
            " LIMIT :limit")
    abstract fun getRoles(teamId: String, date: Date, limit: Int): Maybe<List<Role>>
}
