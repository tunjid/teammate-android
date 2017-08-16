package com.mainstreetcode.teammates;

import com.facebook.stetho.Stetho;
import com.mainstreetcode.teammates.repository.UserRepository;
import com.mainstreetcode.teammates.util.ErrorHandler;

/**
 * Application Singleton
 * <p>
 * Created by Shemanigans on 6/14/17.
 */

public class Application extends android.app.Application {

    static Application INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        Stetho.initializeWithDefaults(this);

        // Load user from cache if they exist
        UserRepository.getInstance().getMe().subscribe(ignored -> {}, ErrorHandler.EMPTY);
    }

    public static Application getInstance() {
        return INSTANCE;
    }
}
