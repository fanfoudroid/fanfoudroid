package com.ch_linghu.fanfoudroid.data;


import android.content.ContentValues;
import android.database.Cursor;

public interface BaseContent {
    long insert();
    int delete();
    int update();
    Cursor select();
}
