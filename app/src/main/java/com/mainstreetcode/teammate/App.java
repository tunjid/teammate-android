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

package com.mainstreetcode.teammate;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.google.android.libraries.places.api.Places;
import com.mainstreetcode.teammate.repository.ConfigRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.repository.RoleRepo;
import com.mainstreetcode.teammate.repository.UserRepo;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.Logger;
import com.mainstreetcode.teammate.viewmodel.events.Alert;

import androidx.annotation.Nullable;
import androidx.core.provider.FontRequest;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.FontRequestEmojiCompatConfig;
import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;

/**
 * Application Singleton
 */

public class App extends Application {

    private static final String PROVIDER_AUTHORITY = "com.google.android.gms.fonts";
    private static final String PROVIDER_PACKAGE = "com.google.android.gms";
    private static final String EMOJI_QUERY = "Noto Color Emoji Compat";

    private static final String EMOJI_INIT_TAG = "EmojiCompatInit";
    private static final String EMOJI_GET_TAG = "EmojiCompatGet";

    static App INSTANCE;

    private final PublishProcessor<Alert> eventSource = PublishProcessor.create();

    private final BroadcastReceiver mediaDownloadListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            MediaTransferIntentService.getDownloadStats().onDownloadComplete(downloadId);
        }
    };

    public static App getInstance() { return INSTANCE; }

    @Override
    @SuppressLint("CheckResult")
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        initializeEmoji();
        registerReceiver(mediaDownloadListener, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        //MobileAds.initialize(this, getString(R.string.admob_app_id));
        Places.initialize(getApplicationContext(), getString(R.string.google_api_key));
    }

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void prime() {
        // Load user from cache if they exist
        UserRepo userRepository = RepoProvider.forRepo(UserRepo.class);
        if (!userRepository.isSignedIn()) return;

        userRepository.getMe()
                .lastOrError()
                .flatMap(ignored -> RepoProvider.forRepo(ConfigRepo.class).get("").lastOrError())
                .flatMap(ignored -> RepoProvider.forRepo(RoleRepo.class).getMyRoles().lastOrError())
                .subscribe(ignored -> {}, ErrorHandler.EMPTY);
    }

   public void pushAlert(Alert alert) { eventSource.onNext(alert); }

   public Flowable<Alert> alerts() { return eventSource; }

    private void initializeEmoji() {
        // Use a downloadable font for EmojiCompat
        FontRequest fontRequest = new FontRequest(
                PROVIDER_AUTHORITY,
                PROVIDER_PACKAGE,
                EMOJI_QUERY,
                R.array.com_google_android_gms_fonts_certs);

        EmojiCompat.Config config = new FontRequestEmojiCompatConfig(getApplicationContext(), fontRequest)
                .setReplaceAll(true)
                .registerInitCallback(getInitCallBack(EMOJI_INIT_TAG));

        EmojiCompat.init(config);
        EmojiCompat.get().registerInitCallback(getInitCallBack(EMOJI_GET_TAG));
    }

    private EmojiCompat.InitCallback getInitCallBack(String tag) {
        return new EmojiCompat.InitCallback() {
            @Override
            public void onInitialized() {
                Logger.log(tag, "EmojiCompat initialized");
            }

            @Override
            public void onFailed(@Nullable Throwable throwable) {
                Logger.log(tag, "EmojiCompat initialization failed", throwable);
            }
        };
    }
}
