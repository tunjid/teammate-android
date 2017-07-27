package com.mainstreetcode.teammates.repository;


import android.content.Context;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonObject;
import com.mainstreetcode.teammates.Application;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.UserDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;

import static io.reactivex.Maybe.concat;
import static io.reactivex.Single.create;
import static io.reactivex.Single.just;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class UserRepository extends CrudRespository<User> {

    private static final int TIME_OUT = 4;
    private static final String PREFS = "prefs";
    private static final String EMAIL_KEY = "email_key";

    private static UserRepository ourInstance;

    private final Application application;
    private final TeammateApi teammateApi;
    private final UserDao userDao;

    private User currentUser;

    // Used to save values of last call
    private ReplaySubject<User> signInSubject;

    private final Consumer<User> currentUserUpdater = updatedUser -> currentUser = updatedUser;

    private UserRepository() {
        application = Application.getInstance();
        teammateApi = TeammateService.getApiInstance();
        userDao = AppDatabase.getInstance().userDao();
    }

    public static UserRepository getInstance() {
        if (ourInstance == null) ourInstance = new UserRepository();
        return ourInstance;
    }

    @Override
    public Single<User> createOrUpdate(User model) {
        ReplaySubject<User> signUpSubject;

        signUpSubject = ReplaySubject.createWithSize(1);

        teammateApi.signUp(model)
                .flatMap(this::save)
                .toObservable()
                .subscribe(signUpSubject);

        return updateCurrent(signUpSubject.singleOrError());
    }

    @Override
    public Flowable<User> get(String primaryEmail) {
        Maybe<User> local = userDao.findByEmail(primaryEmail).subscribeOn(Schedulers.io());
        Single<User> remote = teammateApi.getMe().flatMap(this::save);

        return concat(updateCurrent(local.toSingle()).toMaybe(), updateCurrent(remote).toMaybe()).observeOn(mainThread());
    }

    @Override
    public Single<User> delete(User model) {
        userDao.delete(model);
        return just(model);
    }

    @Override
    Single<List<User>> saveList(List<User> models) {
        return null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Single<User> signUp(String firstName, String lastName, String primaryEmail, String password) {
        User newUser = new User("*", firstName, lastName, primaryEmail, "");
        newUser.setPassword(password);

        return createOrUpdate(newUser);
    }

    public Single<User> signIn(String email, String password) {
        if (signInSubject != null && signInSubject.hasComplete() && !signInSubject.hasThrowable()) {
            return just(signInSubject.getValue());
        }

        signInSubject = ReplaySubject.createWithSize(1);

        JsonObject request = new JsonObject();
        request.addProperty("primaryEmail", email);
        request.addProperty("password", password);

        teammateApi.signIn(request)
                .flatMap(this::save)
                .toObservable()
                .subscribe(signInSubject);

        return updateCurrent(signInSubject.singleOrError());
    }

    public Flowable<User> getMe() {
        return get(getPrimaryEmail());
    }

    public Single<Boolean> signOut() {
        return teammateApi.signOut()
                .flatMap(result -> clearUser())
                .onErrorResumeNext(throwable -> clearUser())
                .observeOn(mainThread());
    }

    @Override
    Single<User> save(User user) {
        userDao.insert(Collections.singletonList(user));
        return just(user);
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
        return create(new ForgotPasswordCall(email)).timeout(TIME_OUT, TimeUnit.SECONDS);
    }

    private Single<Boolean> clearUser() {
        String email = getPrimaryEmail();
        if (email == null) return just(false);

        application.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(EMAIL_KEY)
                .apply();

        return userDao.findByEmail(email)
                .flatMapSingle(this::delete)
                .flatMap(deleted -> {
                    currentUser = null;
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
                .flatMap(user -> {
                    application.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                            .edit()
                            .putString(EMAIL_KEY, user.getPrimaryEmail())
                            .apply();
                    return just(user);
                })
                .observeOn(mainThread());

        result.subscribe(currentUserUpdater, (throwable -> {}));
        return result;
    }

    public static class ForgotPasswordCall implements SingleOnSubscribe<Void> {

        private final String email;

        private ForgotPasswordCall(String email) {
            this.email = email;
        }

        @Override
        public void subscribe(SingleEmitter<Void> emitter) throws Exception {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(emitter::onSuccess)
                    .addOnFailureListener(emitter::onError);
        }
    }
}
