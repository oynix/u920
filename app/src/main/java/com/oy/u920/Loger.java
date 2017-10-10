package com.oy.u920;

import android.util.Log;

/**
 * Author   : xiaoyu
 * Date     : 2017/9/22 8:56
 * Describe :
 */

public final class Loger {

    private Loger() {
        throw new IllegalStateException("u cannot instantiate me, boy");
    }

    public static void v(String tag, String log) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, log);
        }
    }

    public static void i(String tag, String log) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, log);
        }
    }

    public static void d(String tag, String log) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, log);
        }
    }

    public static void w(String tag, String log) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, log);
        }
    }

    public static void e(String tag, String log) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, log);
        }
    }
}
