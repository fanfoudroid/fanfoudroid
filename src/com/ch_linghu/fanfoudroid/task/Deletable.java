package com.ch_linghu.fanfoudroid.task;

import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.data.db.StatusDatabase;
import com.ch_linghu.fanfoudroid.weibo.Weibo;

public interface Deletable {
	// Global
	static Weibo mApi = TwitterApplication.mApi;
	static StatusDatabase mDb = TwitterApplication.mDb; 
	
	void logout();

	// Callback
	void onDeleteSuccess();
	void onDeleteFailure();

}
