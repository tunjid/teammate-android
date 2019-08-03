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
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.ConfigDao
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.TeammateException
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import kotlin.math.min

class ConfigRepo internal constructor() : ModelRepo<Config>() {

    private var numRefreshes = 0
    private var retryPeriod = 3

    private val api: TeammateApi = TeammateService.getApiInstance()
    private val dao: ConfigDao = AppDatabase.getInstance().configDao()

    val current: Config
        get() = dao.current

    override fun dao(): EntityDao<in Config> = dao

    override fun createOrUpdate(model: Config): Single<Config> =
            Single.error(TeammateException("Can't create config locally"))

    override fun get(id: String): Flowable<Config> {
        val config = dao.current
        return when {
            config.isEmpty -> api.config.map(saveFunction).toFlowable()
            else -> Flowable.just(config).doFinally(this::refreshConfig)
        }
    }

    override fun delete(model: Config): Single<Config> {
        dao.deleteCurrent()
        return Single.just(model)
    }

    override fun provideSaveManyFunction(): (List<Config>) -> List<Config> {
        return { devices: List<Config> ->
            dao.upsert(devices)
            devices
        }
    }

    @SuppressLint("CheckResult")
    private fun refreshConfig() {
        if (numRefreshes++ % REFRESH_THRESHOLD != 0) return
        api.config.map(saveFunction)
                .onErrorResumeNext(retryConfig()::invoke)
                .subscribe({ }, ErrorHandler.EMPTY::accept)
    }

    private fun retryConfig(): (Throwable) -> Single<Config> = {
        numRefreshes = 0
        retryPeriod *= retryPeriod
        retryPeriod = min(retryPeriod, 60)
        Completable.timer(retryPeriod.toLong(), TimeUnit.SECONDS)
                .andThen<Config>(api.config.map(saveFunction)
                        .onErrorResumeNext(retryConfig()::invoke))
    }

    companion object {

        private const val REFRESH_THRESHOLD = 10
    }
}
