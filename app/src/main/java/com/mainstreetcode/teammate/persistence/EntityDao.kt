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

import androidx.room.Delete

import io.reactivex.Single


abstract class EntityDao<T> {

    abstract val tableName: String

    abstract fun insert(models: List<T>)

    internal abstract fun update(models: List<T>)

    @Delete
    abstract fun delete(model: T)

    @Delete
    abstract fun delete(models: List<T>)

    fun upsert(models: List<T>) {
        insert(models)
        update(models)
    }

    internal open fun deleteAll(): Single<Int> {
        val sql = "DELETE FROM $tableName"
        return Single.fromCallable { AppDatabase.instance.compileStatement(sql).executeUpdateDelete() }
    }

    companion object {

        fun <T> daDont(): EntityDao<T> {
            return object : EntityDao<T>() {
                override val tableName: String = ""

                override fun insert(models: List<T>) = Unit

                override fun update(models: List<T>) = Unit

                override fun delete(model: T) = Unit

                override fun delete(models: List<T>) = Unit
            }
        }
    }
}
