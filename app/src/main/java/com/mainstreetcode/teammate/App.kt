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

package com.mainstreetcode.teammate

import android.annotation.SuppressLint
import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import com.google.android.libraries.places.api.Places
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.repository.ConfigRepo
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.repository.RoleRepo
import com.mainstreetcode.teammate.repository.UserRepo
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.Logger
import com.mainstreetcode.teammate.viewmodel.events.Alert
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

/**
 * Application Singleton
 */

class App : Application() {

    private val eventSource = PublishProcessor.create<Alert<*>>()

    private val mediaDownloadListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
            MediaTransferIntentService.downloadStats.onDownloadComplete(downloadId)
        }
    }

    @SuppressLint("CheckResult")
    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeEmoji()
        registerReceiver(mediaDownloadListener, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        //MobileAds.initialize(this, getString(R.string.admob_app_id));
        Places.initialize(applicationContext, getString(R.string.google_api_key))
    }

    fun pushAlert(alert: Alert<*>) = eventSource.onNext(alert)

    fun alerts(): Flowable<Alert<*>> = eventSource

    private fun initializeEmoji() {
        // Use a downloadable font for EmojiCompat
        val fontRequest = FontRequest(
                PROVIDER_AUTHORITY,
                PROVIDER_PACKAGE,
                EMOJI_QUERY,
                R.array.com_google_android_gms_fonts_certs)

        val config = FontRequestEmojiCompatConfig(applicationContext, fontRequest)
                .setReplaceAll(true)
                .registerInitCallback(getInitCallBack(EMOJI_INIT_TAG))

        EmojiCompat.init(config)
        EmojiCompat.get().registerInitCallback(getInitCallBack(EMOJI_GET_TAG))
    }

    private fun getInitCallBack(tag: String): EmojiCompat.InitCallback {
        return object : EmojiCompat.InitCallback() {
            override fun onInitialized() = Logger.log(tag, "EmojiCompat initialized")

            override fun onFailed(throwable: Throwable?) {
                if (throwable != null) Logger.log(tag, "EmojiCompat initialization failed", throwable)
            }
        }
    }

    companion object {

        private const val PROVIDER_AUTHORITY = "com.google.android.gms.fonts"
        private const val PROVIDER_PACKAGE = "com.google.android.gms"
        private const val EMOJI_QUERY = "Noto Color Emoji Compat"

        private const val EMOJI_INIT_TAG = "EmojiCompatInit"
        private const val EMOJI_GET_TAG = "EmojiCompatGet"

        lateinit var instance: App
            internal set

        @SuppressLint("CheckResult")
        fun prime() {
            // Load user from cache if they exist
            val userRepository = RepoProvider.forRepo(UserRepo::class.java)
            if (!userRepository.isSignedIn) return

            userRepository.me
                    .lastOrError()
                    .flatMap { RepoProvider.forRepo(ConfigRepo::class.java)[""].lastOrError() }
                    .flatMap { RepoProvider.forRepo(RoleRepo::class.java).myRoles.lastOrError() }
                    .subscribe({ }, ErrorHandler.EMPTY::invoke)
        }
    }
}
