package com.mainstreetcode.teammates.activities;

import android.os.Bundle;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.baseclasses.TeammatesBaseActivity;
import com.mainstreetcode.teammates.fragments.SplashFragment;

public class SplashActivity extends TeammatesBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (savedInstanceState == null) showFragment(SplashFragment.newInstance());
    }

    protected boolean isFullscreenFragment(String tag) {
        return tag != null && tag.contains(SplashFragment.class.getSimpleName());
    }
}
