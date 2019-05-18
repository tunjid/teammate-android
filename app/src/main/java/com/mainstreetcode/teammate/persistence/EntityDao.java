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

package com.mainstreetcode.teammate.persistence;

import androidx.room.Delete;

import java.util.List;

import io.reactivex.Single;


public abstract class EntityDao<T> {

    protected abstract String getTableName();

    public abstract void insert(List<T> models);

    protected abstract void update(List<T> models);

    @Delete
    public abstract void delete(T model);

    @Delete
    public abstract void delete(List<T> models);

    public void upsert(List<T> models) {
        insert(models);
        update(models);
    }

    Single<Integer> deleteAll() {
        final String sql = "DELETE FROM " + getTableName();
        return Single.fromCallable(() -> AppDatabase.getInstance().compileStatement(sql).executeUpdateDelete());
    }

    public static <T> EntityDao<T> daDont(){
        return new EntityDao<T>() {
            @Override
            protected String getTableName() { return ""; }

            @Override
            public void insert(List<T> models) {}

            @Override
            protected void update(List<T> models) {}

            @Override
            public void delete(T model) {}

            @Override
            public void delete(List<T> models) {}
        };
    }
}
