package com.ch_linghu.fanfoudroid.task;

import android.content.SharedPreferences;

import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.data.db.StatusDatabase;
import com.ch_linghu.fanfoudroid.weibo.Weibo;

public interface Followable {
	// Global
	static Weibo mApi = TwitterApplication.mApi;
	static StatusDatabase mDb = TwitterApplication.mDb; 
	SharedPreferences getPreferences();
}
