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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentTransaction
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.activities.RegistrationActivity
import com.mainstreetcode.teammate.baseclasses.RegistrationActivityFragment
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.hasValidEmail
import com.mainstreetcode.teammate.util.hasValidPassword
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import java.util.concurrent.TimeUnit


class SignInFragment : RegistrationActivityFragment(), TextView.OnEditorActionListener {

    private var emailInput: EditText? = null
    private var passwordInput: EditText? = null

    override val fabStringResource: Int
        @StringRes
        get() = R.string.submit

    override val fabIconResource: Int
        @DrawableRes
        get() = R.drawable.ic_check_white_24dp

    override val toolbarTitle: CharSequence
        get() = getString(R.string.sign_in)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_sign_in, container, false)
        val border = rootView.findViewById<View>(R.id.card_view_wrapper)
        emailInput = rootView.findViewById(R.id.email)
        passwordInput = rootView.findViewById(R.id.password)

        rootView.findViewById<View>(R.id.forgot_password).setOnClickListener(this)
        passwordInput!!.setOnEditorActionListener(this)

        ViewCompat.setTransitionName(emailInput!!, SplashFragment.TRANSITION_TITLE)
        ViewCompat.setTransitionName(passwordInput!!, SplashFragment.TRANSITION_SUBTITLE)
        ViewCompat.setTransitionName(border, SplashFragment.TRANSITION_BACKGROUND)

        disposables.add(Completable.timer(200, TimeUnit.MILLISECONDS).observeOn(mainThread()).subscribe({
            rootView.findViewById<View>(R.id.email_wrapper).visibility = View.VISIBLE
            rootView.findViewById<View>(R.id.password_wrapper).visibility = View.VISIBLE
        }, ErrorHandler.EMPTY::invoke))

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        emailInput = null
        passwordInput = null
    }

    override val showsFab: Boolean
        get() {
            return true
        }

    @SuppressLint("CommitTransaction")
    override fun provideFragmentTransaction(fragmentTo: BaseFragment): FragmentTransaction? =
            if (view != null && fragmentTo.stableTag.contains(ForgotPasswordFragment::class.java.simpleName)) beginTransaction()
                    .addSharedElement(emailInput!!, SplashFragment.TRANSITION_TITLE)
                    .addSharedElement(view!!.findViewById(R.id.card_view_wrapper), SplashFragment.TRANSITION_BACKGROUND)
            else super.provideFragmentTransaction(fragmentTo)

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> signIn()
            R.id.forgot_password -> showFragment(ForgotPasswordFragment.newInstance(emailInput!!.text))
        }
    }

    override fun onEditorAction(textView: TextView, actionId: Int, event: KeyEvent): Boolean = when {
        actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN -> signIn().let { true }
        else -> false
    }

    private fun signIn() {
        if (emailInput.hasValidEmail && passwordInput.hasValidPassword) {
            toggleProgress(true)

            val email = emailInput!!.text.toString()
            val password = passwordInput!!.text.toString()

            disposables.add(viewModel.signIn(email, password)
                    .subscribe({ onSignIn() }, defaultErrorHandler::invoke)
            )
        }
    }

    private fun onSignIn() {
        toggleProgress(false)
        hideKeyboard()
        RegistrationActivity.startMainActivity(activity)
    }

    companion object {

        fun newInstance(): SignInFragment = SignInFragment().apply {
            arguments = Bundle()
            setEnterExitTransitions()
        }
    }
}
