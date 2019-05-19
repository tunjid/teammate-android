/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.fragments.registration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.ViewCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.activities.RegistrationActivity;
import com.mainstreetcode.teammate.baseclasses.RegistrationActivityFragment;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import java.util.Arrays;
import java.util.List;

/**
 * Splash screen
 */

public class SplashFragment extends RegistrationActivityFragment
        implements View.OnClickListener {

    static final String TRANSITION_BACKGROUND = "transition-background";
    static final String TRANSITION_TITLE = "transition-title";
    static final String TRANSITION_SUBTITLE = "transition-subtitle";

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

        login.setText(SpanBuilder.of(getString(R.string.login_have_account))
                .appendNewLine()
                .append(SpanBuilder.of(getString(R.string.login_sign_in))
                        .color(context, R.color.white)
                        .underline()
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
    public void onDestroy() {
        super.onDestroy();
        LoginManager.getInstance().unregisterCallback(faceBookResultCallback);
    }

    @Override
    public InsetFlags insetFlags() {return NO_TOP;}

    @Override
    public boolean showsFab() {return false;}

    @Override
    public boolean showsToolBar() {return false;}

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

    private CallbackManager faceBookResultCallback = CallbackManager.Factory.create();
    private FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
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
            showSnackbar(getString(R.string.error_default));
        }
    };
}
