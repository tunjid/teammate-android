package com.mainstreetcode.teammates.fragments.registration;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
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
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.activities.RegistrationActivity;
import com.mainstreetcode.teammates.baseclasses.RegistrationActivityFragment;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

/**
 * Splash screen
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public final class SignInFragment extends RegistrationActivityFragment
        implements
        View.OnClickListener,
        TextView.OnEditorActionListener,
        OnCompleteListener<AuthResult> {

    private EditText emailInput;
    private EditText passwordInput;

    public static SignInFragment newInstance() {
        SignInFragment fragment = new SignInFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        fragment.setDefaultSharedTransitions();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);
        View border = rootView.findViewById(R.id.card_view_wrapper);
        emailInput = rootView.findViewById(R.id.email);
        passwordInput = rootView.findViewById(R.id.password);

        rootView.findViewById(R.id.forgot_password).setOnClickListener(this);
        passwordInput.setOnEditorActionListener(this);

        ViewCompat.setTransitionName(emailInput, SplashFragment.TRANSITION_TITLE);
        ViewCompat.setTransitionName(passwordInput, SplashFragment.TRANSITION_SUBTITLE);
        ViewCompat.setTransitionName(border, SplashFragment.TRANSITION_BACKGROUND);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = getFab();
        fab.setImageResource(R.drawable.ic_check_white_24dp);
        fab.setOnClickListener(this);

        toggleFab(true);
        setToolbarTitle(getString(R.string.sign_in));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        emailInput = null;
        passwordInput = null;
    }

    @Nullable
    @Override
    @SuppressLint("CommitTransaction")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        if (getView() != null && fragmentTo.getStableTag().contains(ForgotPasswordFragment.class.getSimpleName())) {
            return getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .addSharedElement(emailInput, SplashFragment.TRANSITION_TITLE)
                    .addSharedElement(getView().findViewById(R.id.card_view_wrapper), SplashFragment.TRANSITION_BACKGROUND);
        }
        return super.provideFragmentTransaction(fragmentTo);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                signIn();
                break;
            case R.id.forgot_password:
                showFragment(ForgotPasswordFragment.newInstance(emailInput.getText()));
                        break;
        }
    }


    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if (getView() == null) return;
        toggleProgress(false);
        if (task.isSuccessful()) RegistrationActivity.startMainActivity(getActivity());
        else {
            Exception exception = task.getException();
            String message;
            if (exception instanceof FirebaseAuthWeakPasswordException) {
                message = getString(R.string.sign_up_error_weak_password);
            }
            else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                message = getString(R.string.sign_up_error_invalid_credentials);
            }
            else if (exception instanceof FirebaseAuthUserCollisionException) {
                message = getString(R.string.sign_up_error_duplicate_credentials);
            }
            else {
                message = "Error signing in, please try agin later";
            }
            Snackbar.make(emailInput, message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            signIn();
            return true;
        }
        return false;
    }

    private void signIn() {
        if (validator.isValidEmail(emailInput) && validator.isValidPassword(passwordInput)) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(emailInput.getText().toString(),
                    passwordInput.getText().toString())
                    .addOnCompleteListener(this);
            toggleProgress(true);
        }
    }
}
