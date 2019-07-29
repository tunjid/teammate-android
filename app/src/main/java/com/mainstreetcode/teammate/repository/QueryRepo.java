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

package com.mainstreetcode.teammate.repository;


import androidx.annotation.Nullable;

import com.mainstreetcode.teammate.model.Model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

abstract class QueryRepo<T extends Model<T>, S extends Model<S>, R> extends ModelRepo<T> {

    QueryRepo() {}

    public final Flowable<List<T>> modelsBefore(S key, @Nullable R pagination) {
        return fetchThenGet(localModelsBefore(key, pagination), remoteModelsBefore(key, pagination));
    }

    abstract Maybe<List<T>> localModelsBefore(S key, @Nullable R pagination);

    abstract Maybe<List<T>> remoteModelsBefore(S key, @Nullable R pagination);

    Date getFutureDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 100);
        return calendar.getTime();
    }
}
