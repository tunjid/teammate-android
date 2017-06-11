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
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * ViewModel for signed in team
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class TeamViewModel extends ViewModel {

    private static final int TIMEOUT = 4;

    public Observable<List<Team>> findTeams(String queryText) {
        return Observable.create(new TeamCall(queryText.toLowerCase()));
    }

    public Observable<Boolean> joinTeam(User user, Team team) {
        return Observable.create(new JoinValidationCall(user, team))
                .flatMap(success -> success
                        ? Observable.create(new JoinRequestCall(user, team))
                        : Observable.just(false))
                .timeout(TIMEOUT, TimeUnit.SECONDS);
    }

    public Observable<Team> createTeam(User user, Team team){
        return Observable.create(new CreateTeamCall(user, team));
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
                        result.add(Team.fromSnapshot(childSnapshot.getKey(), childSnapshot));
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

    static class JoinValidationCall implements ObservableOnSubscribe<Boolean> {

        private final User user;
        private final Team team;

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child(JoinRequest.DB_NAME)
                .orderByChild(JoinRequest.USER_KEY);

        private JoinValidationCall(User user, Team team) {
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
                            boolean canJoinTeam = true;

                            if (dataSnapshot.hasChildren()) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    if (JoinRequest.fromSnapshot(childSnapshot).getTeamId().equals(team.getUid())) {
                                        canJoinTeam = false;
                                        break;
                                    }
                                }
                            }
                            emitter.onNext(canJoinTeam);
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

        private JoinRequestCall(User user, Team team) {
            this.joinRequest = JoinRequest.builder()
                    .isTeamApproved(false)
                    .isMemberApproved(true)
                    .memberId(user.getUid())
                    .teamId(team.getUid())
                    .roleId(team.get(7).getValue())
                    .build();
        }

        @Override
        public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
            db.setValue(joinRequest).addOnSuccessListener(Void -> {
                emitter.onNext(true);
                emitter.onComplete();
            }).addOnFailureListener(emitter::onError);
        }
    }

    static class CreateTeamCall implements ObservableOnSubscribe<Team> {

        private final Team team;

        DatabaseReference db = FirebaseDatabase.getInstance()
                .getReference()
                .child(Team.DB_NAME)
                .push();

        private CreateTeamCall(User user, Team source) {
            this.team = source.toSource();
            team.setUid(db.getKey());
            team.getMemberIds().add(user.getUid());
        }

        @Override
        public void subscribe(ObservableEmitter<Team> emitter) throws Exception {
            db.setValue(team.toMap()).addOnSuccessListener(Void -> {
                emitter.onNext(team);
                emitter.onComplete();
            }).addOnFailureListener(emitter::onError);
        }
    }
}
