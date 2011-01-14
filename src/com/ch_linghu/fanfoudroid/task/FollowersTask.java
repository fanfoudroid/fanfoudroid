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

public class FollowersTask extends AsyncTask<Void, Void, TaskResult> {
	
	private static final String TAG = "FollowersTask";
	private Followable activity = null;
	
	public FollowersTask(Followable activity) {
		super();
		this.activity = activity;
	}
	
	@Override
	public TaskResult doInBackground(Void... params) {
		try {
			//TODO: 目前仅做新API兼容性改动，待完善Follower处理
			IDs followers = Followable.nApi.getFollowersIDs();
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
}
