package com.ch_linghu.fanfoudroid.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;

import android.util.Log;

import com.ch_linghu.fanfoudroid.TwitterApplication;

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
    public static long between(String tag, int startOrEnd) {
        if(TwitterApplication.DEBUG){
            Log.v("DEBUG", tag + " " + startOrEnd);
        }
        switch (startOrEnd) {
            case START:
                return mark(tag);
            case END:
                long time = System.currentTimeMillis() - mStartTime - get(tag, mStartTime);
                mTime.put(tag, time);
                //touch();
                return time;
            default:
                return -1;
        }
    }
    public static long betweenStart(String tag) {
        return between(tag, START);
    }
    public static long betweenEnd(String tag) {
        return between(tag, END);
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
    
    public static DebugProfile[] getProfile() {
        DebugProfile[] profile = new DebugProfile[mTime.size()];
        
        long totel = mTime.get("_TOTLE");
        
        int i = 0;
        for (String key : mTime.keySet()) {
            long time = mTime.get(key);
            profile[i] = new DebugProfile(key, time, time/(totel*1.0) );
            i++;
        }
        
        try {
            Arrays.sort(profile);
        } catch (NullPointerException e) {
            // in case item is null, do nothing
        }
        return profile;
    }
    
    public static String getProfileAsString() {
        StringBuilder sb = new StringBuilder();
        for (DebugProfile p : getProfile()) {
            sb.append("TAG: ");
            sb.append(p.tag);
            sb.append("\t INC: ");
            sb.append(p.inc);
            sb.append("\t INCP: ");
            sb.append(p.incPercent);
            sb.append("\n");
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return __toString();
    }
    
   
}

class DebugProfile implements Comparable<DebugProfile> {
    private static NumberFormat percent = NumberFormat.getPercentInstance();
    
    public String tag;
    public long inc;
    public String incPercent;
    
    public DebugProfile(String tag, long inc, double incPercent) {
        this.tag = tag;
        this.inc = inc;
        
        percent = new DecimalFormat("0.00#%");
        this.incPercent = percent.format(incPercent);
    }

    @Override
    public int compareTo(DebugProfile o) {
        // TODO Auto-generated method stub
        return (int) (o.inc - this.inc);
    }
    
}
