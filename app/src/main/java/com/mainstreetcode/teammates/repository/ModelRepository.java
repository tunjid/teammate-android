package com.mainstreetcode.teammates.repository;


import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import com.mainstreetcode.teammates.model.Message;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.persistence.EntityDao;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
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
        if (model.isEmpty()) {
            return Flowable.error(new IllegalArgumentException("Model does not exist"));
        }
        return get(model.getId()).map(localMapper(model));
    }

    final Function<List<T>, List<T>> getSaveManyFunction() {
        return saveListFunction;
    }

    final Function<T, T> getSaveFunction() {
        return saveFunction;
    }

    final Function<T, T> localMapper(T original) {
        return emitted -> {
            original.update(emitted);
            return original;
        };
    }

    final Flowable<T> fetchThenGetModel(Maybe<T> local, Maybe<T> remote) {
        AtomicReference<T> reference = new AtomicReference<>();
        local = local.doOnSuccess(reference::set);
        remote = remote.doOnError(throwable -> deleteInvalidModel(reference.get(), throwable));

        return fetchThenGet(local, remote);
    }

    static <R> Flowable<R> fetchThenGet(Maybe<R> local, Maybe<R> remote) {
        return concat(local, remote);
    }

    final void deleteInvalidModel(T model, Throwable throwable) {
        if (model == null || !(throwable instanceof HttpException)) return;

        Message message = new Message((HttpException) throwable);
        if (message.isInvalidObject()) deleteLocally(model);
    }

    final void deleteLocally(T model) {
        Completable.fromRunnable(() -> dao().delete(model))
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {}, ErrorHandler.EMPTY);
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
}
