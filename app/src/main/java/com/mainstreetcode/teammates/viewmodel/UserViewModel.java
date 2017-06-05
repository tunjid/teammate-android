package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mainstreetcode.teammates.model.User;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.subjects.BehaviorSubject;

/**
 * ViewModel for signed in user
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class UserViewModel extends ViewModel {

    private BehaviorSubject<User> userSubject = BehaviorSubject.create();

    public Observable<User> getUser(String email) {
        Observable.create(new UserCall(email)).subscribe(userSubject);
        return userSubject;
    }

    static class UserCall implements ObservableOnSubscribe<User> {

        private final String email;

        Query userQuery = FirebaseDatabase.getInstance()
                .getReference()
                .child(User.DB_NAME)
                .orderByChild(User.PRIMARY_EMAIL_KEY);

        private UserCall(String email) {
            this.email = email;
        }

        @Override
        public void subscribe(ObservableEmitter<User> emitter) throws Exception {
            userQuery.equalTo(email)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot == null) return;
                            emitter.onNext(User.fromSnapshot(dataSnapshot));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            emitter.onError(databaseError.toException());
                        }
                    });
        }
    }
}
