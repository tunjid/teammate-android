package com.mainstreetcode.teammates.activities;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.FirebaseDatabase;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.baseclasses.TeammatesBaseActivity;
import com.mainstreetcode.teammates.fragments.registration.SplashFragment;
import com.mainstreetcode.teammates.viewmodel.UserViewModel;

public class RegistrationActivity extends TeammatesBaseActivity {

    private static boolean isFirebaseInitialized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        if (!isFirebaseInitialized) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            isFirebaseInitialized = true;
        }

        if (savedInstanceState == null) {
            UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

            if (!userViewModel.isSignedIn()) showFragment(SplashFragment.newInstance());
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
