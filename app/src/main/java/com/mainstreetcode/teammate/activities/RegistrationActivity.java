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

package com.mainstreetcode.teammate.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseActivity;
import com.mainstreetcode.teammate.fragments.registration.ResetPasswordFragment;
import com.mainstreetcode.teammate.fragments.registration.SplashFragment;
import com.mainstreetcode.teammate.viewmodel.UserViewModel;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

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
