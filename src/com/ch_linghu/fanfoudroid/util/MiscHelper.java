package com.ch_linghu.fanfoudroid.util;

import android.os.Bundle;

public class MiscHelper {
    private static final String TAG = "MiscHelper";

    public static boolean isTrue(Bundle bundle, String key) {
        return bundle != null && bundle.containsKey(key)
                && bundle.getBoolean(key);
    }
}
