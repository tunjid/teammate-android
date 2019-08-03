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

import com.mainstreetcode.teammate.model.Prefs
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.persistence.PrefsDao
import io.reactivex.Flowable
import io.reactivex.Single

class PrefsRepo internal constructor() : ModelRepo<Prefs>() {

    private val dao: PrefsDao = AppDatabase.getInstance().prefsDao()

    val current: Prefs
        get() = dao.current

    override fun dao(): EntityDao<in Prefs> = dao

    override fun createOrUpdate(model: Prefs): Single<Prefs> {
        dao.insert(listOf(model))
        return Single.just(model)
    }

    override fun get(id: String): Flowable<Prefs> {
        val config = dao.current
        return Flowable.just(config)
    }

    override fun delete(model: Prefs): Single<Prefs> {
        dao.deleteCurrent()
        return Single.just(model)
    }

    override fun provideSaveManyFunction(): (List<Prefs>) -> List<Prefs> = { devices: List<Prefs> ->
        dao.upsert(devices)
        devices
    }
}
