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
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.ReplaySubject;

import static io.reactivex.Observable.concat;
import static io.reactivex.Observable.create;
import static io.reactivex.Observable.fromCallable;
import static io.reactivex.Observable.just;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

public class UserRepository {

    private static final int TIME_OUT = 4;
    private static final String PREFS = "prefs";
    private static final String EMAIL_KEY = "email_key";

    private static UserRepository ourInstance;

    private final Application application;
    private final TeammateApi teammateApi;
    private final UserDao userDao;

    private User currentUser;

    // Used to save values of last call
    private ReplaySubject<User> signUpSubject;
    private ReplaySubject<User> signInSubject;

    private final Consumer<User> currentUserUpdater = (updatedUser) -> currentUser = updatedUser;

    private UserRepository() {
        application = Application.getInstance();
        teammateApi = TeammateService.getApiInstance();
        userDao = AppDatabase.getInstance().userDao();
    }

    public static UserRepository getInstance() {
        if (ourInstance == null) ourInstance = new UserRepository();
        return ourInstance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Observable<User> signUp(String firstName, String lastName, String primaryEmail, String password) {

        if (signUpSubject != null && signUpSubject.hasComplete() && !signUpSubject.hasThrowable()) {
            return just(signUpSubject.getValue());
        }

        signUpSubject = ReplaySubject.createWithSize(1);
        User newUser = new User("*", firstName, lastName, primaryEmail);

        newUser.setPassword(password);
        teammateApi.signUp(newUser)
                .flatMap(this::saveUser)
                .subscribe(signUpSubject);

        return updateCurrent(signUpSubject);
    }

    public Observable<User> signIn(String email, String password) {
        if (signInSubject != null && signInSubject.hasComplete() && !signInSubject.hasThrowable()) {
            return just(signInSubject.getValue());
        }

        signInSubject = ReplaySubject.createWithSize(1);

        JsonObject request = new JsonObject();
        request.addProperty("primaryEmail", email);
        request.addProperty("password", password);

        teammateApi.signIn(request)
                .flatMap(this::saveUser)
                .subscribe(signInSubject);

        return updateCurrent(signInSubject);
    }

    public Observable<User> getMe() {
        String prmaryEmail = getPrimaryEmail();
        Observable<User> local = fromCallable(() -> userDao.findByEmail(prmaryEmail)).subscribeOn(io());
        Observable<User> remote = teammateApi.getMe().flatMap(this::saveUser);

        return updateCurrent(concat(local, remote));
    }

    public Observable<Boolean> signOut() {
        return teammateApi.signOut()
                .flatMap(result -> clearUser())
                .onErrorResumeNext(throwable -> {return clearUser();})
                .observeOn(mainThread());
    }

    Observable<User> saveUser(User user) {
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

    public Observable<Void> forgotPassword(String email) {
        return create(new ForgotPasswordCall(email)).timeout(TIME_OUT, TimeUnit.SECONDS);
    }

    private Observable<Boolean> clearUser() {
        String email = getPrimaryEmail();
        if (email == null) return just(false);

        application.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(EMAIL_KEY)
                .apply();

        User user = userDao.findByEmail(email);

        if (user == null) return just(false);

        userDao.delete(user);
        currentUser = null;

        return just(true);
    }

    /**
     * Used to update changes to the current signed in user
     */
    private Observable<User> updateCurrent(Observable<User> source) {
        Observable<User> result = source.publish()
                .autoConnect(2) // wait for this and the caller to subscribe
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

    public static class ForgotPasswordCall implements ObservableOnSubscribe<Void> {

        private final String email;

        private ForgotPasswordCall(String email) {
            this.email = email;
        }

        @Override
        public void subscribe(ObservableEmitter<Void> emitter) throws Exception {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener((Void) -> {
                        emitter.onNext(Void);
                        emitter.onComplete();
                    })
                    .addOnFailureListener(emitter::onError);
        }
    }
}
