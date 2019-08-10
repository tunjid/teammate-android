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

package com.mainstreetcode.teammate.viewmodel

import androidx.lifecycle.ViewModel

import com.facebook.login.LoginResult
import com.mainstreetcode.teammate.model.Message
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.repository.UserRepo
import com.mainstreetcode.teammate.util.InstantSearch
import com.mainstreetcode.teammate.util.TeammateException
import com.mainstreetcode.teammate.viewmodel.gofers.UserGofer
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

import io.reactivex.Flowable
import io.reactivex.Single

import io.reactivex.android.schedulers.AndroidSchedulers.mainThread

/**
 * View model for User and Auth
 */

class UserViewModel : ViewModel() {

    private val repository: UserRepo = RepoProvider.forRepo(UserRepo::class.java)

    val isSignedIn: Boolean
        get() = repository.isSignedIn

    val currentUser: User
        get() = repository.currentUser

    fun gofer(user: User): UserGofer = UserGofer(
            user,
            currentUser::equals,
            this::getUser,
            this::updateUser)

    fun instantSearch(): InstantSearch<String, User> =
            InstantSearch(repository::findUser) { it }

    fun signUp(firstName: String, lastName: String, primaryEmail: String, password: String): Single<User> =
            repository.signUp(firstName, lastName, primaryEmail, password).observeOn(mainThread())

    fun signIn(loginResult: LoginResult): Single<User> =
            repository.signIn(loginResult).observeOn(mainThread())

    fun signIn(email: String, password: String): Single<User> =
            repository.signIn(email, password).observeOn(mainThread())

    fun deleteAccount(): Single<User> {
        TeamViewModel.teams.clear()
        return repository.delete(currentUser).observeOn(mainThread())
    }

    fun signOut(): Single<Boolean> {
        TeamViewModel.teams.clear()
        return repository.signOut().observeOn(mainThread())
    }

    fun forgotPassword(email: String): Single<Message> =
            repository.forgotPassword(email).observeOn(mainThread())

    fun resetPassword(email: String, token: String, password: String): Single<Message> =
            repository.resetPassword(email, token, password).observeOn(mainThread())

    private fun updateUser(user: User): Single<User> =
            if (currentUser == user) repository.createOrUpdate(user) else Single.error(TeammateException(""))

    private fun getUser(user: User): Flowable<User> =
            if (currentUser == user) repository[user] else Flowable.empty()
}
