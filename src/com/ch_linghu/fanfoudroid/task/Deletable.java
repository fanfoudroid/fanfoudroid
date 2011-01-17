package com.ch_linghu.fanfoudroid.task;

import android.content.SharedPreferences;

import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.data.db.TwitterDbAdapter;
import com.ch_linghu.fanfoudroid.helper.ImageManager;
import com.ch_linghu.fanfoudroid.weibo.Weibo;

public interface Deletable {
	// Global
	static Weibo mApi = TwitterApplication.mApi;
	static TwitterDbAdapter mDb = TwitterApplication.mDb; 
	
	void logout();

	// Callback
	void onDeleteSuccess();
	void onDeleteFailure();

}
