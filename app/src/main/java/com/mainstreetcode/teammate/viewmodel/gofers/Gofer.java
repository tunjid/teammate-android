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

package com.mainstreetcode.teammate.viewmodel.gofers;

import android.annotation.SuppressLint;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;

import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.ListableModel;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public abstract class Gofer<T extends Model<T> & ListableModel<T>> {

    protected final T model;
    private final Consumer<Throwable> onError;

    final List<Differentiable> items;

    Gofer(T model, Consumer<Throwable> onError) {
        this.model = model;
        this.onError = onError;
        items = new ArrayList<>();
    }

    public static String tag(String seed, Model model) {
        String uuid = model.isEmpty() ? UUID.randomUUID().toString() : model.getId();
        return seed + "-" + uuid;
    }

    @Nullable
    public abstract String getImageClickMessage(Fragment fragment);

    abstract Completable delete();

    abstract Flowable<Boolean> changeEmitter();

    abstract Single<DiffUtil.DiffResult> upsert();

    abstract Flowable<DiffUtil.DiffResult> fetch();

    public void clear() { }

    public final Completable remove() {
        return delete().doOnError(onError).observeOn(mainThread());
    }

    public Flowable<Object> watchForChange() {
        return changeEmitter().filter(changed -> changed).cast(Object.class).observeOn(mainThread());
    }

    public final Single<DiffUtil.DiffResult> save() {
        return upsert().doOnError(onError);
    }

    public final Flowable<DiffUtil.DiffResult> get() {
        return model.isEmpty() ? Flowable.empty() : fetch().doOnError(onError);
    }

    public final List<Differentiable> getItems() { return items; }

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void startPrep() { watchForChange().subscribe(ignored -> {}, ErrorHandler.EMPTY); }

    List<Differentiable> preserveItems(List<Differentiable> old, List<Differentiable> fetched) {
        ModelUtils.preserveAscending(old, fetched);
        return old;
    }
}
