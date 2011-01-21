package com.ch_linghu.fanfoudroid.task;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class TaskFactory {
    
    public static final String TAG = "TaskFactory";

    private static GenericTask task;
    
	public static GenericTask create(TaskListener listener) {
		if (task != null && task.getStatus() != AsyncTask.Status.FINISHED){
			try {
				task.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		task = new GenericTask();
		task.setListener(listener);
		
		return task;
	}
}
