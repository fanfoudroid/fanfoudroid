package com.ch_linghu.fanfoudroid.task;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.ch_linghu.fanfoudroid.TwitterApi.ApiException;
import com.ch_linghu.fanfoudroid.TwitterApi.AuthException;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;

public class FavoriteTask extends AsyncTask<String, Void, TaskResult> {

	private static final String TAG = "FavoriteTask";
	private HasFavorite activity = null;
	
	public FavoriteTask(HasFavorite activity) {
		super();
		this.activity = activity;
	}
	
	@Override
	public void onPreExecute() {
		// onSendBegin();
	}

	@Override
	public TaskResult doInBackground(String... params) {
		try {
			String action = params[0];
			String id = params[1];
			com.ch_linghu.fanfoudroid.weibo.Status status = null;
			if (action.equals("add")) {
				status = activity.nApi.createFavorite(id);
			} else {
				status = activity.nApi.destroyFavorite(id);
			}

			Tweet tweet = Tweet.create(status);

			if (!Utils.isEmpty(tweet.profileImageUrl)) {
				// Fetch image to cache.
				try {
					activity.getImageManager().put(tweet.profileImageUrl);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}

			activity.mDb.updateTweet(tweet);
		} catch (WeiboException e) {
			Log.e(TAG, e.getMessage(), e);
			return TaskResult.IO_ERROR;
		}

		return TaskResult.OK;
	}

	@Override
	public void onPostExecute(TaskResult result) {
		if (isCancelled()) {
			// Canceled doesn't really mean "canceled" in this task.
			// We want the request to complete, but don't want to update the
			// activity (it's probably dead).
			return;
		}

		if (result == TaskResult.AUTH_ERROR) {
			activity.logout();
		} else if (result == TaskResult.OK) {
			activity.onFavSuccess();
		} else if (result == TaskResult.IO_ERROR) {
			activity.onFavFailure();
		}
	}

}
