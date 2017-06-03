package com.mainstreetcode.teammates.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.baseclasses.TeammatesBaseFragment;

/**
 * Splash screen
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public class SignInFragment extends TeammatesBaseFragment
        implements View.OnClickListener {

    EditText emailInput;
    EditText passwordInput;

    public static SignInFragment newInstance() {
        SignInFragment fragment = new SignInFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition baseTransition = new Fade();
            Transition baseSharedTransition = getTransition();

            //baseTransition.excludeTarget(R.id.image, true);

            fragment.setEnterTransition(baseTransition);
            fragment.setExitTransition(baseTransition);
            fragment.setSharedElementEnterTransition(baseSharedTransition);
            fragment.setSharedElementReturnTransition(baseSharedTransition);
        }
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);
        View border = rootView.findViewById(R.id.card_view_wrapper);
        emailInput = rootView.findViewById(R.id.email);
        passwordInput = rootView.findViewById(R.id.password);

        ViewCompat.setTransitionName(emailInput, SplashFragment.TRANSITION_BACKGROUND);
        ViewCompat.setTransitionName(passwordInput, SplashFragment.TRANSITION_TITLE);
        ViewCompat.setTransitionName(border, SplashFragment.TRANSITION_SUBTITLE);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        emailInput = null;
        passwordInput = null;
    }

    @Override
    public void onClick(View view) {

    }

    static android.transition.Transition getTransition() {
        TransitionSet result = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            result = new TransitionSet();

            result.setOrdering(TransitionSet.ORDERING_TOGETHER)
                    .addTransition(new ChangeBounds())
                    .addTransition(new ChangeTransform())
                    .addTransition(new ChangeImageTransform());
        }
        return result;
    }
}
