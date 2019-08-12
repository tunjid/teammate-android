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

import android.text.TextUtils.isEmpty
import com.mainstreetcode.teammate.model.Device
import com.mainstreetcode.teammate.model.toMessage
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.DeviceDao
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.util.TeammateException
import io.reactivex.Flowable
import io.reactivex.Single

class DeviceRepo internal constructor() : ModelRepo<Device>() {

    private val api: TeammateApi = TeammateService.getApiInstance()
    private val dao: DeviceDao = AppDatabase.instance.deviceDao()

    override fun dao(): EntityDao<in Device> = dao

    override fun createOrUpdate(model: Device): Single<Device> {
        val current = dao.current
        val saveFunction = { device: Device -> dao.upsert(listOf(device)) }

        return when {
            isEmpty(model.fcmToken) -> Single.error(TeammateException("No token"))
            current.isEmpty -> api.createDevice(model).doOnSuccess(saveFunction)
            else -> api.updateDevice(current.setFcmToken(model.fcmToken).id, model)
                    .doOnSuccess(saveFunction)
                    .doOnError { throwable -> deleteInvalidModel(current, throwable) }
                    .onErrorResumeNext { throwable ->
                        val message = throwable.toMessage()
                        when {
                            message != null && message.isInvalidObject -> api.createDevice(current).doOnSuccess(saveFunction)
                            else -> Single.error(throwable)
                        }
                    }
        }
    }

    override fun get(id: String): Flowable<Device> = dao.current.let {
        when {
            it.isEmpty -> Flowable.error(TeammateException("No stored device"))
            else -> Flowable.just(it)
        }
    }


    override fun delete(model: Device): Single<Device> {
        dao.deleteCurrent()
        return Single.just(model)
    }

    override fun provideSaveManyFunction(): (List<Device>) -> List<Device> {
        return { devices: List<Device> ->
            dao.upsert(devices)
            devices
        }
    }
}
