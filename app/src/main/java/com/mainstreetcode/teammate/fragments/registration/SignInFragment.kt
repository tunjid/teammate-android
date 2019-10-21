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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.baseclasses.setSimpleSharedTransitions
import com.mainstreetcode.teammate.databinding.FragmentSignInBinding
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.hasValidEmail
import com.mainstreetcode.teammate.util.hasValidPassword
import com.mainstreetcode.teammate.util.input
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import java.util.concurrent.TimeUnit


class SignInFragment : TeammatesBaseFragment(R.layout.fragment_sign_in), TextView.OnEditorActionListener {

    private var binding: FragmentSignInBinding? = null

    override val showsFab: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultUi(
                fabText = R.string.submit,
                fabIcon = R.drawable.ic_check_white_24dp,
                fabShows = true,
                toolbarShows = false,
                bottomNavShows = false,
                grassShows = true,
                navBarColor = GRASS_COLOR
        )

        binding = FragmentSignInBinding.bind(view).apply {
            cardViewWrapper.transitionName = SplashFragment.TRANSITION_BACKGROUND
            password.transitionName = SplashFragment.TRANSITION_SUBTITLE
            email.transitionName = SplashFragment.TRANSITION_TITLE

            password.setOnEditorActionListener(this@SignInFragment)
            forgotPassword.setOnClickListener(this@SignInFragment)

            disposables.add(Completable.timer(200, TimeUnit.MILLISECONDS).observeOn(mainThread()).subscribe({
                emailWrapper.visibility = View.VISIBLE
                passwordWrapper.visibility = View.VISIBLE
            }, ErrorHandler.EMPTY::invoke))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) {
        val binding = this.binding ?: return

        if (view != null && incomingFragment is ForgotPasswordFragment) {
            incomingFragment.setSimpleSharedTransitions()
            transaction
                    .addSharedElement(binding.email, SplashFragment.TRANSITION_TITLE)
                    .addSharedElement(binding.cardViewWrapper, SplashFragment.TRANSITION_BACKGROUND)
        } else super.augmentTransaction(transaction, incomingFragment)
    }

    override fun onClick(view: View) {
        val binding = this.binding ?: return
        when (view.id) {
            R.id.fab -> signIn()
            R.id.forgot_password -> navigator.push(ForgotPasswordFragment.newInstance(binding.email.input))
        }
    }

    override fun onEditorAction(textView: TextView, actionId: Int, event: KeyEvent): Boolean = when {
        actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN -> signIn().let { true }
        else -> false
    }

    private fun signIn() {
        val binding = this.binding ?: return

        if (binding.email.hasValidEmail && binding.password.hasValidPassword) {
            transientBarDriver.toggleProgress(true)

            val email = binding.email.input.toString()
            val password = binding.password.input.toString()

            disposables.add(userViewModel.signIn(email, password)
                    .subscribe({ onSignIn() }, defaultErrorHandler::invoke)
            )
        }
    }

    private fun onSignIn() {
        transientBarDriver.toggleProgress(false)
        hideKeyboard()
        navigator.completeSignIn()
    }

    companion object {

        fun newInstance(): SignInFragment = SignInFragment().apply {
            arguments = Bundle()
        }
    }
}
