package com.mainstreetcode.teammates.util;

import android.text.TextUtils;
import android.widget.EditText;

import java.util.regex.Pattern;

/**
 * Validates values
 * <p>
 * Created by Shemanigans on 6/2/17.
 */

public class Validator {

    private static final Pattern emailPattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-]+)\\.([a-zA-Z]{2,5})$");

    @SuppressWarnings("WeakerAccess")
    public boolean isValidEmail(CharSequence email) {
        return emailPattern.matcher(email).matches();
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isValidPassword(CharSequence password) {
        return password.length() > 6;
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isNotEmpty(CharSequence name) {
        return !TextUtils.isEmpty(name);
    }

    public boolean isValidEmail(EditText editText) {
        boolean isValid = isValidEmail(editText.getText());
        if (!isValid) editText.setError("Invalid email");
        return isValid;
    }

    public boolean isValidPassword(EditText editText) {
        boolean isValid = isValidPassword(editText.getText());
        if (!isValid) editText.setError("Invalid password");
        return isValid;
    }

    public boolean isNotEmpty(EditText editText) {
        boolean isValid = isNotEmpty(editText.getText());
        if (!isValid) editText.setError("Invalid name");
        return isValid;
    }
}
