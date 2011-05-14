package com.ch_linghu.fanfoudroid.helper.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.db.TwitterDatabase;

public class DateTimeHelper {
    private static final String TAG = "DateTimeHelper";

    // Wed Dec 15 02:53:36 +0000 2010
    public static final DateFormat TWITTER_DATE_FORMATTER = new SimpleDateFormat(
            "E MMM d HH:mm:ss Z yyyy", Locale.US);

    public static final DateFormat TWITTER_SEARCH_API_DATE_FORMATTER = new SimpleDateFormat(
            "E, d MMM yyyy HH:mm:ss Z", Locale.US); // TODO: Z -> z ?

    public static final Date parseDateTime(String dateString) {
        try {
            Log.v(TAG, String.format("in parseDateTime, dateString=%s", dateString));
            return TWITTER_DATE_FORMATTER.parse(dateString);
        } catch (ParseException e) {
            Log.w(TAG, "Could not parse Twitter date string: " + dateString);
            return null;
        }
    }

    // Handle "yyyy-MM-dd'T'HH:mm:ss.SSS" from sqlite
    public static final Date parseDateTimeFromSqlite(String dateString) {
        try {
            Log.v(TAG, String.format("in parseDateTime, dateString=%s", dateString));
            return TwitterDatabase.DB_DATE_FORMATTER.parse(dateString);
        } catch (ParseException e) {
            Log.w(TAG, "Could not parse Twitter date string: " + dateString);
            return null;
        }
    }

    public static final Date parseSearchApiDateTime(String dateString) {
        try {
            return TWITTER_SEARCH_API_DATE_FORMATTER.parse(dateString);
        } catch (ParseException e) {
            Log.w(TAG, "Could not parse Twitter search date string: "
                    + dateString);
            return null;
        }
    }

    public static final DateFormat AGO_FULL_DATE_FORMATTER = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm");

    public static String getRelativeDate(Date date) {
        Date now = new Date();

        String prefix = TwitterApplication.mContext
                .getString(R.string.tweet_created_at_beautify_prefix);
        String sec = TwitterApplication.mContext
                .getString(R.string.tweet_created_at_beautify_sec);
        String min = TwitterApplication.mContext
                .getString(R.string.tweet_created_at_beautify_min);
        String hour = TwitterApplication.mContext
                .getString(R.string.tweet_created_at_beautify_hour);
        String day = TwitterApplication.mContext
                .getString(R.string.tweet_created_at_beautify_day);
        String suffix = TwitterApplication.mContext
                .getString(R.string.tweet_created_at_beautify_suffix);

        // Seconds.
        long diff = (now.getTime() - date.getTime()) / 1000;

        if (diff < 0) {
            diff = 0;
        }

        if (diff < 60) {
            return diff + sec + suffix;
        }

        // Minutes.
        diff /= 60;

        if (diff < 60) {
            return prefix + diff + min + suffix;
        }

        // Hours.
        diff /= 60;

        if (diff < 24) {
            return prefix + diff + hour + suffix;
        }

        return AGO_FULL_DATE_FORMATTER.format(date);
    }

    public static long getNowTime() {
        return Calendar.getInstance().getTime().getTime();
    }
}
