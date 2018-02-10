package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.facebook.login.LoginResult;
import com.mainstreetcode.teammates.model.Message;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.repository.UserRepository;

import io.reactivex.Flowable;
import io.reactivex.Single;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * View model for User and Auth
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
        return repository.signUp(firstName, lastName, primaryEmail, password).observeOn(mainThread());
    }

    public Single<User> signIn(LoginResult loginResult) {
        return repository.signIn(loginResult).observeOn(mainThread());
    }

    public Single<User> signIn(String email, String password) {
        return repository.signIn(email, password).observeOn(mainThread());
    }

    public Single<User> updateUser(User user) {
        return repository.createOrUpdate(user).observeOn(mainThread());
    }

    public Flowable<User> getMe() {
        return repository.getMe().observeOn(mainThread());
    }

    public Single<Boolean> signOut() {
        TeamViewModel.teams.clear();
        return repository.signOut().observeOn(mainThread());
    }

    public Single<Message> forgotPassword(String email) {
        return repository.forgotPassword(email).observeOn(mainThread());
    }

    public Single<Message> resetPassword(String email, String token, String password) {
        return repository.resetPassword(email, token, password).observeOn(mainThread());
    }
}
