package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.repository.UserRepository;

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * View model for registration
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class UserViewModel extends ViewModel {

    private final UserRepository repository;

    public UserViewModel() {
        repository = UserRepository.getInstance();
    }

    public boolean isSignedIn() {
        return repository.isSignedIn();
    }

    public User getCurrentUser() {
        return repository.getCurrentUser();
    }

    public Single<User> signUp(String firstName, String lastName, String primaryEmail, String password) {
        return repository.signUp(firstName, lastName, primaryEmail, password);
    }

    public Single<User> signIn(String email, String password) {
        return repository.signIn(email, password);
    }

    public Flowable<User> getMe() {
        return repository.getMe();
    }

    public Single<Boolean> signOut() {
        return repository.signOut();
    }

    public Single<Void> forgotPassword(String email) {
        return repository.forgotPassword(email);
    }
}
