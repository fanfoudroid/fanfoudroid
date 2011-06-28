package com.ch_linghu.fanfoudroid.dao;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.ch_linghu.fanfoudroid.data2.Status;
import com.ch_linghu.fanfoudroid.data2.User;
import com.ch_linghu.fanfoudroid.db2.FanDatabase;

public class UserDAO {
    private static final String TAG = UserDAO.class.getSimpleName();

    private SQLiteTemplate mSqlTemplate;

    public UserDAO(Context context) {
        mSqlTemplate = new SQLiteTemplate(FanDatabase.getInstance(context)
                .getSQLiteOpenHelper());
    }
}
