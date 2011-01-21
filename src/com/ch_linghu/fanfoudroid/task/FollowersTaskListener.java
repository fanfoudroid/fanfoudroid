package com.ch_linghu.fanfoudroid.task;

import java.util.Arrays;
import java.util.List;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.weibo.IDs;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;

public class FollowersTaskListener implements TaskListener {

    private static FollowersTaskListener instance = null; 
    public static FollowersTaskListener getInstance(Followable activity){
    	if (instance == null){
    		instance = new FollowersTaskListener();
    	}
    	instance.setFollowable(activity);
    	return instance;
    }
	
	private static final String TAG = "FollowersTask";
	private Followable activity = null;
	
	public void setFollowable(Followable activity) {
		this.activity = activity;
	}
	
	@Override
	public TaskResult doInBackground(TaskParams params) {
		try {
			//TODO: 目前仅做新API兼容性改动，待完善Follower处理
			IDs followers = Followable.mApi.getFollowersIDs();
			List<String> followerIds = Arrays.asList(followers.getIDs());
			Followable.mDb.syncFollowers(followerIds);
		} catch (WeiboException e) {
			Log.e(TAG, e.getMessage(), e);
			return TaskResult.IO_ERROR;
		}

		return TaskResult.OK;
	}

	@Override
	public void onPostExecute(TaskResult result) {
		if (result == TaskResult.OK) {
			SharedPreferences sp = activity.getPreferences();
			SharedPreferences.Editor editor = sp.edit();
			editor.putLong(Preferences.LAST_FOLLOWERS_REFRESH_KEY, Utils
					.getNowTime());
			editor.commit();
		} else {
			// Do nothing.
		}
	}

	@Override
	public String getName() {
		return "Followers";
	}

	@Override
	public void onCancelled() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPreExecute() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProgressUpdate(Object param) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTask(GenericTask task) {
		// TODO Auto-generated method stub
		
	}
}
