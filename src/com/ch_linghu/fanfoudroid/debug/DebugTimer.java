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
    private static HashMap<String, Long> mTime = new HashMap<String, Long>();
    private static long mStartTime = 0;
    private static long mLastTime = 0;
    
    /**
     * Start a timer
     */
    public static void start() {
        mStartTime = touch();
    }
    
    /**
     * Mark current time
     * 
     * @param tag mark tag
     * @return
     */
    public static long mark(String tag) {
        long time = System.currentTimeMillis() - mLastTime;
        touch();
        mTime.put(tag, time);
        return time;
    }
    
    /**
     * Stop timer
     * 
     * @return result
     */
    public static String stop() {
        mTime.put("_TOTAL", touch() - mStartTime);
        return __toString();
    }
    
    /**
     * Get a mark time
     * 
     * @param tag mark tag
     * @return time(milliseconds) or NULL
     */
    public static long get(String tag) {
        return mTime.get(tag);
    }
    
    /**
     * Reset timer
     */
    public static void reset() {
        mTime = new HashMap<String, Long>();
        mStartTime = 0;
        mLastTime = 0;
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
        return mLastTime = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return __toString();
    }
}
