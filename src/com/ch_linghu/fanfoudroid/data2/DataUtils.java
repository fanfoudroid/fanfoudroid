package com.ch_linghu.fanfoudroid.data2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

public class DataUtils {
    static SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
    
    public static Date parseDate(String str, String format) throws ParseException {
        if (str == null || "".equals(str)) {
            return null;
        }
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.parse(str);
    }
    
    public static boolean getBoolean(String key, JSONObject json) throws JSONException {
        String str = json.getString(key);
        if(null == str || "".equals(str)||"null".equals(str)){
            return false;
        }
        return Boolean.valueOf(str);
    }
    
    public static String getString(String key, JSONObject json) throws JSONException {
        String str = json.getString(key);
        if(null == str || "".equals(str)||"null".equals(str)){
            return "";
        }
        return String.valueOf(str);
    }
}
