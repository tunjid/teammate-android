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

import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.baseclasses.RegistrationActivityFragment;

/**
 * Forgot password screen
 */

public final class ForgotPasswordFragment extends RegistrationActivityFragment
        implements
        TextView.OnEditorActionListener {

    private static final String ARG_EMAIL = "email";

    private EditText emailInput;

    public static ForgotPasswordFragment newInstance(CharSequence email) {
        ForgotPasswordFragment fragment = new ForgotPasswordFragment();
        Bundle args = new Bundle();

        args.putCharSequence(ARG_EMAIL, email);
        fragment.setArguments(args);
        fragment.setEnterExitTransitions();
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        View border = rootView.findViewById(R.id.card_view_wrapper);
        emailInput = rootView.findViewById(R.id.email);

        Bundle args = getArguments();

        if (args != null) emailInput.setText(args.getCharSequence(ARG_EMAIL, ""));
        emailInput.setOnEditorActionListener(this);

        ViewCompat.setTransitionName(emailInput, SplashFragment.TRANSITION_TITLE);
        ViewCompat.setTransitionName(border, SplashFragment.TRANSITION_BACKGROUND);

        return rootView;
    }

    @Override
    @StringRes
    protected int getFabStringResource() { return R.string.submit; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_check_white_24dp; }

    @Override
    protected CharSequence getToolbarTitle() {
        return getString(R.string.sign_in_forgot_password);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        emailInput = null;
    }

    @Override
    public boolean showsFab() {
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                sendForgotEmail();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            sendForgotEmail();
            return true;
        }
        return false;
    }

    private void sendForgotEmail() {
        if (VALIDATOR.isValidEmail(emailInput)) {
            toggleProgress(true);

            String email = emailInput.getText().toString();

            disposables.add(viewModel.forgotPassword(email)
                    .subscribe(message -> showSnackbar(message.getMessage()), defaultErrorHandler));
        }
    }
}
