package com.mainstreetcode.teammates;

import com.google.android.gms.ads.MobileAds;
import com.mainstreetcode.teammates.repository.UserRepository;
import com.mainstreetcode.teammates.util.ErrorHandler;

/**
 * Application Singleton
 */

public class App extends android.app.Application {

    static App INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        MobileAds.initialize(this, getString(R.string.admob_app_id));

        // Load user from cache if they exist
        UserRepository.getInstance().getMe().subscribe(ignored -> {}, ErrorHandler.EMPTY);
    }

    public static App getInstance() {
        return INSTANCE;
    }
}
