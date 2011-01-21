package com.ch_linghu.fanfoudroid.task;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.db.TwitterDbAdapter;
import com.ch_linghu.fanfoudroid.helper.ImageManager;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.weibo.Status;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;

public class FavoriteTaskListener implements TaskListener {

    private static FavoriteTaskListener instance = null; 
    public static FavoriteTaskListener getInstance(HasFavorite activity){
    	if (instance == null){
    		instance = new FavoriteTaskListener();
    	}
    	instance.setHasFavorite(activity);
    	return instance;
    }

	
	private static final String TAG = "FavoriteTask";
	private HasFavorite activity = null;
	
	public static final String TYPE_ADD = "add";
    public static final String TYPE_DEL = "del";
    public String type = TYPE_ADD;
    
    
    public String getType() {
        return type;
    }
	
	public void setHasFavorite(HasFavorite activity) {
		this.activity = activity;
	}
	
	@Override
	public void onPreExecute() {
		// onSendBegin();
	}

	@Override
	public TaskResult doInBackground(TaskParams params) {
		try {
			String action = params.getString("action");
			String id = params.getString("id");
			
			Status status = null;
			if (action.equals(TYPE_ADD)) {
				status = HasFavorite.mApi.createFavorite(id);
			} else {
			    this.type = TYPE_DEL;
				status = HasFavorite.mApi.destroyFavorite(id);
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

			//对所有相关表的对应消息都进行刷新（如果存在的话）
			HasFavorite.mDb.updateTweet(TwitterDbAdapter.TABLE_FAVORITE, tweet);
			HasFavorite.mDb.updateTweet(TwitterDbAdapter.TABLE_MENTION, tweet);
			HasFavorite.mDb.updateTweet(TwitterDbAdapter.TABLE_TWEET, tweet);
			if(action.equals("del")){
				HasFavorite.mDb.destoryStatus(TwitterDbAdapter.TABLE_FAVORITE, tweet.id);
			}
		} catch (WeiboException e) {
			Log.e(TAG, e.getMessage(), e);
			return TaskResult.IO_ERROR;
		}

		return TaskResult.OK;
	}

	@Override
	public void onPostExecute(TaskResult result) {
		if (result == TaskResult.AUTH_ERROR) {
			activity.logout();
		} else if (result == TaskResult.OK) {
			activity.onFavSuccess();
		} else if (result == TaskResult.IO_ERROR) {
			activity.onFavFailure();
		}
	}

	@Override
	public String getName() {
		return "Favorite";
	}

	@Override
	public void setTask(GenericTask task){
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onCancelled() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProgressUpdate(Object param) {
		// TODO Auto-generated method stub
		
	}

}
