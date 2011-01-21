package com.ch_linghu.fanfoudroid.task;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.db.TwitterDbAdapter;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;

public class DeleteTaskListener implements TaskListener {
	private static DeleteTaskListener instance = null;
	public static DeleteTaskListener getInstance(Deletable activity){
		if (instance == null){
			instance = new DeleteTaskListener();
		}
		instance.setDeletable(activity);
		return instance;
	}

	private static final String TAG = "FavoriteTask";
	private Deletable activity = null;
	
	public void setDeletable(Deletable activity){
		this.activity = activity;
	}
	
	@Override
	public void onPreExecute() {
		// onSendBegin();
	}

	@Override
	public TaskResult doInBackground(TaskParams params) {
		try {
			String id = params.getString("id");
			com.ch_linghu.fanfoudroid.weibo.Status status = null;

			status = HasFavorite.mApi.destroyStatus(id);

			//对所有相关表的对应消息都进行删除（如果存在的话）
			Deletable.mDb.destoryStatus(TwitterDbAdapter.TABLE_FAVORITE, status.getId());
			Deletable.mDb.destoryStatus(TwitterDbAdapter.TABLE_MENTION, status.getId());
			Deletable.mDb.destoryStatus(TwitterDbAdapter.TABLE_TWEET, status.getId());
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
			activity.onDeleteSuccess();
		} else if (result == TaskResult.IO_ERROR) {
			activity.onDeleteFailure();
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Delete";
	}

	@Override
	public void onCancelled() {
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
