package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mainstreetcode.teammates.model.Role;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.subjects.BehaviorSubject;

/**
 * ViewModel for signed in team
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class RoleViewModel extends ViewModel {

    private BehaviorSubject<List<Role>> roleSubject = BehaviorSubject.create();

    public Observable<List<Role>> getRoles() {
        Observable.create(new RoleCall()).subscribe(roleSubject);
        return roleSubject;
    }

    static class RoleCall implements ObservableOnSubscribe<List<Role>> {

        private final Query teamQuery;

        private RoleCall() {
            teamQuery = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(Role.DB_NAME)
                    .limitToFirst(10);
        }

        @Override
        public void subscribe(ObservableEmitter<List<Role>> emitter) throws Exception {
            teamQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot == null) return;
                    List<Role> result = new ArrayList<>((int) dataSnapshot.getChildrenCount());

                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        result.add(new Role(childSnapshot.getKey(), childSnapshot));
                    }

                    emitter.onNext(result);

                    // Do not complete, as observers will onlsy see completion because of prefetch
                    //emitter.onComplete();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    emitter.onError(databaseError.toException());
                }
            });
        }
    }
}
