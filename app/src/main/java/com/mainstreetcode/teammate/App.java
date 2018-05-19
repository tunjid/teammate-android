package com.mainstreetcode.teammate;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.FontRequestEmojiCompatConfig;
import android.support.v4.provider.FontRequest;

import com.mainstreetcode.teammate.repository.ConfigRepository;
import com.mainstreetcode.teammate.repository.RoleRepository;
import com.mainstreetcode.teammate.repository.UserRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.Logger;

/**
 * Application Singleton
 */

public class App extends MultiDexApplication {

    private static final String PROVIDER_AUTHORITY = "com.google.android.gms.fonts";
    private static final String PROVIDER_PACKAGE = "com.google.android.gms";
    private static final String EMOJI_QUERY = "Noto Color Emoji Compat";

    private static final String EMOJI_INIT_TAG = "EmojiCompatInit";
    private static final String EMOJI_GET_TAG = "EmojiCompatGet";

    static App INSTANCE;

    @Override
    @SuppressLint("CheckResult")
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        initializeEmoji();
        //MobileAds.initialize(this, getString(R.string.admob_app_id));
    }

    public static App getInstance() {
        return INSTANCE;
    }

    @SuppressLint("CheckResult")
    public static void prime() {
        // Load user from cache if they exist
        UserRepository userRepository = UserRepository.getInstance();
        if (!userRepository.isSignedIn()) return;

        userRepository.getMe()
                .lastOrError()
                .flatMap(ignored -> ConfigRepository.getInstance().get("").lastOrError())
                .flatMap(ignored -> RoleRepository.getInstance().getMyRoles().lastOrError())
                .subscribe(ignored -> {}, ErrorHandler.EMPTY);
    }

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
