package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamDetailAdapter;
import com.mainstreetcode.teammates.baseclasses.RegistrationActivityFragment;
import com.mainstreetcode.teammates.model.Team;

/**
 * Splash screen
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public class TeamDetailFragment extends RegistrationActivityFragment
        implements
        View.OnClickListener {

    private static final String ARG_TEAM = "team";
    private static final String ARG_EDITABLE = "editable";

    private Team team;

    public static TeamDetailFragment newInstance(Team team, boolean isEditable) {
        TeamDetailFragment fragment = new TeamDetailFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
        args.putBoolean(ARG_EDITABLE, isEditable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = getArguments().getParcelable(ARG_TEAM);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team_detail, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.team_detail);

        boolean isEditable = getArguments().getBoolean(ARG_EDITABLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TeamDetailAdapter(team, isEditable, (team) -> {
        }));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                toggleFab(dy < 0);
            }
        });

        recyclerView.requestFocus();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = getFab();
        fab.setImageResource(R.drawable.ic_check_white_24dp);
        fab.setOnClickListener(this);

        toggleFab(true);
        setToolbarTitle(getString(getArguments().getBoolean(ARG_EDITABLE, false)
                ? R.string.create_team
                : R.string.join_team));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                break;
        }
    }

//    @Override
//    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
//        if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
//                && (event.getAction() == KeyEvent.ACTION_DOWN))) {
//            signUp();
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public void onComplete(@NonNull Task<AuthResult> task) {
//        if (getView() == null) return;
//        if (task.isSuccessful()) {
//            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
//                    .setDisplayName(nameInput.getText().toString())
//                    .build();
//            task.getResult().getUser().updateProfile(request)
//                    .addOnCompleteListener(success -> RegistrationActivity.startMainActivity(getActivity()))
//                    .addOnFailureListener(fail -> Snackbar.make(cityInput, R.string.sign_up_error_default, Snackbar.LENGTH_LONG).show());
//        }
//        else {
//            Exception exception = task.getException();
//            @StringRes int errorMessage;
//            if (exception instanceof FirebaseAuthWeakPasswordException) {
//                errorMessage = R.string.sign_up_error_weak_password;
//            }
//            else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
//                errorMessage = R.string.sign_up_error_invalid_credentials;
//            }
//            else if (exception instanceof FirebaseAuthUserCollisionException) {
//                errorMessage = R.string.sign_up_error_duplicate_credentials;
//            }
//            else {
//                errorMessage = R.string.sign_up_error_default;
//            }
//            Snackbar.make(cityInput, errorMessage, Snackbar.LENGTH_LONG).show();
//        }
//    }
//
//    private void signUp() {
//        if (validator.isValidName(nameInput)
//                && validator.isValidEmail(cityInput)
//                && validator.isValidPassword(stateInput)) {
//            FirebaseAuth auth = FirebaseAuth.getInstance();
//            auth.createUserWithEmailAndPassword(cityInput.getText().toString(),
//                    stateInput.getText().toString())
//                    .addOnCompleteListener(this);
//        }
//    }

}
