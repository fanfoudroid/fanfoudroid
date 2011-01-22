package com.ch_linghu.fanfoudroid.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.ch_linghu.fanfoudroid.weibo.Status;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.helper.ImageManager;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.Utils;

public class RetrieveListTaskListener implements TaskListener {
	
    private static RetrieveListTaskListener instance = null; 
    public static RetrieveListTaskListener getInstance(Retrievable activity){
    	if (instance == null){
    		instance = new RetrieveListTaskListener();
    	}
    	instance.setRetrievable(activity);
    	return instance;
    }
	
	private static final String TAG = "RetrieveListTask";
	private Retrievable activity = null;
	private GenericTask task;
	
	public void setRetrievable(Retrievable activity) {
		this.activity = activity;
	}
	
	@Override
	public void onPreExecute() {
		activity.onRetrieveBegin();
	}

	@Override
	public void onProgressUpdate(Object param) {
		activity.draw();
	}

	@Override
	public TaskResult doInBackground(TaskParams params) {
		List<Status> statusList;

		String maxId = activity.fetchMaxId(); // getDb().fetchMaxMentionId();

		try {
			statusList = activity.getMessageSinceId(maxId);
		} catch (WeiboException e) {
			Log.e(TAG, e.getMessage(), e);
			return TaskResult.IO_ERROR;
		}

		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
		HashSet<String> imageUrls = new HashSet<String>();
		
		for (Status status : statusList) {
			if (task.isCancelled()) {
				return TaskResult.CANCELLED;
			}

			Tweet tweet;

			tweet = Tweet.create(status);
			tweets.add(tweet);

			imageUrls.add(tweet.profileImageUrl);

			if (task.isCancelled()) {
				return TaskResult.CANCELLED;
			}
		}

		activity.addMessages(tweets, false); // getDb().addMentions(tweets, false);

		if (task.isCancelled()) {
			return TaskResult.CANCELLED;
		}

		//task.publishProgress();

		for (String imageUrl : imageUrls) {
			if (!Utils.isEmpty(imageUrl)) {
				// Fetch image to cache.
				try {
					activity.getImageManager().put(imageUrl);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}

			if (task.isCancelled()) {
				return TaskResult.CANCELLED;
			}
		}

		return TaskResult.OK;
	}

	@Override
	public void onPostExecute(TaskResult result) {
		if (result == TaskResult.AUTH_ERROR) {
			activity.logout();
		} else if (result == TaskResult.OK) {
			SharedPreferences.Editor editor = activity.getPreferences().edit();
			editor.putLong(Preferences.LAST_TWEET_REFRESH_KEY, Utils
					.getNowTime());
			editor.commit();
			activity.draw();
			activity.goTop();
		} else {
			// Do nothing.
		}

		// 刷新按钮停止旋转
		activity.getRefreshButton().clearAnimation();
		activity.updateProgress("");
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCancelled() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTask(GenericTask task) {
		this.task = task;
	}
}
