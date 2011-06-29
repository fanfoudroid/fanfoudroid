package com.ch_linghu.fanfoudroid.data2;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class PhotoUtils {
    private static final String TAG = StatusHelper.class.getSimpleName();

    private Context context;

    public PhotoUtils(Context con) {
        context = con;
    }

    public Photo json2Object(JSONObject jsonObject) throws JSONException {
        Photo photo = new Photo();
        return photo;
    }
}
