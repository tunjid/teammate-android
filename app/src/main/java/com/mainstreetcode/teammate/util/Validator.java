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

package com.mainstreetcode.teammate.util;

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
