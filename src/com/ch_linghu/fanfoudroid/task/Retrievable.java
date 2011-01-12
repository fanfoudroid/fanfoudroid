package com.ch_linghu.fanfoudroid.task;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.widget.ImageButton;

import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.db.TwitterDbAdapter;
import com.ch_linghu.fanfoudroid.helper.ImageManager;
import com.ch_linghu.fanfoudroid.weibo.Weibo;

public interface Retrievable {
	// Global
	static Weibo nApi = TwitterApplication.nApi;
	static TwitterDbAdapter mDb = TwitterApplication.mDb; 
	SharedPreferences getPreferences();
	
	void draw();
	void goTop();
	void logout();
	
	void doRetrieve();
	
	String fetchMaxId();
	ImageManager getImageManager();
	void addMessages(ArrayList<Tweet> tweets, boolean isUnread);
	
	// Callback
	void onRetrieveBegin();
	
	// View
	ImageButton getRefreshButton();
	void updateProgress(String msg);
	
}
