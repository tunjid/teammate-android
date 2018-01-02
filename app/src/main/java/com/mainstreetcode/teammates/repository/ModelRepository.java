package com.mainstreetcode.teammates.repository;


import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import com.mainstreetcode.teammates.model.Message;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.persistence.EntityDao;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.HttpException;

import static io.reactivex.Maybe.concat;

/**
 * Repository that manages {@link Model} CRUD operations
 */

public abstract class ModelRepository<T extends Model<T>> {

    private final Function<List<T>, List<T>> saveListFunction = provideSaveManyFunction();
    private final Function<T, T> saveFunction = model -> saveListFunction.apply(Collections.singletonList(model)).get(0);

    public abstract EntityDao<? super T> dao();

    public abstract Single<T> createOrUpdate(T model);

    public abstract Flowable<T> get(String id);

    public abstract Single<T> delete(T model);

    abstract Function<List<T>, List<T>> provideSaveManyFunction();

    public final Flowable<T> get(T model) {
        AtomicInteger counter = new AtomicInteger(0);
        return model.isEmpty()
                ? Flowable.error(new IllegalArgumentException("Model does not exist"))
                : get(model.getId())
                .doOnNext(fetched -> counter.incrementAndGet())
                .map(getLocalUpdateFunction(model, () -> counter.get() == 2));
    }

    private Function<T, T> getLocalUpdateFunction(T original, Callable<Boolean> shouldReset) {
        return emitted -> {
            if (shouldReset.call()) original.reset();
            original.update(emitted);
            return original;
        };
    }

    final Function<T, T> getLocalUpdateFunction(T original) {
        return getLocalUpdateFunction(original, () -> true);
    }

    final Function<List<T>, List<T>> getSaveManyFunction() {
        return saveListFunction;
    }

    final Function<T, T> getSaveFunction() {
        return saveFunction;
    }

    final Flowable<T> fetchThenGetModel(Maybe<T> local, Maybe<T> remote) {
        AtomicReference<T> reference = new AtomicReference<>();
        local = local.doOnSuccess(reference::set);
        remote = remote.doOnError(throwable -> deleteInvalidModel(reference.get(), throwable));

        return fetchThenGet(local, remote);
    }

    final void deleteInvalidModel(T model, Throwable throwable) {
        if (model == null || !(throwable instanceof HttpException)) return;

        Message message = new Message((HttpException) throwable);
        if (message.isInvalidObject() || message.isIllegalTeamMember()) deleteLocally(model);
    }

    final T deleteLocally(T model) {
        dao().delete(model);
        return model;
    }

    @Nullable
    MultipartBody.Part getBody(String path, String photoKey) {
        File file = new File(path);

        if (!file.exists()) return null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        if (extension == null) return null;
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        if (type == null) return null;
        RequestBody requestBody = RequestBody.create(MediaType.parse(type), file);

        return MultipartBody.Part.createFormData(photoKey, file.getName(), requestBody);
    }

    static <R> Flowable<R> fetchThenGet(Maybe<R> local, Maybe<R> remote) {
        return concat(local, remote);
    }
}
