package io.github.dkbai.tinyhttpd.nanohttpd.core.util;

import android.util.Log;

import java.util.logging.Level;

/**
 * @author X
 * @version 0.1
 */

public class Logger {

    private final String TAG;
    public static boolean disableDebug = false;

    public static Logger getLogger(String name) {
        return new Logger(name);
    }

    @SuppressWarnings("WrongConstant")
    public void log(Level level, String msg, Throwable thrown) {
        if (disableDebug) return;
        Log.println(level(level), TAG, msg + "\n" + Log.getStackTraceString(thrown));
    }

    @SuppressWarnings("WrongConstant")
    public void log(Level level, String msg) {
        if (disableDebug) return;
        Log.println(level(level), TAG, msg);
    }

    private int level(Level level) {

        int value = level.intValue();
        
        if (value == Level.INFO.intValue()) {
            return Log.INFO;
        } else if (value == Level.WARNING.intValue()) {
            return Log.WARN;
        } else if (value == Level.SEVERE.intValue()) {
            return Log.ERROR;
        } else if (value == Level.CONFIG.intValue()) {
            return Log.DEBUG;
        } else if (value == Level.ALL.intValue()
                || value == Level.FINEST.intValue()
                || value == Level.FINER.intValue()
                || value == Level.FINE.intValue()) {
            return Log.VERBOSE;
        } else {
            return Log.VERBOSE;
        }

    }

    private Logger(String tag) {
        TAG = tag;
    }

}
