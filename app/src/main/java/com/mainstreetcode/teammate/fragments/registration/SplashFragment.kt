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
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentTransaction
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.activities.RegistrationActivity
import com.mainstreetcode.teammate.baseclasses.RegistrationActivityFragment
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.core.text.SpanBuilder
import com.tunjid.androidbootstrap.view.util.InsetFlags

/**
 * Splash screen
 */

class SplashFragment : RegistrationActivityFragment(), View.OnClickListener {

    private val faceBookResultCallback = CallbackManager.Factory.create()
    private val facebookCallback = object : FacebookCallback<LoginResult> {
        override fun onSuccess(loginResult: LoginResult) {
            toggleProgress(true)
            disposables.add(viewModel.signIn(loginResult)
                    .subscribe({ RegistrationActivity.startMainActivity(activity) }, defaultErrorHandler::accept))
        }

        override fun onCancel() {
            toggleProgress(false)
            showSnackbar(getString(R.string.cancelled))
        }

        override fun onError(error: FacebookException) {
            toggleProgress(false)
            showSnackbar(getString(R.string.error_default))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_splash, container, false)
        val facebookSignUp = rootView.findViewById<TextView>(R.id.facebook_login)
        val emailSignUp = rootView.findViewById<TextView>(R.id.email_sign_up)
        val login = rootView.findViewById<TextView>(R.id.login)

        val context = rootView.context

        login.text = SpanBuilder.of(getString(R.string.login_have_account))
                .appendNewLine()
                .append(SpanBuilder.of(getString(R.string.login_sign_in))
                        .color(context, R.color.white)
                        .underline()
                        .build())
                .build()

        facebookSignUp.setOnClickListener(this)
        emailSignUp.setOnClickListener(this)
        login.setOnClickListener(this)

        ViewCompat.setTransitionName(rootView.findViewById(R.id.title), TRANSITION_TITLE)
        ViewCompat.setTransitionName(rootView.findViewById(R.id.sub_title), TRANSITION_SUBTITLE)
        ViewCompat.setTransitionName(rootView.findViewById(R.id.border), TRANSITION_BACKGROUND)
        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        LoginManager.getInstance().unregisterCallback(faceBookResultCallback)
    }

    override fun insetFlags(): InsetFlags = NO_TOP

    override fun showsFab(): Boolean = false

    override fun showsToolBar(): Boolean = false

    @SuppressLint("CommitTransaction")
    override fun provideFragmentTransaction(fragmentTo: BaseFragment): FragmentTransaction? {
        val rootView = view
        if (rootView != null) {
            if (fragmentTo.stableTag.contains(SignInFragment::class.java.simpleName)) {
                return beginTransaction()
                        .addSharedElement(rootView.findViewById(R.id.border), TRANSITION_BACKGROUND)
                        .addSharedElement(rootView.findViewById(R.id.title), TRANSITION_TITLE)
                        .addSharedElement(rootView.findViewById(R.id.sub_title), TRANSITION_SUBTITLE)
            } else if (fragmentTo.stableTag.contains(SignUpFragment::class.java.simpleName)) {
                return beginTransaction()
                        .addSharedElement(rootView.findViewById(R.id.border), TRANSITION_BACKGROUND)
                        .addSharedElement(rootView.findViewById(R.id.title), TRANSITION_TITLE)
            }
        }
        return super.provideFragmentTransaction(fragmentTo)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.email_sign_up -> showFragment(SignUpFragment.newInstance())
            R.id.login -> showFragment(SignInFragment.newInstance())
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
