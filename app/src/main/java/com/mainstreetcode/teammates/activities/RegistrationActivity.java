package com.mainstreetcode.teammates.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.baseclasses.TeammatesBaseActivity;
import com.mainstreetcode.teammates.fragments.registration.SplashFragment;

public class RegistrationActivity extends TeammatesBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        if (savedInstanceState == null) {
            if (FirebaseAuth.getInstance().getCurrentUser() == null)
                showFragment(SplashFragment.newInstance());
            else startMainActivity(this);
        }
    }

    protected boolean isFullscreenFragment(String tag) {
        return tag != null && tag.contains(SplashFragment.class.getSimpleName());
    }

    public static void startMainActivity(Activity activity) {
        Intent main = new Intent(activity, MainActivity.class);
        activity.startActivity(main);
        activity.finish();
    }
}
