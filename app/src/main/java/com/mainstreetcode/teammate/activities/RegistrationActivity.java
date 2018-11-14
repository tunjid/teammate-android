package com.mainstreetcode.teammate.activities;

import android.app.Activity;

import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseActivity;
import com.mainstreetcode.teammate.fragments.registration.ResetPasswordFragment;
import com.mainstreetcode.teammate.fragments.registration.SplashFragment;
import com.mainstreetcode.teammate.viewmodel.UserViewModel;

public class RegistrationActivity extends TeammatesBaseActivity {


    private static final String TOKEN = "token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        toggleToolbar(false);

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
        String domain1 = getString(R.string.domain_1);
        String domain2 = getString(R.string.domain_2);

        String path = data.getPath();
        if (path == null) return false;
        boolean domainMatches = domain1.equals(data.getHost()) || domain2.equals(data.getHost());
        return domainMatches && path.contains("forgotPassword");
    }

    public static void startMainActivity(@Nullable Activity activity) {
        if (activity == null) return;
        Intent main = new Intent(activity, MainActivity.class);
        activity.startActivity(main);
        activity.finish();
    }

    @Override
    public void toggleToolbar(boolean show) {
        super.toggleToolbar(false);
    }
}
