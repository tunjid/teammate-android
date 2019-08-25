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

package com.mainstreetcode.teammate.viewmodel.gofers

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.repository.UserRepo
import com.mainstreetcode.teammate.util.FunctionalDiff
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class RoleGofer(
        model: Role,
        onError: (Throwable) -> Unit,
        private val getFunction: (Role) -> Flowable<Role>,
        private val deleteFunction: (Role) -> Single<Role>,
        private val updateFunction: (Role) -> Single<Role>
) : TeamHostingGofer<Role>(model, onError) {

    init {
        this.items.addAll(model.asItems())
    }

    fun canChangeRoleFields(): Boolean = hasPrivilegedRole() || signedInUser == model.user

    fun getDropRolePrompt(fragment: Fragment): String = when (val roleUser = model.user) {
        RepoProvider.forRepo(UserRepo::class.java).currentUser -> fragment.getString(R.string.confirm_user_leave)
        else -> fragment.getString(R.string.confirm_user_drop, roleUser.firstName)
    }

    override fun getImageClickMessage(fragment: Fragment): String? =
            if (hasPrivilegedRole() || signedInUser == model.user) null else fragment.getString(R.string.no_permission)

    public override fun fetch(): Flowable<DiffUtil.DiffResult> =
            FunctionalDiff.of(getFunction.invoke(model).map(Role::asDifferentiables), items) { _, updated -> updated }

    override fun upsert(): Single<DiffUtil.DiffResult> =
            FunctionalDiff.of(updateFunction.invoke(model).map(Role::asDifferentiables), items) { _, updated -> updated }

    public override fun delete(): Completable = deleteFunction.invoke(model).ignoreElement()
}
