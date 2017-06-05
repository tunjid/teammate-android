package com.mainstreetcode.teammates.fragments.registration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.activities.RegistrationActivity;
import com.mainstreetcode.teammates.baseclasses.RegistrationActivityFragment;
import com.mainstreetcode.teammates.util.ErrorHandler;

/**
 * Forgot password screen
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public final class ForgotPasswordFragment extends RegistrationActivityFragment
        implements
        View.OnClickListener,
        TextView.OnEditorActionListener {

    private static final String ARG_EMAIL = "email";

    private EditText emailInput;

    public static ForgotPasswordFragment newInstance(CharSequence email) {
        ForgotPasswordFragment fragment = new ForgotPasswordFragment();
        Bundle args = new Bundle();

        args.putCharSequence(ARG_EMAIL, email);
        fragment.setArguments(args);
        fragment.setDefaultSharedTransitions();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        View border = rootView.findViewById(R.id.card_view_wrapper);
        emailInput = rootView.findViewById(R.id.email);

        emailInput.setText(getArguments().getCharSequence(ARG_EMAIL, ""));
        emailInput.setOnEditorActionListener(this);

        ViewCompat.setTransitionName(emailInput, SplashFragment.TRANSITION_TITLE);
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
        setToolbarTitle(getString(R.string.sign_in_forgot_password));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        emailInput = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                sendForgotEmail();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            sendForgotEmail();
            return true;
        }
        return false;
    }

    private void sendForgotEmail() {
        if (validator.isValidEmail(emailInput)) {
            toggleProgress(true);

            String email = emailInput.getText().toString();

            disposables.add(viewModel.forgotPassword(email)
                    .subscribe((Void) -> RegistrationActivity.startMainActivity(getActivity()),
                            ErrorHandler.builder()
                                    .defaultMessage(getString(R.string.default_error))
                                    .add(this::showErrorSnackbar)
                                    .add(getString(R.string.sign_in_error_invalid_email), FirebaseAuthInvalidUserException.class)
                                    .build())
            );
        }
    }
}
