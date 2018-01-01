package com.mainstreetcode.teammates.fragments.registration;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

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
        TextView.OnEditorActionListener {

    private EditText emailInput;
    private EditText passwordInput;

    public static SignInFragment newInstance() {
        SignInFragment fragment = new SignInFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        fragment.setEnterExitTransitions();
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
        setFabIcon(R.drawable.ic_check_white_24dp);
        setFabClickListener(this);
        setToolbarTitle(getString(R.string.sign_in));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        emailInput = null;
        passwordInput = null;
    }

    @Override
    protected boolean showsFab() {
        return true;
    }

    @Nullable
    @Override
    @SuppressLint("CommitTransaction")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        if (getView() != null && fragmentTo.getStableTag().contains(ForgotPasswordFragment.class.getSimpleName())) {
            return beginTransaction()
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
            toggleProgress(true);

            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            disposables.add(viewModel.signIn(email, password)
                    .subscribe((authResult) -> RegistrationActivity.startMainActivity(getActivity()),
                            defaultErrorHandler)
            );
        }
    }
}
