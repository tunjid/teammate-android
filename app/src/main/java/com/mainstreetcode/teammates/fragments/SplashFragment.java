package com.mainstreetcode.teammates.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.baseclasses.TeammatesBaseFragment;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;

/**
 * Spalsh screen
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public class SplashFragment extends TeammatesBaseFragment
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
        TextView options = rootView.findViewById(R.id.options_text);
        TextView login = rootView.findViewById(R.id.login);

        ViewCompat.setTransitionName(rootView.findViewById(R.id.title), TRANSITION_BACKGROUND);
        ViewCompat.setTransitionName(rootView.findViewById(R.id.sub_title), TRANSITION_TITLE);
        ViewCompat.setTransitionName(rootView.findViewById(R.id.border), TRANSITION_SUBTITLE);

        Context context = rootView.getContext();

        String emailText = getString(R.string.login_email);

        Spannable span = Spannable.Factory.getInstance().newSpannable(emailText);
        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View v) {
                showFragment(SignInFragment.newInstance());
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(Color.WHITE);
                ds.setUnderlineText(false);
            }
        }, 0, emailText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        options.setMovementMethod(LinkMovementMethod.getInstance());

        options.setText(new SpanBuilder(context, getString(R.string.login_facebook))
                .appendNewLine()
                .appendCharsequence(new SpanBuilder(context, getString(R.string.login_or)).resize(0.8F).build())
                .appendNewLine()
                .appendCharsequence(span)
                .build()
        );

        login.setText(new SpanBuilder(context, getString(R.string.login_have_account))
                .appendNewLine()
                .appendCharsequence(new SpanBuilder(context, getString(R.string.login_sign_in))
                        .color(R.color.colorAccent)
                        .build())
                .build());

        login.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toggleToolbar(false);
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
                        .addSharedElement(rootView.findViewById(R.id.title), TRANSITION_BACKGROUND)
                        .addSharedElement(rootView.findViewById(R.id.sub_title), TRANSITION_TITLE)
                        .addSharedElement(rootView.findViewById(R.id.border), TRANSITION_SUBTITLE);
            }
        }
        return super.provideFragmentTransaction(fragmentTo);
    }

    @Override
    public void onClick(View view) {

    }
}
