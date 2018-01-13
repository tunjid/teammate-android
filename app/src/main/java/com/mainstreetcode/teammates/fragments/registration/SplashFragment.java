package com.mainstreetcode.teammates.fragments.registration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.activities.RegistrationActivity;
import com.mainstreetcode.teammates.baseclasses.RegistrationActivityFragment;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Splash screen
 */

public class SplashFragment extends RegistrationActivityFragment
        implements View.OnClickListener {

    public static final String TRANSITION_BACKGROUND = "transition-background";
    public static final String TRANSITION_TITLE = "transition-title";
    public static final String TRANSITION_SUBTITLE = "transition-subtitle";

    private final static List<String> FACEBOOK_PERMISSIONS = Arrays.asList("email", "public_profile");

    public static SplashFragment newInstance() {
        SplashFragment fragment = new SplashFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_splash, container, false);
        TextView facebookSignUp = rootView.findViewById(R.id.facebook_login);
        TextView emailSignUp = rootView.findViewById(R.id.email_sign_up);
        TextView login = rootView.findViewById(R.id.login);

        Context context = rootView.getContext();

        login.setText(new SpanBuilder(context, getString(R.string.login_have_account))
                .appendNewLine()
                .appendCharsequence(new SpanBuilder(context, getString(R.string.login_sign_in))
                        .color(R.color.colorAccent)
                        .build())
                .build());

        facebookSignUp.setOnClickListener(this);
        emailSignUp.setOnClickListener(this);
        login.setOnClickListener(this);

        ViewCompat.setTransitionName(rootView.findViewById(R.id.title), TRANSITION_TITLE);
        ViewCompat.setTransitionName(rootView.findViewById(R.id.sub_title), TRANSITION_SUBTITLE);
        ViewCompat.setTransitionName(rootView.findViewById(R.id.border), TRANSITION_BACKGROUND);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LoginManager.getInstance().unregisterCallback(faceBookResultCallback);
    }

    @Override
    public boolean drawsBehindStatusBar() {
        return true;
    }

    @Override
    protected boolean showsFab() {
        return false;
    }

    @Override
    protected boolean showsToolBar() {
        return false;
    }

    @Nullable
    @Override
    @SuppressLint("CommitTransaction")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        View rootView = getView();
        if (rootView != null) {
            if (fragmentTo.getStableTag().contains(SignInFragment.class.getSimpleName())) {
                return beginTransaction()
                        .addSharedElement(rootView.findViewById(R.id.border), TRANSITION_BACKGROUND)
                        .addSharedElement(rootView.findViewById(R.id.title), TRANSITION_TITLE)
                        .addSharedElement(rootView.findViewById(R.id.sub_title), TRANSITION_SUBTITLE);
            }
            else if (fragmentTo.getStableTag().contains(SignUpFragment.class.getSimpleName())) {
                return beginTransaction()
                        .addSharedElement(rootView.findViewById(R.id.border), TRANSITION_BACKGROUND)
                        .addSharedElement(rootView.findViewById(R.id.title), TRANSITION_TITLE);
            }
        }
        return super.provideFragmentTransaction(fragmentTo);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.email_sign_up:
                showFragment(SignUpFragment.newInstance());
                break;
            case R.id.login:
                showFragment(SignInFragment.newInstance());
                break;
            case R.id.facebook_login:
                LoginManager loginManager = LoginManager.getInstance();
                loginManager.registerCallback(faceBookResultCallback, facebookCallback);
                loginManager.logInWithReadPermissions(this, FACEBOOK_PERMISSIONS);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Forward callbacks to social network SDKs
        faceBookResultCallback.onActivityResult(requestCode, resultCode, data);
    }

    CallbackManager faceBookResultCallback = CallbackManager.Factory.create();
    FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            toggleProgress(true);
            disposables.add(viewModel.signIn(loginResult).subscribe((authResult) -> RegistrationActivity.startMainActivity(getActivity()), defaultErrorHandler));
        }

        @Override
        public void onCancel() {
            toggleProgress(false);
            showSnackbar(getString(R.string.cancelled));
        }

        @Override
        public void onError(FacebookException error) {
            toggleProgress(false);
            showSnackbar(getString(R.string.default_error));
        }
    };
}
