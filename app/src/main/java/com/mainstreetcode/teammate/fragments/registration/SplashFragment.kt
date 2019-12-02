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
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.baseclasses.setSimpleSharedTransitions
import com.mainstreetcode.teammate.databinding.FragmentSplashBinding
import com.tunjid.androidx.core.content.colorAt
import com.tunjid.androidx.core.text.appendNewLine
import com.tunjid.androidx.core.text.color
import com.tunjid.androidx.core.text.underline
import com.tunjid.androidx.view.util.InsetFlags

/**
 * Splash screen
 */

class SplashFragment : TeammatesBaseFragment(R.layout.fragment_splash), View.OnClickListener {

    override val insetFlags: InsetFlags
        get() = NO_TOP

    private val faceBookResultCallback = CallbackManager.Factory.create()

    private val facebookCallback = object : FacebookCallback<LoginResult> {
        override fun onSuccess(loginResult: LoginResult) {
            transientBarDriver.toggleProgress(true)
            disposables.add(userViewModel.signIn(loginResult)
                    .subscribe({ navigator.completeSignIn() }, defaultErrorHandler::invoke))
        }

        override fun onCancel() {
            transientBarDriver.toggleProgress(false)
            transientBarDriver.showSnackBar(getString(R.string.cancelled))
        }

        override fun onError(error: FacebookException) {
            transientBarDriver.toggleProgress(false)
            transientBarDriver.showSnackBar(getString(R.string.error_default))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        defaultUi(
                fabShows = false,
                toolbarShows = false,
                bottomNavShows = false,
                grassShows = true,
                navBarColor = GRASS_COLOR
        )

        FragmentSplashBinding.bind(view).apply {
            login.apply {
                setOnClickListener(this@SplashFragment)
                text = SpannableStringBuilder(getString(R.string.login_have_account))
                        .appendNewLine()
                        .append(getString(R.string.login_sign_in).color(context.colorAt(R.color.white)).underline())
            }

            facebookLogin.apply { setOnClickListener(this@SplashFragment) }
            emailSignUp.apply { setOnClickListener(this@SplashFragment) }

            title.transitionName = TRANSITION_TITLE
            subTitle.transitionName = TRANSITION_SUBTITLE
            border.transitionName = TRANSITION_BACKGROUND
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        LoginManager.getInstance().unregisterCallback(faceBookResultCallback)
    }

    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) {
        val root = view
        if (root != null) {
            if (incomingFragment is SignInFragment) {
                incomingFragment.setSimpleSharedTransitions()
                transaction
                        .addSharedElement(root.findViewById(R.id.border), TRANSITION_BACKGROUND)
                        .addSharedElement(root.findViewById(R.id.title), TRANSITION_TITLE)
                        .addSharedElement(root.findViewById(R.id.sub_title), TRANSITION_SUBTITLE)
            } else if (incomingFragment is SignUpFragment) {
                incomingFragment.setSimpleSharedTransitions()
                transaction
                        .addSharedElement(root.findViewById(R.id.border), TRANSITION_BACKGROUND)
                        .addSharedElement(root.findViewById(R.id.title), TRANSITION_TITLE)
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.email_sign_up -> navigator.push(SignUpFragment.newInstance())
            R.id.login -> navigator.push(SignInFragment.newInstance())
            R.id.facebook_login -> {
                val loginManager = LoginManager.getInstance()
                loginManager.registerCallback(faceBookResultCallback, facebookCallback)
                loginManager.logInWithReadPermissions(this, FACEBOOK_PERMISSIONS)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Forward callbacks to social network SDKs
        faceBookResultCallback.onActivityResult(requestCode, resultCode, data)
    }

    companion object {

        internal const val TRANSITION_BACKGROUND = "transition-background"
        internal const val TRANSITION_TITLE = "transition-title"
        internal const val TRANSITION_SUBTITLE = "transition-subtitle"

        private val FACEBOOK_PERMISSIONS = listOf("email", "public_profile")

        fun newInstance(): SplashFragment {
            val fragment = SplashFragment()
            val args = Bundle()

            fragment.arguments = args
            return fragment
        }
    }
}
