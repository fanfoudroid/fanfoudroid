package com.ch_linghu.fanfoudroid.task;

import android.os.AsyncTask;
import android.util.Log;

import com.ch_linghu.fanfoudroid.weibo.WeiboException;

public class DeleteTask extends AsyncTask<String, Void, TaskResult> {

	private static final String TAG = "FavoriteTask";
	private Deletable activity = null;
	
	public DeleteTask(Deletable activity) {
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
			String id = params[0];
			com.ch_linghu.fanfoudroid.weibo.Status status = null;

			status = HasFavorite.mApi.destroyStatus(id);
			Deletable.mDb.deleteTweet(status.getId());
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
			activity.onDeleteSuccess();
		} else if (result == TaskResult.IO_ERROR) {
			activity.onDeleteFailure();
		}
	}

}
