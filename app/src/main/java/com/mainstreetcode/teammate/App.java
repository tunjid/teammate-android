package com.mainstreetcode.teammate;

import android.support.multidex.MultiDexApplication;

import com.mainstreetcode.teammate.repository.RoleRepository;
import com.mainstreetcode.teammate.repository.UserRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;

/**
 * Application Singleton
 */

public class App extends MultiDexApplication {

    static App INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        //MobileAds.initialize(this, getString(R.string.admob_app_id));

        // Load user from cache if they exist
        UserRepository userRepository = UserRepository.getInstance();
        if (!userRepository.isSignedIn()) return;

        userRepository.getMe()
                .lastOrError()
                .flatMap(user -> RoleRepository.getInstance().getMyRoles().lastOrError())
                .subscribe(ignored -> {}, ErrorHandler.EMPTY);
    }

    public static App getInstance() {
        return INSTANCE;
    }
}
