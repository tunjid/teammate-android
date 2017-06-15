package com.mainstreetcode.teammates;

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
    }

    public static Application getInstance() {
        return INSTANCE;
    }
}
