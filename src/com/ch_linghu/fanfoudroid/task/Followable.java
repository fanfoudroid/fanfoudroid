package com.ch_linghu.fanfoudroid.task;

import android.content.SharedPreferences;

import com.ch_linghu.fanfoudroid.TwitterApi;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.data.db.TwitterDbAdapter;

public interface Followable {
	// Global
	static TwitterApi mApi = TwitterApplication.mApi;
	static TwitterDbAdapter mDb = TwitterApplication.mDb; 
	SharedPreferences getPreferences();

}
