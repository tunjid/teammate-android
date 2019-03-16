package com.mainstreetcode.teammate.fragments.registration;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.activities.RegistrationActivity;
import com.mainstreetcode.teammate.baseclasses.RegistrationActivityFragment;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;

import java.util.concurrent.TimeUnit;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import io.reactivex.Completable;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;


public class SignUpFragment extends RegistrationActivityFragment
        implements
        TextView.OnEditorActionListener {

    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private CheckBox terms;

    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        fragment.setEnterExitTransitions();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);
        View border = rootView.findViewById(R.id.card_view_wrapper);
        firstNameInput = rootView.findViewById(R.id.first_name);
        lastNameInput = rootView.findViewById(R.id.last_name);
        emailInput = rootView.findViewById(R.id.email);
        passwordInput = rootView.findViewById(R.id.password);
        terms = rootView.findViewById(R.id.terms);

        passwordInput.setOnEditorActionListener(this);
        terms.setText(getTermsCharSequence());
        terms.setMovementMethod(LinkMovementMethod.getInstance());

        ViewCompat.setTransitionName(border, SplashFragment.TRANSITION_BACKGROUND);
        ViewCompat.setTransitionName(rootView.findViewById(R.id.member_info), SplashFragment.TRANSITION_TITLE);

        disposables.add(Completable.timer(200, TimeUnit.MILLISECONDS).observeOn(mainThread()).subscribe(() -> {
            rootView.findViewById(R.id.first_name_wrapper).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.last_name_wrapper).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.email_wrapper).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.password_wrapper).setVisibility(View.VISIBLE);
        }, ErrorHandler.EMPTY));

        return rootView;
    }

    @Override
    @StringRes
    protected int getFabStringResource() { return R.string.submit; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_check_white_24dp; }

    @Override protected CharSequence getToolbarTitle() {
        return getString(R.string.sign_up);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        firstNameInput = null;
        lastNameInput = null;
        emailInput = null;
        passwordInput = null;
    }

    @Override
    public boolean showsFab() {
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                signUp();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            signUp();
            return true;
        }
        return false;
    }

    private void signUp() {
        boolean hasFirstName = VALIDATOR.isNotEmpty(firstNameInput);
        boolean hasLastName = VALIDATOR.isNotEmpty(lastNameInput);
        boolean hasEmail = VALIDATOR.isValidEmail(emailInput);
        boolean hasPassword = VALIDATOR.isValidPassword(passwordInput);
        boolean acceptedTerms = terms.isChecked();

        if (!acceptedTerms) showSnackbar(getString(R.string.sign_up_terms_accept));

        if (hasFirstName && hasLastName && hasEmail && hasPassword && acceptedTerms) {
            String firstName = firstNameInput.getText().toString();
            String lastName = lastNameInput.getText().toString();
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            toggleProgress(true);

            disposables.add(viewModel.signUp(firstName, lastName, email, password)
                    .subscribe(user -> onSignUp(), defaultErrorHandler)
            );
        }
    }

    private void onSignUp() {
        toggleProgress(false);
        hideKeyboard();
        RegistrationActivity.startMainActivity(getActivity());
    }

    private CharSequence getTermsCharSequence() {
        return SpanBuilder.format(getString(R.string.sign_up_terms_phrase),
                clickableSpan(getString(R.string.sign_up_terms), () -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TeammateService.API_BASE_URL + "terms")))),
                clickableSpan(getString(R.string.sign_up_privacy_policy), () -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TeammateService.API_BASE_URL + "privacy")))));
    }

    private CharSequence clickableSpan(CharSequence text, Runnable clickAction) {
        SpannableString spannableString = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            public void onClick(View widget) {
                clickAction.run();
            }

            public void updateDrawState(TextPaint ds) {
                ds.setColor(Color.WHITE);
                ds.setUnderlineText(true);
            }
        };

        spannableString.setSpan(clickableSpan, 0, spannableString.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }
}