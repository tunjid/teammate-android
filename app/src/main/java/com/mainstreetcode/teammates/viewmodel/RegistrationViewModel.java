package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mainstreetcode.teammates.model.User;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.subjects.ReplaySubject;

/**
 * View model for registration
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class RegistrationViewModel extends ViewModel {

    // Used to save values of last call
    private ReplaySubject<User> signUpSubject;
    private ReplaySubject<AuthResult> signInSubject;

    public Observable<User> signUp(String firstName, String lastName, String email, String password) {
        if (signUpSubject != null && signUpSubject.hasComplete() && !signUpSubject.hasThrowable()) {
            return Observable.just(signUpSubject.getValue());
        }

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .primaryEmail(email)
                .build();

        signUpSubject = ReplaySubject.createWithSize(1);
        Observable.create(new SignUpCall(user, password)).subscribe(signUpSubject);
        return signUpSubject;
    }

    public Observable<AuthResult> signIn(String email, String password) {
        if (signInSubject != null && signInSubject.hasComplete() && !signInSubject.hasThrowable()) {
            return Observable.just(signInSubject.getValue());
        }

        signInSubject = ReplaySubject.createWithSize(1);
        Observable.create(new SignInCall(email, password)).subscribe(signInSubject);
        return signInSubject;
    }

    public Observable<Void> forgotPassword(String email) {
        return Observable.create(new ForgotPasswordCall(email));
    }

    static class SignUpCall implements ObservableOnSubscribe<User> {

        private DatabaseReference userDb = FirebaseDatabase.getInstance()
                .getReference()
                .child(User.DB_NAME)
                .push();

        private final String password;
        private final User user;

        private SignUpCall(User user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        public void subscribe(final ObservableEmitter<User> emitter) throws Exception {

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.getPrimaryEmail(), password)
                    .addOnSuccessListener((fireBaseUser) -> userDb.setValue(user)
                            .addOnSuccessListener((Void) -> emitter.onNext(user))
                            .addOnFailureListener(emitter::onError))
                    .addOnFailureListener(emitter::onError);
        }
    }

    static class SignInCall implements ObservableOnSubscribe<AuthResult> {

        private final String email;
        private final String password;

        private SignInCall(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        public void subscribe(ObservableEmitter<AuthResult> emitter) throws Exception {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(emitter::onNext)
                    .addOnFailureListener(emitter::onError);
        }
    }

    static class ForgotPasswordCall implements ObservableOnSubscribe<Void> {

        private final String email;

        private ForgotPasswordCall(String email) {
            this.email = email;
        }

        @Override
        public void subscribe(ObservableEmitter<Void> emitter) throws Exception {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(emitter::onNext)
                    .addOnFailureListener(emitter::onError);
        }
    }
}
