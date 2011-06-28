package com.ch_linghu.fanfoudroid.data2;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.ch_linghu.fanfoudroid.dao.UserDAO;

public class UserUtils {
    private static final String TAG = StatusUtils.class.getSimpleName();

    private Context context;
    private UserDAO userDAO;

    public UserUtils(Context con) {
        context = con;
        userDAO = new UserDAO(con);
    }

    public User json2Object(JSONObject jsonObject) throws JSONException {
        User user = new User();
        return user;
    }
}
