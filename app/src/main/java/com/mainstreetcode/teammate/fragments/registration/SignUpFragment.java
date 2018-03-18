package com.mainstreetcode.teammate.fragments.registration;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.activities.RegistrationActivity;
import com.mainstreetcode.teammate.baseclasses.RegistrationActivityFragment;

/**
 * Splash screen
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public class SignUpFragment extends RegistrationActivityFragment
        implements
        TextView.OnEditorActionListener {

    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText emailInput;
    private EditText passwordInput;


    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        fragment.setEnterExitTransitions();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);
        View border = rootView.findViewById(R.id.card_view_wrapper);
        firstNameInput = rootView.findViewById(R.id.first_name);
        lastNameInput = rootView.findViewById(R.id.last_name);
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
        setFabIcon(R.drawable.ic_check_white_24dp);
        setFabClickListener(this);
        setToolbarTitle(getString(R.string.sign_up));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        firstNameInput = null;
        lastNameInput = null;
        emailInput = null;
        passwordInput = null;
    }

    @Override
    public boolean showsFab() {
        return true;
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

    private void signUp() {
        if (VALIDATOR.isNotEmpty(firstNameInput)
                && VALIDATOR.isValidEmail(emailInput)
                && VALIDATOR.isValidPassword(passwordInput)) {

            String firstName = firstNameInput.getText().toString();
            String lastname = lastNameInput.getText().toString();
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            toggleProgress(true);

            disposables.add(viewModel.signUp(firstName, lastname, email, password)
                    .subscribe((user) -> RegistrationActivity.startMainActivity(getActivity()),
                            defaultErrorHandler)
            );
        }
    }
}

// (exception) -> {
//@StringRes int errorMessage;
//        if (exception instanceof FirebaseAuthWeakPasswordException) {
//        errorMessage = R.string.sign_up_error_weak_password;
//        }
//        else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
//        errorMessage = R.string.sign_up_error_invalid_credentials;
//        }
//        else if (exception instanceof FirebaseAuthUserCollisionException) {
//        errorMessage = R.string.sign_up_error_duplicate_credentials;
//        }
//        else {
//        errorMessage = R.string.sign_up_error_default;
//        }
//        Snackbar.make(emailInput, errorMessage, Snackbar.LENGTH_LONG).show();
//        }