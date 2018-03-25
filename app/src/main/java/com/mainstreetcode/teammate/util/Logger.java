package com.mainstreetcode.teammate.util;


import android.util.Log;

public class Logger {

    public static void log(String source, String message, Throwable e) {
        Log.e(source, message, e);
    }
}
