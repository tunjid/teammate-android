package com.mainstreetcode.teammate.repository;


import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.facebook.login.LoginResult;
import com.google.gson.JsonObject;
import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.model.Device;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.UserDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import okhttp3.MultipartBody;

import static android.content.Context.MODE_PRIVATE;
import static com.mainstreetcode.teammate.rest.TeammateService.SESSION_COOKIE;
import static io.reactivex.Single.just;
import static io.reactivex.schedulers.Schedulers.io;

public class UserRepository extends ModelRepository<User> {

    private static final String PREFS = "prefs";
    private static final String USER_ID = "user_id_key";
    private static final String PRIMARY_EMAIL = "primaryEmail";
    private static final String TOKEN = "token";
    private static final String PASSWORD = "password";

    private static UserRepository ourInstance;

    private final App app;
    private final TeammateApi api;
    private final UserDao userDao;

    private User currentUser = User.empty();

    private final Consumer<User> currentUserUpdater = updatedUser -> currentUser = updatedUser;

    private UserRepository() {
        app = App.getInstance();
        api = TeammateService.getApiInstance();
        userDao = AppDatabase.getInstance().userDao();
    }

    public static UserRepository getInstance() {
        if (ourInstance == null) ourInstance = new UserRepository();
        return ourInstance;
    }

    @Override
    public EntityDao<? super User> dao() {
        return userDao;
    }

    @Override
    public Single<User> createOrUpdate(User model) {
        Single<User> remote = model.isEmpty()
                ? api.signUp(model).map(getLocalUpdateFunction(model))
                : api.updateUser(model.getId(), model).map(getLocalUpdateFunction(model))
                .doOnError(throwable -> deleteInvalidModel(model, throwable));

        MultipartBody.Part body = getBody(model.getHeaderItem().getValue(), User.PHOTO_UPLOAD_KEY);
        if (body != null) {
            remote = remote.flatMap(put -> api.uploadUserPhoto(model.getId(), body).map(getLocalUpdateFunction(model)));
        }

        remote = remote.map(getSaveFunction());
        return updateCurrent(remote);
    }

    @Override
    public Flowable<User> get(String id) {
        Maybe<User> local = userDao.get(id).subscribeOn(io());
        Single<User> remote = api.getMe().map(getSaveFunction());

        if (id.equals(currentUser.getId())) {
            local = updateCurrent(local.toSingle()).toMaybe();
            remote = updateCurrent(remote);
        }

        return fetchThenGetModel(local, remote.toMaybe());
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
        User newUser = new User("", "", "", primaryEmail, firstName, lastName, "");
        newUser.setPassword(password);

        return createOrUpdate(newUser);
    }

    public Single<User> signIn(LoginResult loginResult) {
        return updateCurrent(api.signIn(loginResult).map(getSaveFunction()));
    }

    public Single<User> signIn(String email, String password) {
        JsonObject request = new JsonObject();
        request.addProperty(PRIMARY_EMAIL, email);
        request.addProperty(PASSWORD, password);

        return updateCurrent(api.signIn(request).map(getSaveFunction()));
    }

    public Flowable<User> getMe() {
        String userId = getUserId();
        return TextUtils.isEmpty(userId)
                ? Flowable.error(new TeammateException("No signed in user"))
                : get(userId).map(getLocalUpdateFunction(currentUser));
    }

    public Single<Boolean> signOut() {
        AppDatabase database = AppDatabase.getInstance();
        Single<Boolean> local = database.clearTables().flatMap(result -> clearUser());
        Device device = database.deviceDao().getCurrent();
        String deviceId = device != null ? device.getId() : "";

        return api.signOut(deviceId)
                .flatMap(result -> local)
                .onErrorResumeNext(throwable -> local)
                .subscribeOn(io());
    }

    public boolean isSignedIn() {
        return getUserId() != null;
    }

    @Nullable
    private String getUserId() {
        return app.getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(USER_ID, null);
    }

    public Single<Message> forgotPassword(String email) {
        JsonObject json = new JsonObject();
        json.addProperty(PRIMARY_EMAIL, email);

        return api.forgotPassword(json);
    }

    public Single<Message> resetPassword(String email, String token, String password) {
        JsonObject json = new JsonObject();
        json.addProperty(PRIMARY_EMAIL, email);
        json.addProperty(TOKEN, token);
        json.addProperty(PASSWORD, password);

        return api.resetPassword(json);
    }

    private Single<Boolean> clearUser() {
        String userId = getUserId();
        if (userId == null) return just(false);

        app.getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .remove(USER_ID)
                .remove(SESSION_COOKIE) // Delete cookies when signing out
                .apply();

        return userDao.get(userId)
                .flatMapSingle(this::delete)
                .flatMap(deleted -> {
                    currentUser = User.empty();
                    return just(true);
                });
    }

    @NonNull
    private User saveUserId(User user) {
        app.getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(USER_ID, user.getId()).apply();
        return user;
    }

    /**
     * Used to update changes to the current signed in user
     */
    @SuppressLint("CheckResult")
    private Single<User> updateCurrent(Single<User> source) {
        Single<User> result = source.toObservable().publish()
                .autoConnect(2) // wait for this and the caller to subscribe
                .singleOrError()
                .map(this::saveUserId);

        result.subscribe(currentUserUpdater, ErrorHandler.EMPTY);
        return result;
    }
}
