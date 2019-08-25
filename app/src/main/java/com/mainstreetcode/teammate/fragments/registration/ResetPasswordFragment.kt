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
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.baseclasses.RegistrationActivityFragment
import com.mainstreetcode.teammate.util.hasValidEmail

/**
 * Forgot password screen
 */

class ResetPasswordFragment : RegistrationActivityFragment(), TextView.OnEditorActionListener {

    private var emailInput: EditText? = null
    private var tokenInput: EditText? = null
    private var passwordInput: EditText? = null

    override val fabStringResource: Int
        @StringRes
        get() = R.string.submit

    override val fabIconResource: Int
        @DrawableRes
        get() = R.drawable.ic_check_white_24dp

    override val toolbarTitle: CharSequence
        get() = getString(R.string.sign_in_forgot_password)

    override fun getStableTag(): String {
        return arguments!!.getCharSequence(ARG_TOKEN, "").toString()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_reset_password, container, false)
        val border = rootView.findViewById<View>(R.id.card_view_wrapper)

        emailInput = rootView.findViewById(R.id.email)
        tokenInput = rootView.findViewById(R.id.token)
        passwordInput = rootView.findViewById(R.id.password)

        val args = arguments

        if (args != null) tokenInput!!.setText(args.getCharSequence(ARG_TOKEN, ""))
        emailInput!!.setOnEditorActionListener(this)

        ViewCompat.setTransitionName(emailInput!!, SplashFragment.TRANSITION_TITLE)
        ViewCompat.setTransitionName(border, SplashFragment.TRANSITION_BACKGROUND)

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        emailInput = null
    }

    override val showsFab: Boolean
        get() {
            return true
        }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> resetPassword()
        }
    }

    override fun onEditorAction(textView: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
            resetPassword()
            return true
        }
        return false
    }

    private fun resetPassword() {
        if (emailInput.hasValidEmail) {
            toggleProgress(true)

            val email = emailInput!!.text.toString()
            val token = tokenInput!!.text.toString()
            val password = passwordInput!!.text.toString()

            disposables.add(viewModel.resetPassword(email, token, password)
                    .subscribe({
                        showSnackbar { snackbar ->
                            snackbar.setText(it.message)
                                    .setAction(R.string.sign_in) { showFragment(SignInFragment.newInstance()) }
                        }
                    }, defaultErrorHandler::invoke))
        }
    }

    companion object {

        private const val ARG_TOKEN = "token"

        fun newInstance(token: CharSequence): ResetPasswordFragment {
            val fragment = ResetPasswordFragment()
            val args = Bundle()

            args.putCharSequence(ARG_TOKEN, token)
            fragment.arguments = args
            fragment.setEnterExitTransitions()
            return fragment
        }
    }
}
