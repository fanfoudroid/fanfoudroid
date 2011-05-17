package com.ch_linghu.fanfoudroid.debug;

import java.util.HashMap;

/**
 * Debug Timer
 * 
 * Usage:
 * --------------------------------
 * DebugTimer.start();
 * DebugTimer.mark("my_mark1"); // optional
 * DebugTimer.stop();
 * 
 * System.out.println(DebugTimer.__toString()); // get report
 * --------------------------------
 * 
 * @author LDS
 */
public class DebugTimer {
    public static final int START = 0;
    public static final int END = 1;
    
    private static HashMap<String, Long> mTime = new HashMap<String, Long>();
    private static long mStartTime = 0;
    private static long mLastTime = 0;
    
    /**
     * Start a timer
     */
    public static void start() {
        reset();
        mStartTime = touch();
    }
    
    /**
     * Mark current time
     * 
     * @param tag mark tag
     * @return
     */
    public static long mark(String tag) {
        long time = System.currentTimeMillis() - mStartTime;
        mTime.put(tag, time);
        return time;
    }
    
    /**
     * Mark current time
     * 
     * @param tag mark tag
     * @return
     */
    public static long between(String tag, boolean isEnd) {
        //Log.v("DEBUG", tag + ((!isEnd) ? " Start" : " End"));
        if (!isEnd) {
            return mark(tag);
        } else {
            long time = System.currentTimeMillis() - mStartTime
                    - get(tag, mStartTime);
            mTime.put(tag, time);
            // touch();
            return time;
        }
    }
    public static long betweenStart(String tag) {
        return between(tag, false);
    }
    public static long betweenEnd(String tag) {
        return between(tag, true);
    }
    
    /**
     * Stop timer
     * 
     * @return result
     */
    public static String stop() {
        mTime.put("_TOTLE", touch() - mStartTime);
        return __toString();
    }
    
    public static String stop(String tag) {
        mark(tag);
        return stop();
    }
    
    /**
     * Get a mark time
     * 
     * @param tag mark tag
     * @return time(milliseconds) or NULL
     */
    public static long get(String tag) {
        return get(tag, 0);
    }
    
    public static long get(String tag, long defaultValue) {
        if (mTime.containsKey(tag)) {
            return mTime.get(tag);
        }
        return defaultValue;
    }
    
    /**
     * Reset timer
     */
    public static void reset() {
        mTime = new HashMap<String, Long>();
        mStartTime = 0;
        setLastTime(0);
    }

    /**
     * static toString()
     * 
     * @return
     */
    public static String __toString() {
        return "Debuger [time =" + mTime.toString() + "]";
    }
    
    private static long touch() {
        return setLastTime(System.currentTimeMillis());
    }
    
    public static TimerProfile getProfile() {
        TimerProfile profile = new TimerProfile();
        
        long totel = mTime.get("_TOTLE");
        
        int i = 0;
        for (String key : mTime.keySet()) {
            long time = mTime.get(key);
            profile.writeRow(key, time, time/(totel*1.0) );
            i++;
        }
        
        return profile;
    }
    
    public static String getProfileAsString() {
        TimerProfile profile = getProfile();
        ProfileFormater formater = new PlainTextFormater();
        return formater.format(profile);
    }
    
    @Override
    public String toString() {
        return __toString();
    }

    public static long setLastTime(long mLastTime) {
        DebugTimer.mLastTime = mLastTime;
        return mLastTime;
    }

    public static long getLastTime() {
        return mLastTime;
    }
}

class TimerProfile extends DebugProfile {

    public TimerProfile() {
        super();
        // TODO Auto-generated constructor stub
    }
}
