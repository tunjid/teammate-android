/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.viewmodel;

import androidx.lifecycle.ViewModel;

import com.facebook.login.LoginResult;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.repository.UserRepo;
import com.mainstreetcode.teammate.util.InstantSearch;
import com.mainstreetcode.teammate.util.TeammateException;
import com.mainstreetcode.teammate.viewmodel.gofers.UserGofer;

import io.reactivex.Flowable;
import io.reactivex.Single;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * View model for User and Auth
 */

public class UserViewModel extends ViewModel {

    private final UserRepo repository;

    public UserViewModel() {
        repository = RepoProvider.forRepo(UserRepo.class);
    }

    public boolean isSignedIn() {
        return repository.isSignedIn();
    }

    public User getCurrentUser() {
        return repository.getCurrentUser();
    }

    public UserGofer gofer(User user) {
        return new UserGofer(user, getCurrentUser()::equals, this::getUser, this::updateUser);
    }

    public InstantSearch<String, User> instantSearch() {
        return new InstantSearch<>(repository::findUser);
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

    public Single<User> deleteAccount() {
        TeamViewModel.teams.clear();
        return repository.delete(getCurrentUser()).observeOn(mainThread());
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

    private Single<User> updateUser(User user) {
        return getCurrentUser().equals(user) ? repository.createOrUpdate(user) : Single.error(new TeammateException(""));
    }

    private Flowable<User> getUser(User user) {
        return getCurrentUser().equals(user) ? repository.get(user) : Flowable.empty();
    }
}
