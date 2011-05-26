package com.temp.afan;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Application Preferences
 */
public class Preferences {
    private static final String PREFERENCES_FILE = "fanfoudroid";
    
    // Preferences field names
    public static final String ENABLE_DEBUG="debug";
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String CHECK_UPDATES_KEY = "check_updates";
    public static final String CHECK_UPDATE_INTERVAL_KEY = "check_update_interval";
    public static final String VIBRATE_KEY = "vibrate";
    public static final String TIMELINE_ONLY_KEY = "timeline_only";
    public static final String REPLIES_ONLY_KEY = "replies_only";
    public static final String DM_ONLY_KEY = "dm_only";
    public static final String RINGTONE_KEY = "ringtone";
    public static final String RINGTONE_DEFAULT_KEY = "content://settings/system/notification_sound";  
    public static final String LAST_TWEET_REFRESH_KEY = "last_tweet_refresh";
    public static final String LAST_DM_REFRESH_KEY = "last_dm_refresh";
    public static final String LAST_FOLLOWERS_REFRESH_KEY = "last_followers_refresh";
    public static final String TWITTER_ACTIVITY_STATE_KEY = "twitter_activity_state";
    public static final String USE_PROFILE_IMAGE = "use_profile_image";
    public static final String PHOTO_PREVIEW = "photo_preview";
    public static final String FORCE_SHOW_ALL_IMAGE = "force_show_all_image";
    public static final String RT_PREFIX_KEY = "rt_prefix";
    public static final String RT_INSERT_APPEND = "rt_insert_append"; //转发时光标放置在开始还是结尾 
    public static final String NETWORK_TYPE = "network_type";
    public static final String CURRENT_USER_ID="current_user_id";
    public static final String CURRENT_USER_SCREEN_NAME="current_user_screenname";
    public static final String UI_FONT_SIZE = "ui_font_size";
    public static final String USE_GESTRUE = "use_gestrue";

    private static Preferences sInstance;
    
    final SharedPreferences mSharedPreferences;

    private Preferences(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFERENCES_FILE,
                Context.MODE_PRIVATE);
    }

    /**
     * Get Preferences
     * 
     * @param context
     * @return self
     */
    public static synchronized Preferences getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Preferences(context);
        }
        return sInstance;
    }
    
    /**
     * Get low-level Preferences
     * @return
     */
    public SharedPreferences getPreferences() {
        return mSharedPreferences;
    }

    /**
     * Enable debug logging
     * 
     * @param value
     */
    public void setEnableDebugLogging(boolean value) {
        mSharedPreferences.edit().putBoolean(ENABLE_DEBUG, value).commit();
    }

    /**
     * Whether logging is enabled
     * @return
     */
    public boolean getEnableDebugLogging() {
        return mSharedPreferences.getBoolean(ENABLE_DEBUG, false);
    }

    /**
     * Clear All Preferences for this session
     */
    public void clear() {
        mSharedPreferences.edit().clear().commit();
    }
}
