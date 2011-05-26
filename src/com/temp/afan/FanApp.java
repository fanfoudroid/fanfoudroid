package com.temp.afan;

import android.app.Application;

/**
 * FanfouDroid Application
 */
public class FanApp extends Application {
    public static final String TAG = "Fanfoudroid";
    
    /**
     * Whether debug logging is enabled 
     */
    public static boolean DEBUG = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Preferences prefs = Preferences.getInstance(this);
        DEBUG = prefs.getEnableDebugLogging();
    }

}
