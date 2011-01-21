package com.ch_linghu.fanfoudroid.task;

public interface TaskListener {
	void setTask(GenericTask task);
	String getName();
	
	TaskResult doInBackground(TaskParams params);
	void onPreExecute();
	void onPostExecute(TaskResult result);
	void onProgressUpdate(Object param);
	void onCancelled();
}
