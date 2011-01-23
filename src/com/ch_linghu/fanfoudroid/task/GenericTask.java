package com.ch_linghu.fanfoudroid.task;

import java.util.Map;

import com.ch_linghu.fanfoudroid.TwitterApplication;

import android.os.AsyncTask;
import android.widget.Toast;

public abstract class GenericTask extends AsyncTask<TaskParams, Object, TaskResult> {

	private TaskListener mListener = null;
	
	abstract protected TaskResult _doInBackground(TaskParams...params);
	
	public void setListener(TaskListener taskListener){
		mListener = taskListener;
	}
	
	public TaskListener getListener(){
		return mListener;
	}
	
	public void doPublishProgress(Object... values){
		super.publishProgress(values);
	}
	
	@Override
	protected void onCancelled() {
		super.onCancelled();

		if (mListener != null){
			mListener.onCancelled(this);
		}
		Toast.makeText(TwitterApplication.mContext, mListener.getName() + " has been cancelled", Toast.LENGTH_SHORT);
	}
	@Override
	protected void onPostExecute(TaskResult result) {
		super.onPostExecute(result);

		if (mListener != null){
			mListener.onPostExecute(this, result);
		}
		Toast.makeText(TwitterApplication.mContext, mListener.getName() + " completed", Toast.LENGTH_SHORT);
	}
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		if (mListener != null){
			mListener.onPreExecute(this);
		}
	}
	@Override
	protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);
		
		if (mListener != null){
			if (values != null && values.length > 0){
				mListener.onProgressUpdate(this, values[0]);
			}
		}
	}
	@Override
	protected TaskResult doInBackground(TaskParams... params){
		return _doInBackground(params);
	}
}
