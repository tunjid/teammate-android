package com.mainstreetcode.teammates;

import com.facebook.stetho.Stetho;

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
    }

    public static Application getInstance() {
        return INSTANCE;
    }
}
