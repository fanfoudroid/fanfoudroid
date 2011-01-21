package com.ch_linghu.fanfoudroid.task;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;

public class FavoriteTask extends AsyncTask<String, Void, TaskResult> {

	private static final String TAG = "FavoriteTask";
	private HasFavorite activity = null;
	
    public static final String TYPE_ADD = "add";
    public static final String TYPE_DEL = "del";
    public String type = TYPE_ADD;
    
    public String getType() {
        return type;
    }
	
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
			if (action.equals(TYPE_ADD)) {
				status = HasFavorite.mApi.createFavorite(id);
				HasFavorite.mDb.setFavorited(id, "true");
			} else {
			    this.type = TYPE_DEL;
				status = HasFavorite.mApi.destroyFavorite(id);
				HasFavorite.mDb.setFavorited(id, "false");
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

			//TODO: 新数据库暂时没有favorite类型
			
			//对所有相关表的对应消息都进行刷新（如果存在的话）
//			HasFavorite.mDb.updateTweet(TwitterDbAdapter.TABLE_FAVORITE, tweet);
//			HasFavorite.mDb.updateTweet(TwitterDbAdapter.TABLE_MENTION, tweet);
//			HasFavorite.mDb.updateTweet(TwitterDbAdapter.TABLE_TWEET, tweet);
//			if(action.equals("del")){
//				HasFavorite.mDb.destoryStatus(TwitterDbAdapter.TABLE_FAVORITE, tweet.id);
//			}
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
