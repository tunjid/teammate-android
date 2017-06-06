package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mainstreetcode.teammates.model.Team;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * ViewModel for signed in team
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class TeamViewModel extends ViewModel {

    public Observable<List<Team>> getTeam(String queryText) {
        return Observable.create(new TeamCall(queryText));
    }

    static class TeamCall implements ObservableOnSubscribe<List<Team>> {

        private final Query teamQuery;

        private TeamCall(String queryText) {
            teamQuery = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(Team.DB_NAME)
                    .orderByChild(Team.SEARCH_INDEX_KEY)
                    .startAt(queryText)
                    .endAt(queryText + "\uf8ff")
                    .limitToFirst(10);
        }

        @Override
        public void subscribe(ObservableEmitter<List<Team>> emitter) throws Exception {
            teamQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot == null) return;
                    List<Team> result = new ArrayList<>((int) dataSnapshot.getChildrenCount());

                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        result.add(new Team(childSnapshot.getKey(), childSnapshot));
                    }

                    emitter.onNext(result);
                    emitter.onComplete();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    emitter.onError(databaseError.toException());
                }
            });
        }
    }
}
