package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;

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

    public Observable<Boolean> hasJoinRequest(User user, Team team) {
        return Observable.create(new JoinRequestCheckCall(user, team));
    }

    public Observable<Boolean> requestTeamJoin(JoinRequest joinRequest) {
        return Observable.create(new JoinRequestCall(joinRequest));
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

    static class JoinRequestCheckCall implements ObservableOnSubscribe<Boolean> {

        private final User user;
        private final Team team;

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child(JoinRequest.DB_NAME)
                .orderByChild(JoinRequest.USER_KEY);

        private JoinRequestCheckCall(User user, Team team) {
            this.user = user;
            this.team = team;
        }

        @Override
        public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
            query.equalTo(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot == null) return;
                            boolean hasTeam = false;

                            if (dataSnapshot.hasChildren()) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    if (JoinRequest.fromSnapshot(childSnapshot).getTeamId().equals(team.getUid())) {
                                        hasTeam = true;
                                        break;
                                    }
                                }
                            }
                            emitter.onNext(hasTeam);
                            emitter.onComplete();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            emitter.onError(databaseError.toException());
                        }
                    });
        }
    }

    static class JoinRequestCall implements ObservableOnSubscribe<Boolean> {

        private final JoinRequest joinRequest;

        DatabaseReference db = FirebaseDatabase.getInstance()
                .getReference()
                .child(JoinRequest.DB_NAME)
                .push();

        private JoinRequestCall(JoinRequest joinRequest) {
            this.joinRequest = joinRequest;
        }

        @Override
        public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
            db.setValue(joinRequest).addOnSuccessListener(Void -> {
                emitter.onNext(true);
                emitter.onComplete();
            }).addOnFailureListener(emitter::onError);
        }
    }
}
