package com.ch_linghu.android.fanfoudroid.task;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.ch_linghu.android.fanfoudroid.TwitterApi.ApiException;
import com.ch_linghu.android.fanfoudroid.TwitterApi.AuthException;
import com.ch_linghu.android.fanfoudroid.data.Tweet;
import com.ch_linghu.android.fanfoudroid.helper.Utils;

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
			JSONObject jsonObject = null;
			if (action.equals("add")) {
				jsonObject = activity.mApi.addFavorite(id);
			} else {
				jsonObject = activity.mApi.delFavorite(id);
			}

			Tweet tweet = Tweet.create(jsonObject);

			if (!Utils.isEmpty(tweet.profileImageUrl)) {
				// Fetch image to cache.
				try {
					activity.getImageManager().put(tweet.profileImageUrl);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}

			activity.mDb.updateTweet(tweet);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			return TaskResult.IO_ERROR;
		} catch (AuthException e) {
			Log.i(TAG, "Invalid authorization.");
			return TaskResult.AUTH_ERROR;
		} catch (JSONException e) {
			Log.w(TAG, "Could not parse JSON after sending update.");
			return TaskResult.IO_ERROR;
		} catch (ApiException e) {
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
