package com.mainstreetcode.teammates.repository;


import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import com.mainstreetcode.teammates.model.BaseModel;

import java.io.File;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static io.reactivex.Maybe.concat;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

/**
 * Repository that manages {@link com.mainstreetcode.teammates.model.Model} CRUD operations
 */

public abstract class ModelRespository<T extends BaseModel<T>> {

    private final Function<List<T>, List<T>> saveListFunction = provideSaveManyFunction();
    private final Function<T, T> saveFunction = model -> saveListFunction.apply(Collections.singletonList(model)).get(0);

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

    public Predicate<T> getNotificationFilter() {
        return t -> true;
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

    public final void save(T model) {
        Completable.fromCallable(() -> {
            try {saveFunction.apply(model);}
            catch (Exception e) {e.printStackTrace();}
            return null;
        }).subscribeOn(io());
    }

    static <R> Flowable<R> cacheThenRemote(Maybe<R> local, Maybe<R> remote) {
        return concat(local, remote).observeOn(mainThread(), true);
    }

    @Nullable
    static MultipartBody.Part getBody(String path, String photokey) {
        File file = new File(path);

        if (!file.exists()) return null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        if (extension == null) return null;
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        if (type == null) return null;
        RequestBody requestBody = RequestBody.create(MediaType.parse(type), file);

        return MultipartBody.Part.createFormData(photokey, file.getName(), requestBody);
    }
}
