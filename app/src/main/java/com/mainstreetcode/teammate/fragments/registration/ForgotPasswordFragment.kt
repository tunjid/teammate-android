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
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.databinding.FragmentForgotPasswordBinding
import com.mainstreetcode.teammate.util.hasValidEmail
import com.mainstreetcode.teammate.util.input

/**
 * Forgot password screen
 */

class ForgotPasswordFragment : MainActivityFragment(R.layout.fragment_forgot_password), TextView.OnEditorActionListener {

    private var binding: FragmentForgotPasswordBinding? = null

    override val showsFab: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = FragmentForgotPasswordBinding.bind(view).run {
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

        val args = arguments

        if (args != null) email.setText(args.getCharSequence(ARG_EMAIL, ""))
        email.setOnEditorActionListener(this@ForgotPasswordFragment)

        email.transitionName = SplashFragment.TRANSITION_TITLE
        cardViewWrapper.transitionName = SplashFragment.TRANSITION_BACKGROUND
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onClick(view: View) = when (view.id) {
        R.id.fab -> sendForgotEmail()
        else -> Unit
    }

    override fun onEditorAction(textView: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
            sendForgotEmail()
            return true
        }
        return false
    }

    private fun sendForgotEmail() {
        val binding = this.binding ?: return
        if (binding.email.hasValidEmail) {
            transientBarDriver.toggleProgress(true)

            val email = binding.email.input.toString()

            disposables.add(userViewModel.forgotPassword(email)
                    .subscribe({ transientBarDriver.showSnackBar(it.message) }, defaultErrorHandler::invoke))
        }
    }

    companion object {

        private const val ARG_EMAIL = "email"

        fun newInstance(email: CharSequence): ForgotPasswordFragment = ForgotPasswordFragment().apply {
            arguments = bundleOf(ARG_EMAIL to email)
            setEnterExitTransitions()
        }
    }
}
