package com.mainstreetcode.teammates.activities;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.baseclasses.TeammatesBaseActivity;
import com.mainstreetcode.teammates.fragments.registration.ResetPasswordFragment;
import com.mainstreetcode.teammates.fragments.registration.SplashFragment;
import com.mainstreetcode.teammates.viewmodel.UserViewModel;

public class RegistrationActivity extends TeammatesBaseActivity {


    private static final String TOKEN = "token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        if (savedInstanceState == null) {
            UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

            if (userViewModel.isSignedIn()) startMainActivity(this);
            else if (hasNoDeepLink()) showFragment(SplashFragment.newInstance());
        }
    }

    private boolean hasNoDeepLink() {
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data == null) return true;
        if (isForgotPasswordDeepLink(data)) {
            String token = data.getQueryParameter(TOKEN);
            showFragment(ResetPasswordFragment.newInstance(token != null ? token : ""));
            return false;
        }
        return true;
    }

    private boolean isForgotPasswordDeepLink(Uri data) {
        return getString(R.string.deep_link_host).equals(data.getHost()) && data.getPath().contains("forgotPassword");
    }

    public static void startMainActivity(Activity activity) {
        Intent main = new Intent(activity, MainActivity.class);
        activity.startActivity(main);
        activity.finish();
    }
}
