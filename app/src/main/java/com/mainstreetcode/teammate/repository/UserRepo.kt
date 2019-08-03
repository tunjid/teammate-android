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

package com.mainstreetcode.teammate.repository


import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.text.TextUtils
import com.facebook.login.LoginResult
import com.google.gson.JsonObject
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.model.Message
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.persistence.UserDao
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.rest.TeammateService.SESSION_COOKIE
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.TeammateException
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.Single.just
import io.reactivex.schedulers.Schedulers.io

class UserRepo internal constructor() : ModelRepo<User>() {

    private val app: App = App.getInstance()
    private val api: TeammateApi = TeammateService.getApiInstance()
    private val userDao: UserDao = AppDatabase.getInstance().userDao()

    var currentUser: User = User.empty()
        private set

    private val userId: String?
        get() = app.getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(USER_ID, "")

    val me: Flowable<User>
        get() {
            val userId = userId
            return when {
                userId.isNullOrBlank() -> Flowable.error(TeammateException("No signed in user"))
                else -> get(userId).map(getLocalUpdateFunction(currentUser))
            }
        }

    val isSignedIn: Boolean
        get() = !TextUtils.isEmpty(userId)

    init {
        currentUser.setId(userId)
    }

    override fun dao(): EntityDao<in User> = userDao

    override fun createOrUpdate(model: User): Single<User> {
        var remote = when {
            model.isEmpty -> api.signUp(model).map(getLocalUpdateFunction(model))
            else -> api.updateUser(model.id, model).map(getLocalUpdateFunction(model))
                    .doOnError { throwable -> deleteInvalidModel(model, throwable) }
        }

        val body = getBody(model.headerItem.getValue(), User.PHOTO_UPLOAD_KEY)
        if (body != null) remote = remote.flatMap {
            api.uploadUserPhoto(model.id, body)
                    .map(getLocalUpdateFunction(model))
        }

        remote = remote.map(saveFunction)

        return updateCurrent(remote)
    }

    override fun get(id: String): Flowable<User> {
        var local = userDao.get(id).subscribeOn(io())
        var remote = api.me.map(saveFunction)

        if (id == currentUser.id) {
            local = updateCurrent(local.toSingle()).toMaybe()
            remote = updateCurrent(remote)
        }

        return fetchThenGetModel(local, remote.toMaybe())
    }

    override fun delete(model: User): Single<User> = api.deleteUser(model.id)
            .map { this.deleteLocally(it) }
            .flatMap { clearTables() }
            .map { model }
            .doOnError { throwable -> deleteInvalidModel(model, throwable) }

    override fun provideSaveManyFunction(): (List<User>) -> List<User> = { models ->
        userDao.upsert(models)
        models
    }

    fun signUp(firstName: String, lastName: String, primaryEmail: String, password: String): Single<User> {
        val newUser = User("", "", "", primaryEmail, firstName, lastName, "")
        newUser.setPassword(password)

        return createOrUpdate(newUser)
    }

    fun signIn(loginResult: LoginResult): Single<User> =
            updateCurrent(api.signIn(loginResult).map(saveFunction))

    fun signIn(email: String, password: String): Single<User> {
        val request = JsonObject()
        request.addProperty(PRIMARY_EMAIL, email)
        request.addProperty(PASSWORD, password)

        return updateCurrent(api.signIn(request).map(saveFunction))
    }

    fun signOut(): Single<Boolean> {
        val device = AppDatabase.getInstance().deviceDao().current

        return api.signOut(device.id)
                .flatMap { clearTables() }
                .onErrorResumeNext { clearTables() }
                .subscribeOn(io())
    }

    fun findUser(screenName: String): Single<List<User>> = api.findUser(screenName)

    fun forgotPassword(email: String): Single<Message> {
        val json = JsonObject()
        json.addProperty(PRIMARY_EMAIL, email)

        return api.forgotPassword(json)
    }

    fun resetPassword(email: String, token: String, password: String): Single<Message> {
        val json = JsonObject()
        json.addProperty(PRIMARY_EMAIL, email)
        json.addProperty(TOKEN, token)
        json.addProperty(PASSWORD, password)

        return api.resetPassword(json)
    }

    private fun clearUser(): Single<Boolean> {
        val userId = userId
        if (TextUtils.isEmpty(userId)) return just(false)

        app.getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .remove(USER_ID)
                .remove(SESSION_COOKIE) // Delete cookies when signing out
                .apply()

        return userDao.get(userId)
                .flatMapSingle { this.delete(it) }
                .flatMap {
                    currentUser = User.empty()
                    just(true)
                }
    }

    private fun saveUserId(user: User): User {
        app.getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(USER_ID, user.id).apply()
        return user
    }

    private fun clearTables(): Single<Boolean> {
        val database = AppDatabase.getInstance()
        return database.clearTables().flatMap { clearUser() }.onErrorReturn { false }
    }

    /**
     * Used to update changes to the current signed in user
     */
    @SuppressLint("CheckResult")
    private fun updateCurrent(source: Single<User>): Single<User> {
        val result = source.toObservable().publish()
                .autoConnect(2) // wait for this and the caller to subscribe
                .singleOrError()
                .map { this.saveUserId(it) }

        result.subscribe({ currentUser = it }, ErrorHandler.EMPTY::accept)
        return result
    }

    companion object {

        private const val PREFS = "prefs"
        private const val USER_ID = "user_id_key"
        private const val PRIMARY_EMAIL = "primaryEmail"
        private const val TOKEN = "token"
        private const val PASSWORD = "password"
    }
}
