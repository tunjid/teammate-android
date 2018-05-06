package com.mainstreetcode.teammate;

import android.annotation.SuppressLint;
import android.support.multidex.MultiDexApplication;

import com.mainstreetcode.teammate.repository.ConfigRepository;
import com.mainstreetcode.teammate.repository.RoleRepository;
import com.mainstreetcode.teammate.repository.UserRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;

/**
 * Application Singleton
 */

public class App extends MultiDexApplication {

    static App INSTANCE;

    @Override
    @SuppressLint("CheckResult")
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
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
}
