package com.ch_linghu.fanfoudroid.task;

import android.util.Log;

import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.db.StatusTable;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.ui.base.BaseActivity;

public class TweetCommonTask {
	public static class DeleteTask extends GenericTask{
		public static final String TAG="DeleteTask";
		
		private BaseActivity activity;
		
		public DeleteTask(BaseActivity activity){
			this.activity = activity;
		}

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			TaskParams param = params[0];
			try {
				String id = param.getString("id");
				com.ch_linghu.fanfoudroid.weibo.Status status = null;

				status = activity.getApi().destroyStatus(id);

				// 对所有相关表的对应消息都进行删除（如果存在的话）
				activity.getDb().deleteTweet(status.getId(), "", -1);
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			return TaskResult.OK;

		}

	}


	public static class FavoriteTask extends GenericTask{
		private static final String TAG = "FavoriteTask";
		
		private BaseActivity activity;

		public static final String TYPE_ADD = "add";
	    public static final String TYPE_DEL = "del";
	    
	    private String type;
	    public String getType(){
	    	return type;
	    }
	    
	    public FavoriteTask(BaseActivity activity){
	    	this.activity = activity;
	    }
	    
		@Override
		protected TaskResult _doInBackground(TaskParams...params){
			TaskParams param = params[0];
			try {
				String action = param.getString("action");
				String id = param.getString("id");
				
				com.ch_linghu.fanfoudroid.weibo.Status status = null;
				if (action.equals(TYPE_ADD)) {
					status = activity.getApi().createFavorite(id);
					activity.getDb().setFavorited(id, "true");
					type = TYPE_ADD;
				} else {
					status = activity.getApi().destroyFavorite(id);
					activity.getDb().setFavorited(id, "false");
					type = TYPE_DEL;
				}

				Tweet tweet = Tweet.create(status);

//				if (!Utils.isEmpty(tweet.profileImageUrl)) {
//					// Fetch image to cache.
//					try {
//						activity.getImageManager().put(tweet.profileImageUrl);
//					} catch (IOException e) {
//						Log.e(TAG, e.getMessage(), e);
//					}
//				}

				if(action.equals(TYPE_DEL)){
					activity.getDb().deleteTweet(tweet.id, TwitterApplication.getMyselfId(), StatusTable.TYPE_FAVORITE);
				}
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			return TaskResult.OK;			
		}
	}
	
//	public static class UserTask extends GenericTask{
//
//		@Override
//		protected TaskResult _doInBackground(TaskParams... params) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//		
//	}
}
