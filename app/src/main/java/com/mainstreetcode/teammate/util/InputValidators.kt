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

package com.mainstreetcode.teammate.util

import android.widget.EditText

import java.util.regex.Pattern

/**
 * Validates values
 *
 *
 * Created by Shemanigans on 6/2/17.
 */

private val emailPattern = Pattern.compile("^([a-zA-Z0-9_\\-.]+)@([a-zA-Z0-9_\\-]+)\\.([a-zA-Z]{2,5})$")

val EditText?.input: CharSequence
    get() = if(this == null) "" else text

val EditText?.hasValidPassword: Boolean
    get() = guard("Invalid password", CharSequence::isValidPassword)

val EditText?.hasValidName: Boolean
    get() = guard("Invalid name", CharSequence::isNotBlank)

val EditText?.hasValidEmail: Boolean
    get() = guard("Invalid email", CharSequence::isValidEmail)

private inline fun EditText?.guard(errorText: CharSequence, checker: (CharSequence) -> Boolean): Boolean {
    if (this == null) return false
    val isValid = checker(text)
    if (!isValid) error = errorText
    return isValid
}

private fun CharSequence.isValidEmail(): Boolean = emailPattern.matcher(this).matches()

private fun CharSequence.isValidPassword(): Boolean = length > 6
