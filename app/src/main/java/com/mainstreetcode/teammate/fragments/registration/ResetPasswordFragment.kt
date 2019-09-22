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
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.os.bundleOf
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.baseclasses.RegistrationActivityFragment
import com.mainstreetcode.teammate.databinding.FragmentResetPasswordBinding
import com.mainstreetcode.teammate.util.hasValidEmail
import com.mainstreetcode.teammate.util.input

/**
 * Forgot password screen
 */

class ResetPasswordFragment : RegistrationActivityFragment(R.layout.fragment_reset_password), TextView.OnEditorActionListener {

    private var binding: FragmentResetPasswordBinding? = null

    override val stableTag: String
        get() = arguments!!.getCharSequence(ARG_TOKEN, "").toString()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultUi(
                fabText = R.string.submit,
                fabIcon = R.drawable.ic_check_white_24dp,
                fabShows = true,
                toolbarShows = false,
                bottomNavShows = false,
                navBarColor = GRASS_COLOR
        )

        FragmentResetPasswordBinding.bind(view).apply {
            val args = arguments

            if (args != null) token.setText(args.getCharSequence(ARG_TOKEN, ""))
            email.setOnEditorActionListener(this@ResetPasswordFragment)

            email.transitionName = SplashFragment.TRANSITION_TITLE
            cardViewWrapper.transitionName = SplashFragment.TRANSITION_BACKGROUND
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onClick(view: View) = when (view.id) {
        R.id.fab -> resetPassword()
        else -> Unit
    }

    override fun onEditorAction(textView: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
            resetPassword()
            return true
        }
        return false
    }

    private fun resetPassword() {
        val binding = this.binding ?: return
        if (binding.email.hasValidEmail) {
            transientBarDriver.toggleProgress(true)

            val email = binding.email.input.toString()
            val token = binding.token.input.toString()
            val password = binding.password.input.toString()

            disposables.add(viewModel.resetPassword(email, token, password)
                    .subscribe({
                        transientBarDriver.showSnackBar { snackbar ->
                            snackbar.setText(it.message)
                                    .setAction(R.string.sign_in) { navigator.show(SignInFragment.newInstance()) }
                        }
                    }, defaultErrorHandler::invoke))
        }
    }

    companion object {

        private const val ARG_TOKEN = "token"

        fun newInstance(token: CharSequence): ResetPasswordFragment = ResetPasswordFragment().apply {
            arguments = bundleOf(ARG_TOKEN to token)
            setEnterExitTransitions()
        }
    }
}
