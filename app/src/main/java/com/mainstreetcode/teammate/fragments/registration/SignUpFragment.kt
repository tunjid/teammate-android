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
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.databinding.FragmentSignUpBinding
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.hasValidEmail
import com.mainstreetcode.teammate.util.hasValidName
import com.mainstreetcode.teammate.util.hasValidPassword
import com.mainstreetcode.teammate.util.input
import com.tunjid.androidx.core.text.SpanBuilder
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import java.util.concurrent.TimeUnit


class SignUpFragment : TeammatesBaseFragment(R.layout.fragment_sign_up), TextView.OnEditorActionListener {

    private var binding: FragmentSignUpBinding? = null

    private val termsCharSequence: CharSequence
        get() = SpanBuilder.format(getString(R.string.sign_up_terms_phrase),
                clickableSpan(getString(R.string.sign_up_terms)) { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(TeammateService.API_BASE_URL + "terms"))) },
                clickableSpan(getString(R.string.sign_up_privacy_policy)) { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(TeammateService.API_BASE_URL + "privacy"))) })

    override val showsFab: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = FragmentSignUpBinding.bind(view).run {
        super.onViewCreated(view, savedInstanceState)
        binding = this

        defaultUi(
                fabText = R.string.submit,
                fabIcon = R.drawable.ic_check_white_24dp,
                fabShows = true,
                toolbarShows = false,
                bottomNavShows = false,
                grassShows = true,
                navBarColor = GRASS_COLOR
        )

        disposables.add(Completable.timer(200, TimeUnit.MILLISECONDS).observeOn(mainThread()).subscribe({
            firstNameWrapper.visibility = View.VISIBLE
            lastNameWrapper.visibility = View.VISIBLE
            emailWrapper.visibility = View.VISIBLE
            passwordWrapper.visibility = View.VISIBLE
        }, ErrorHandler.EMPTY::invoke))

        terms.text = termsCharSequence
        terms.movementMethod = LinkMovementMethod.getInstance()

        memberInfo.transitionName = SplashFragment.TRANSITION_TITLE
        cardViewWrapper.transitionName = SplashFragment.TRANSITION_BACKGROUND

        password.setOnEditorActionListener(this@SignUpFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onClick(view: View) = when (view.id) {
        R.id.fab -> signUp()
        else -> Unit
    }

    override fun onEditorAction(textView: TextView, actionId: Int, event: KeyEvent): Boolean = when {
        actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN -> signUp().let { true }
        else -> false
    }

    private fun signUp() {
        val binding = this.binding ?: return
        val hasFirstName = binding.firstName.hasValidName
        val hasLastName = binding.lastName.hasValidName
        val hasEmail = binding.email.hasValidEmail
        val hasPassword = binding.password.hasValidPassword
        val acceptedTerms = binding.terms.isChecked

        if (acceptedTerms) transientBarDriver.showSnackBar(getString(R.string.sign_up_terms_accept))

        if (hasFirstName && hasLastName && hasEmail && hasPassword && acceptedTerms) {
            val firstName = binding.firstName.input.toString()
            val lastName = binding.lastName.input.toString()
            val email = binding.email.input.toString()
            val password = binding.password.input.toString()

            transientBarDriver.toggleProgress(true)

            disposables.add(userViewModel.signUp(firstName, lastName, email, password)
                    .subscribe({ onSignUp() }, defaultErrorHandler::invoke)
            )
        }
    }

    private fun onSignUp() {
        transientBarDriver.toggleProgress(false)
        hideKeyboard()
        navigator.completeSignIn()
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

        fun newInstance(): SignUpFragment = SignUpFragment().apply {
            arguments = Bundle()
            setEnterExitTransitions()
        }
    }
}