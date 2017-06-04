package com.mainstreetcode.teammates.fragments.registration;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.activities.RegistrationActivity;
import com.mainstreetcode.teammates.baseclasses.RegistrationActivityFragment;

/**
 * Splash screen
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public class SignUpFragment extends RegistrationActivityFragment
        implements
        View.OnClickListener,
        TextView.OnEditorActionListener,
        OnCompleteListener<AuthResult> {

    private EditText firstNameInput;
    private EditText emailInput;
    private EditText passwordInput;

    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        fragment.setDefaultSharedTransitions();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);
        View border = rootView.findViewById(R.id.card_view_wrapper);
        firstNameInput = rootView.findViewById(R.id.first_name);
        emailInput = rootView.findViewById(R.id.email);
        passwordInput = rootView.findViewById(R.id.password);

        passwordInput.setOnEditorActionListener(this);
        ViewCompat.setTransitionName(border, SplashFragment.TRANSITION_BACKGROUND);
        ViewCompat.setTransitionName(rootView.findViewById(R.id.member_info), SplashFragment.TRANSITION_TITLE);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = getFab();
        fab.setImageResource(R.drawable.ic_check_white_24dp);
        fab.setOnClickListener(this);

        toggleFab(true);
        setToolbarTitle(getString(R.string.sign_up));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        firstNameInput = null;
        emailInput = null;
        passwordInput = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                signUp();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            signUp();
            return true;
        }
        return false;
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if (getView() == null) return;
        if (task.isSuccessful()) {
            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                    .setDisplayName(firstNameInput.getText().toString())
                    .build();
            task.getResult().getUser().updateProfile(request)
                    .addOnCompleteListener(success -> RegistrationActivity.startMainActivity(getActivity()))
                    .addOnFailureListener(fail -> Snackbar.make(emailInput, R.string.sign_up_error_default, Snackbar.LENGTH_LONG).show());
        }
        else {
            Exception exception = task.getException();
            @StringRes int errorMessage;
            if (exception instanceof FirebaseAuthWeakPasswordException) {
                errorMessage = R.string.sign_up_error_weak_password;
            }
            else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                errorMessage = R.string.sign_up_error_invalid_credentials;
            }
            else if (exception instanceof FirebaseAuthUserCollisionException) {
                errorMessage = R.string.sign_up_error_duplicate_credentials;
            }
            else {
                errorMessage = R.string.sign_up_error_default;
            }
            Snackbar.make(emailInput, errorMessage, Snackbar.LENGTH_LONG).show();
        }
    }

    private void signUp(){
        if (validator.isValidName(firstNameInput)
                && validator.isValidEmail(emailInput)
                && validator.isValidPassword(passwordInput)) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.createUserWithEmailAndPassword(emailInput.getText().toString(),
                    passwordInput.getText().toString())
                    .addOnCompleteListener(this);
        }
    }
}
