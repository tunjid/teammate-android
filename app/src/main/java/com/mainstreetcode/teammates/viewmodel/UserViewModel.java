package com.mainstreetcode.teammates.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonObject;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.UserDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;

/**
 * View model for registration
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class UserViewModel extends AndroidViewModel {

    private static final int TIME_OUT = 4;
    private static final String PREFS = "prefs";
    private static final String EMAIL_KEY = "email_key";

    private final UserDao userDao;
    private final TeammateApi teammateApi;

    // Used to save values of last call
    private ReplaySubject<User> signUpSubject;
    private ReplaySubject<User> signInSubject;

    public UserViewModel(Application application) {
        super(application);
        teammateApi = TeammateService.getApiInstance();
        userDao = AppDatabase.getInstance(application).userDao();
    }

    public Observable<User> signUp(String firstName, String lastName, String email, String password) {

        User newUser = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .primaryEmail(email)
                .password(password)
                .build();

        if (signUpSubject != null && signUpSubject.hasComplete() && !signUpSubject.hasThrowable()) {
            return Observable.just(signUpSubject.getValue());
        }

        signUpSubject = ReplaySubject.createWithSize(1);

        teammateApi.signUp(newUser)
                .flatMap(this::saveUser)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(signUpSubject);

        return signUpSubject;
    }

    public Observable<User> signIn(String email, String password) {
        if (signInSubject != null && signInSubject.hasComplete() && !signInSubject.hasThrowable()) {
            return Observable.just(signInSubject.getValue());
        }

        signInSubject = ReplaySubject.createWithSize(1);

        JsonObject request = new JsonObject();
        request.addProperty("primaryEmail", email);
        request.addProperty("password", password);

        teammateApi.signIn(request)
                .flatMap(this::saveUser)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(signInSubject);

        return signInSubject;
    }

    public Observable<Boolean> signOut() {
        return teammateApi.signOut()
                .flatMap((result) -> {
                    String email = getPrimaryEmail();
                    if (email == null) return Observable.just(false);

                    User user = userDao.findByEmail(email);
                    userDao.delete(user);

                    getApplication().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                            .edit()
                            .remove(EMAIL_KEY)
                            .apply();

                    return Observable.just(true);
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<User> getUser() {
        return Observable.defer(() -> Observable.just(userDao.findByEmail(getPrimaryEmail())))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<User> saveUser(User user) {
        getApplication().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(EMAIL_KEY, user.getPrimaryEmail())
                .apply();

        userDao.insert(user);
        return Observable.just(user);
    }

    @Nullable
    public String getPrimaryEmail() {
        return getApplication().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(EMAIL_KEY, null);
    }

    public Observable<Void> forgotPassword(String email) {
        return Observable.create(new ForgotPasswordCall(email)).timeout(TIME_OUT, TimeUnit.SECONDS);
    }

    static class ForgotPasswordCall implements ObservableOnSubscribe<Void> {

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
