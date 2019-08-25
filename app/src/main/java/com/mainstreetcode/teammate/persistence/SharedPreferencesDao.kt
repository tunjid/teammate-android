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

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Dao
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.model.Device
import io.reactivex.Single

/**
 * DAO for [Device]
 */

@Dao
abstract class SharedPreferencesDao<T> : EntityDao<T>() {

    override val tableName: String
        get() = preferenceName()

    internal abstract val empty: T

    val current: T
        get() {
            val preferences = preferences
            val serialized = preferences.getString(KEY, "")

            return if (serialized.isNullOrBlank()) empty else from(serialized)
        }

    private val preferences: SharedPreferences
        get() = App.instance.getSharedPreferences(tableName, Context.MODE_PRIVATE)

    fun deleteCurrent() = preferences.edit().remove(KEY).apply()

    override fun insert(models: List<T>) {
        if (models.isEmpty() || models.size > 1) return

        val device = models[0]
        preferences.edit().putString(KEY, to(device)).apply()
    }

    override fun delete(model: T) = deleteCurrent()

    override fun update(models: List<T>) = insert(models)

    override fun delete(models: List<T>) = deleteCurrent()

    override fun deleteAll(): Single<Int> = Single.fromCallable {
        deleteCurrent()
        0
    }

    internal abstract fun preferenceName(): String

    internal abstract fun to(item: T): String

    internal abstract fun from(serialized: String): T

    companion object {

        private const val KEY = "SharedPreferencesDao"
    }
}
