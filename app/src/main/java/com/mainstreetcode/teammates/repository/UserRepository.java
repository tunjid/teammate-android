package com.mainstreetcode.teammates.repository;


import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.mainstreetcode.teammates.Application;
import com.mainstreetcode.teammates.model.Device;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.UserDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.TeammateException;

import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import okhttp3.MultipartBody;

import static com.mainstreetcode.teammates.rest.TeammateService.SESSION_COOKIE;
import static io.reactivex.Single.just;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

public class UserRepository extends ModelRepository<User> {

    private static final String PREFS = "prefs";
    private static final String EMAIL_KEY = "email_key";

    private static UserRepository ourInstance;

    private final Application application;
    private final TeammateApi api;
    private final UserDao userDao;

    private User currentUser = User.empty();

    private final Consumer<User> currentUserUpdater = updatedUser -> currentUser = updatedUser;

    private UserRepository() {
        application = Application.getInstance();
        api = TeammateService.getApiInstance();
        userDao = AppDatabase.getInstance().userDao();
    }

    public static UserRepository getInstance() {
        if (ourInstance == null) ourInstance = new UserRepository();
        return ourInstance;
    }

    @Override
    public Single<User> createOrUpdate(User model) {
        Single<User> remote = model.isEmpty()
                ? api.signUp(model)
                : api.updateUser(model.getId(), model);


        MultipartBody.Part body = getBody(model.getHeaderItem().getValue(), User.PHOTO_UPLOAD_KEY);
        if (body != null) {
            remote = remote.flatMap(put -> api.uploadUserPhoto(model.getId(), body));
        }

        remote = remote.map(getSaveFunction());
        return updateCurrent(remote);
    }

    @Override
    public Flowable<User> get(String primaryEmail) {
        Maybe<User> local = userDao.findByEmail(primaryEmail).subscribeOn(io());
        Single<User> remote = api.getMe().map(getSaveFunction());

        return cacheThenRemote(updateCurrent(local.toSingle()).toMaybe(), updateCurrent(remote).toMaybe());
    }

    @Override
    public Single<User> delete(User model) {
        userDao.delete(model);
        return just(model);
    }

    @Override
    Function<List<User>, List<User>> provideSaveManyFunction() {
        return models -> {
            userDao.upsert(Collections.unmodifiableList(models));
            return models;
        };
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Single<User> signUp(String firstName, String lastName, String primaryEmail, String password) {
        User newUser = new User("", firstName, lastName, primaryEmail, "");
        newUser.setPassword(password);

        return createOrUpdate(newUser);
    }

    public Single<User> signIn(String email, String password) {
        JsonObject request = new JsonObject();
        request.addProperty("primaryEmail", email);
        request.addProperty("password", password);

        Single<User> remote = api.signIn(request).map(getSaveFunction());
        return updateCurrent(remote);
    }

    public Flowable<User> getMe() {
        String primaryEmail = getPrimaryEmail();
        return TextUtils.isEmpty(primaryEmail)
                ? Flowable.error(new TeammateException("No signed in user"))
                : get(primaryEmail);
    }

    public Single<Boolean> signOut() {
        AppDatabase database = AppDatabase.getInstance();
        Single<Boolean> local = database.clearTables().flatMap(result -> clearUser());
        Device device = database.deviceDao().getCurrentDevice();
        String deviceId = device != null ? device.getId() : "";

        return api.signOut(deviceId)
                .flatMap(result -> local)
                .onErrorResumeNext(throwable -> local)
                .subscribeOn(io())
                .observeOn(mainThread());
    }

    public boolean isSignedIn() {
        return getPrimaryEmail() != null;
    }

    @Nullable
    private String getPrimaryEmail() {
        return application.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(EMAIL_KEY, null);
    }

    public Single<Void> forgotPassword(String email) {
        // TODO Implement this
        return Single.error(new UnsupportedOperationException(email + "Not implemented"));
    }

    private Single<Boolean> clearUser() {
        String email = getPrimaryEmail();
        if (email == null) return just(false);

        application.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(EMAIL_KEY)
                .remove(SESSION_COOKIE) // Delete cookies when signing out
                .apply();

        return userDao.findByEmail(email)
                .flatMapSingle(this::delete)
                .flatMap(deleted -> {
                    currentUser = User.empty();
                    return just(true);
                });
    }

    /**
     * Used to update changes to the current signed in user
     */
    private Single<User> updateCurrent(Single<User> source) {
        Single<User> result = source.toObservable().publish()
                .autoConnect(2) // wait for this and the caller to subscribe
                .singleOrError()
                .map(user -> {
                    application.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                            .edit()
                            .putString(EMAIL_KEY, user.getPrimaryEmail())
                            .apply();
                    return user;
                })
                .observeOn(mainThread());

        result.subscribe(currentUserUpdater, throwable -> {});
        return result;
    }
}
