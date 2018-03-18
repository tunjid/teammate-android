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
import com.mainstreetcode.teammate.baseclasses.RegistrationActivityFragment;

/**
 * Forgot password screen
 */

public final class ResetPasswordFragment extends RegistrationActivityFragment
        implements
        TextView.OnEditorActionListener {

    private static final String ARG_TOKEN = "token";

    private EditText emailInput;
    private EditText tokenInput;
    private EditText passwordInput;

    public static ResetPasswordFragment newInstance(CharSequence token) {
        ResetPasswordFragment fragment = new ResetPasswordFragment();
        Bundle args = new Bundle();

        args.putCharSequence(ARG_TOKEN, token);
        fragment.setArguments(args);
        fragment.setEnterExitTransitions();
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        return getArguments().getCharSequence(ARG_TOKEN, "").toString();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reset_password, container, false);
        View border = rootView.findViewById(R.id.card_view_wrapper);

        emailInput = rootView.findViewById(R.id.email);
        tokenInput = rootView.findViewById(R.id.token);
        passwordInput = rootView.findViewById(R.id.password);

        Bundle args = getArguments();

        if (args != null) tokenInput.setText(args.getCharSequence(ARG_TOKEN, ""));
        emailInput.setOnEditorActionListener(this);

        ViewCompat.setTransitionName(emailInput, SplashFragment.TRANSITION_TITLE);
        ViewCompat.setTransitionName(border, SplashFragment.TRANSITION_BACKGROUND);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFabIcon(R.drawable.ic_check_white_24dp);
        setFabClickListener(this);
        setToolbarTitle(getString(R.string.sign_in_forgot_password));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        emailInput = null;
    }

    @Override
    public boolean showsFab() {
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                resetPassword();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            resetPassword();
            return true;
        }
        return false;
    }

    private void resetPassword() {
        if (VALIDATOR.isValidEmail(emailInput)) {
            toggleProgress(true);

            String email = emailInput.getText().toString();
            String token = tokenInput.getText().toString();
            String password = passwordInput.getText().toString();

            disposables.add(viewModel.resetPassword(email, token, password)
                    .subscribe(message -> showSnackbar(message.getMessage(), R.string.sign_in, view -> showFragment(SignInFragment.newInstance())), defaultErrorHandler));
        }
    }
}
