package com.mainstreetcode.teammate.util;


import android.util.Log;

import com.mainstreetcode.teammate.BuildConfig;

public class Logger {

    public static void log(String source, String message, Throwable e) {
        if (BuildConfig.DEV) Log.e(source, message, e);
    }

    public static void log(String source, String message) {
        if (BuildConfig.DEV) Log.e(source, message);
    }
}
