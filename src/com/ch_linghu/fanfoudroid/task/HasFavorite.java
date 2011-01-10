package com.ch_linghu.fanfoudroid.task;

import android.content.SharedPreferences;

import com.ch_linghu.fanfoudroid.TwitterApi;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.db.TwitterDbAdapter;
import com.ch_linghu.fanfoudroid.helper.ImageManager;

public interface HasFavorite {
	// Global
	static TwitterApi mApi = TwitterApplication.mApi;
	static TwitterDbAdapter mDb = TwitterApplication.mDb; 
	SharedPreferences getPreferences();
	
	void logout();
	
	ImageManager getImageManager();
	
	// Callback
	void onFavSuccess();
	void onFavFailure();

}
