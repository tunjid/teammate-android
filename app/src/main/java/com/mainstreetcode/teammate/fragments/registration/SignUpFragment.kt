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

package com.mainstreetcode.teammate.fragments.registration

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.activities.RegistrationActivity
import com.mainstreetcode.teammate.baseclasses.RegistrationActivityFragment
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.hasValidName
import com.mainstreetcode.teammate.util.hasValidEmail
import com.mainstreetcode.teammate.util.hasValidPassword
import com.tunjid.androidbootstrap.core.text.SpanBuilder
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import java.util.concurrent.TimeUnit


class SignUpFragment : RegistrationActivityFragment(), TextView.OnEditorActionListener {

    private var firstNameInput: EditText? = null
    private var lastNameInput: EditText? = null
    private var emailInput: EditText? = null
    private var passwordInput: EditText? = null
    private var terms: CheckBox? = null

    override val fabStringResource: Int
        @StringRes
        get() = R.string.submit

    override val fabIconResource: Int
        @DrawableRes
        get() = R.drawable.ic_check_white_24dp

    override val toolbarTitle: CharSequence
        get() = getString(R.string.sign_up)

    private val termsCharSequence: CharSequence
        get() = SpanBuilder.format(getString(R.string.sign_up_terms_phrase),
                clickableSpan(getString(R.string.sign_up_terms)) { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(TeammateService.API_BASE_URL + "terms"))) },
                clickableSpan(getString(R.string.sign_up_privacy_policy)) { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(TeammateService.API_BASE_URL + "privacy"))) })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_sign_up, container, false)
        val border = rootView.findViewById<View>(R.id.card_view_wrapper)
        firstNameInput = rootView.findViewById(R.id.first_name)
        lastNameInput = rootView.findViewById(R.id.last_name)
        emailInput = rootView.findViewById(R.id.email)
        passwordInput = rootView.findViewById(R.id.password)
        terms = rootView.findViewById(R.id.terms)

        passwordInput!!.setOnEditorActionListener(this)
        terms!!.text = termsCharSequence
        terms!!.movementMethod = LinkMovementMethod.getInstance()

        ViewCompat.setTransitionName(border, SplashFragment.TRANSITION_BACKGROUND)
        ViewCompat.setTransitionName(rootView.findViewById(R.id.member_info), SplashFragment.TRANSITION_TITLE)

        disposables.add(Completable.timer(200, TimeUnit.MILLISECONDS).observeOn(mainThread()).subscribe({
            rootView.findViewById<View>(R.id.first_name_wrapper).visibility = View.VISIBLE
            rootView.findViewById<View>(R.id.last_name_wrapper).visibility = View.VISIBLE
            rootView.findViewById<View>(R.id.email_wrapper).visibility = View.VISIBLE
            rootView.findViewById<View>(R.id.password_wrapper).visibility = View.VISIBLE
        }, ErrorHandler.EMPTY::invoke))

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        firstNameInput = null
        lastNameInput = null
        emailInput = null
        passwordInput = null
    }

    override val showsFab: Boolean get() = true

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> signUp()
        }
    }

    override fun onEditorAction(textView: TextView, actionId: Int, event: KeyEvent): Boolean = when {
        actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN -> signUp().let { true }
        else -> false
    }

    private fun signUp() {
        val hasFirstName = firstNameInput.hasValidName
        val hasLastName = lastNameInput.hasValidName
        val hasEmail = emailInput.hasValidEmail
        val hasPassword = passwordInput.hasValidPassword
        val acceptedTerms = terms!!.isChecked

        if (!acceptedTerms) showSnackbar(getString(R.string.sign_up_terms_accept))

        if (hasFirstName && hasLastName && hasEmail && hasPassword && acceptedTerms) {
            val firstName = firstNameInput!!.text.toString()
            val lastName = lastNameInput!!.text.toString()
            val email = emailInput!!.text.toString()
            val password = passwordInput!!.text.toString()

            toggleProgress(true)

            disposables.add(viewModel.signUp(firstName, lastName, email, password)
                    .subscribe({ onSignUp() }, defaultErrorHandler::invoke)
            )
        }
    }

    private fun onSignUp() {
        toggleProgress(false)
        hideKeyboard()
        RegistrationActivity.startMainActivity(activity)
    }

    private fun clickableSpan(text: CharSequence, clickAction: () -> Unit): CharSequence {
        val spannableString = SpannableString(text)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) = clickAction.invoke()

            override fun updateDrawState(ds: TextPaint) {
                ds.color = Color.WHITE
                ds.isUnderlineText = true
            }
        }

        spannableString.setSpan(clickableSpan, 0, spannableString.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    companion object {

        fun newInstance(): SignUpFragment {
            val fragment = SignUpFragment()
            val args = Bundle()

            fragment.arguments = args
            fragment.setEnterExitTransitions()
            return fragment
        }
    }
}