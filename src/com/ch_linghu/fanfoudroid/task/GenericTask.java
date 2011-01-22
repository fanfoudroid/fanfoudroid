package com.ch_linghu.fanfoudroid.task;

import com.ch_linghu.fanfoudroid.TwitterApplication;

import android.os.AsyncTask;
import android.widget.Toast;

public class GenericTask extends AsyncTask<TaskParams, Object, TaskResult> {

	private TaskListener mListener = null;
	
	public void setListener(TaskListener taskListener){
		mListener = taskListener;
		mListener.setTask(this);
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
			mListener.onCancelled();
		}
		Toast.makeText(TwitterApplication.mContext, mListener.getName() + " has been cancelled", Toast.LENGTH_SHORT);
	}
	@Override
	protected void onPostExecute(TaskResult result) {
		super.onPostExecute(result);

		if (mListener != null){
			mListener.onPostExecute(result);
		}
		Toast.makeText(TwitterApplication.mContext, mListener.getName() + " completed", Toast.LENGTH_SHORT);
	}
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		if (mListener != null){
			mListener.onPreExecute();
		}
	}
	@Override
	protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);
		
		if (mListener != null){
			if (values != null && values.length > 0){
				mListener.onProgressUpdate(values[0]);
			}
		}
	}
	@Override
	protected TaskResult doInBackground(TaskParams... arg0) {
		if (mListener != null){
			if (arg0 != null && arg0.length > 0){
				return mListener.doInBackground(arg0[0]);
			}else{
				return mListener.doInBackground(null);
			}
		}
		return null;
	}
}
