package com.ch_linghu.android.fanfoudroid.task;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;

import android.content.SharedPreferences;
import android.widget.ImageButton;

import com.ch_linghu.android.fanfoudroid.TwitterApi;
import com.ch_linghu.android.fanfoudroid.TwitterApplication;
import com.ch_linghu.android.fanfoudroid.TwitterApi.ApiException;
import com.ch_linghu.android.fanfoudroid.TwitterApi.AuthException;
import com.ch_linghu.android.fanfoudroid.data.Tweet;
import com.ch_linghu.android.fanfoudroid.data.db.TwitterDbAdapter;
import com.ch_linghu.android.fanfoudroid.helper.ImageManager;

public interface Retrievable {
	// Global
	static TwitterApi mApi = TwitterApplication.mApi;
	static TwitterDbAdapter mDb = TwitterApplication.mDb; 
	SharedPreferences getPreferences();
	
	void draw();
	void goTop();
	void logout();
	
	void doRetrieve();
	
	String fetchMaxId();
	ImageManager getImageManager();
	JSONArray getMessageSinceId(String maxId) throws IOException, AuthException, ApiException;;
	void addMessages(ArrayList<Tweet> tweets, boolean isUnread);
	
	// Callback
	void onRetrieveBegin();
	
	// View
	ImageButton getRefreshButton();
	void updateProgress(String msg);
	
}
