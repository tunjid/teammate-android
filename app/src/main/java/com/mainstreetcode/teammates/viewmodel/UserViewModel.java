package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.repository.UserRepository;

import io.reactivex.Observable;

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

    public boolean isTeamAdmin(Team team) {
        return Role.isTeamAdmin(team, getCurrentUser());
    }

    public User getCurrentUser() {
        return repository.getCurrentUser();
    }

    public Observable<User> signUp(String firstName, String lastName, String primaryEmail, String password) {
        return repository.signUp(firstName, lastName, primaryEmail, password);
    }

    public Observable<User> signIn(String email, String password) {
        return repository.signIn(email, password);
    }

    public Observable<User> getMe() {
        return repository.getMe();
    }

    public Observable<Boolean> signOut() {
        return repository.signOut();
    }

    public Observable<Void> forgotPassword(String email) {
        return repository.forgotPassword(email);
    }
}
