package com.mainstreetcode.teammates.fragments.registration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.baseclasses.RegistrationActivityFragment;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;

/**
 * Spalsh screen
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public class SplashFragment extends RegistrationActivityFragment
        implements View.OnClickListener {

    public static final String TRANSITION_BACKGROUND = "transition-background";
    public static final String TRANSITION_TITLE = "transition-title";
    public static final String TRANSITION_SUBTITLE = "transition-subtitle";

    public static SplashFragment newInstance() {
        SplashFragment fragment = new SplashFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_splash, container, false);
        TextView emailSignUp = rootView.findViewById(R.id.email_sign_up);
        TextView login = rootView.findViewById(R.id.login);

        Context context = rootView.getContext();

        login.setText(new SpanBuilder(context, getString(R.string.login_have_account))
                .appendNewLine()
                .appendCharsequence(new SpanBuilder(context, getString(R.string.login_sign_in))
                        .color(R.color.colorAccent)
                        .build())
                .build());

        emailSignUp.setOnClickListener(this);
        login.setOnClickListener(this);

        ViewCompat.setTransitionName(rootView.findViewById(R.id.title), TRANSITION_TITLE);
        ViewCompat.setTransitionName(rootView.findViewById(R.id.sub_title), TRANSITION_SUBTITLE);
        ViewCompat.setTransitionName(rootView.findViewById(R.id.border), TRANSITION_BACKGROUND);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toggleToolbar(false);
        toggleFab(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        toggleToolbar(true);
    }

    @Nullable
    @Override
    @SuppressLint("CommitTransaction")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        View rootView = getView();
        if (rootView != null) {
            if (fragmentTo.getStableTag().contains(SignInFragment.class.getSimpleName())) {
                return getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addSharedElement(rootView.findViewById(R.id.border), TRANSITION_BACKGROUND)
                        .addSharedElement(rootView.findViewById(R.id.title), TRANSITION_TITLE)
                        .addSharedElement(rootView.findViewById(R.id.sub_title), TRANSITION_SUBTITLE);
            }
            else if (fragmentTo.getStableTag().contains(SignUpFragment.class.getSimpleName())) {
                return getActivity().getSupportFragmentManager()
                        .beginTransaction()
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
        }
    }
}
