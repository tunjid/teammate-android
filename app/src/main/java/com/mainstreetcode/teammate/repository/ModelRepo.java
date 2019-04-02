package com.mainstreetcode.teammate.repository;


import android.annotation.SuppressLint;
import android.webkit.MimeTypeMap;

import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.util.ErrorHandler;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.Nullable;
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

import static io.reactivex.Maybe.concatDelayError;

/**
 * Repository that manages {@link Model} CRUD operations
 */

public abstract class ModelRepo<T extends Model<T>> {

    static final int DEF_QUERY_LIMIT = 12;

    private final Function<List<T>, List<T>> saveListFunction = provideSaveManyFunction();
    private final Function<T, T> saveFunction = model -> saveListFunction.apply(Collections.singletonList(model)).get(0);

    public abstract EntityDao<? super T> dao();

    public abstract Single<T> createOrUpdate(T model);

    public abstract Flowable<T> get(String id);

    public abstract Single<T> delete(T model);

    abstract Function<List<T>, List<T>> provideSaveManyFunction();

    public final Flowable<T> get(T model) {
        return model.isEmpty()
                ? Flowable.error(new IllegalArgumentException("Model does not exist"))
                : get(model.getId()).map(getLocalUpdateFunction(model));
    }

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public final void queueForLocalDeletion(T model) {
        Completable.fromRunnable(() -> deleteLocally(model))
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {}, ErrorHandler.EMPTY);
    }

    final Function<T, T> getLocalUpdateFunction(T original) {
        return emitted -> {
            original.update(emitted);
            return original;
        };
    }

    final Function<List<T>, List<T>> getSaveManyFunction() {
        return saveListFunction;
    }

    final Function<T, T> getSaveFunction() {
        return saveFunction;
    }

    final Function<List<T>, List<T>> saveAsNested() {
        return models -> {
            if (models.isEmpty()) return models;

            T litmus = models.get(0);
            if (litmus.hasMajorFields()) return saveListFunction.apply(models);

            dao().insert(Collections.unmodifiableList(models));
            return models;
        };
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

    T deleteLocally(T model) {
        dao().delete(model);
        return model;
    }

    @Nullable
    MultipartBody.Part getBody(CharSequence path, String photoKey) {
        String pathString = path.toString();
        File file = new File(pathString);

        if (!file.exists()) return null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(pathString);

        if (extension == null) return null;
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        if (type == null) return null;
        RequestBody requestBody = RequestBody.create(MediaType.parse(type), file);

        return MultipartBody.Part.createFormData(photoKey, file.getName(), requestBody);
    }

    static <R> Flowable<R> fetchThenGet(Maybe<R> local, Maybe<R> remote) {
        return concatDelayError(Arrays.asList(local, remote));
    }

}
