package com.ch_linghu.fanfoudroid.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.util.Log;

import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;

public class ProfileImageCacheManager {
	private static final String TAG="ProfileImageCacheManager";
	
	private ImageManager mImageManager = new ImageManager(TwitterApplication.mContext);
	private ArrayList<String> mUrlList = new ArrayList<String>();
	private HashMap<String, ProfileImageCacheCallback> mCallbackMap = new HashMap<String, ProfileImageCacheCallback>();
	
	private GenericTask mTask;
	private TaskListener mTaskListener = new TaskAdapter(){

		@Override
		public String getName() {
			return "GetProfileImage";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
		
			if (result == TaskResult.OK){
				//如果还有没处理完的，再次启动Task继续处理
				if (mUrlList.size() != 0){
					doGetImage(null);
				}
			}
		}
		
		@Override
		public void onProgressUpdate(GenericTask task, Object param) {
			super.onProgressUpdate(task, param);
			
			TaskParams p = (TaskParams)param;
			String url = (String)p.get("url");
			Bitmap bitmap = (Bitmap)p.get("bitmap");
			
			mCallbackMap.get(url).refresh(url, bitmap);
		}
		
	};
	
	public Bitmap get(String url, ProfileImageCacheCallback callback){
		Bitmap bitmap = mImageManager.get(url);
		if(bitmap == ImageCache.mDefaultBitmap){
			//bitmap不存在，启动Task进行下载
			mCallbackMap.put(url, callback);
			doGetImage(url);
		}
		return bitmap;
	}
	
	//Low-level interface to get ImageManager
	public ImageManager getImageManager(){
		return mImageManager;
	}
	
	private void putUrl(String url){
		synchronized(mUrlList){
			mUrlList.add(url);
		}
	}
	
	private void doGetImage(String url){
		if (url != null){
			putUrl(url);
		}
		
		if (mTask != null && mTask.getStatus() == GenericTask.Status.RUNNING){
			return;
		}else{
			mTask = new GetImageTask();
			mTask.setListener(mTaskListener);
			
			mTask.execute();
		}	
	}
	
	private class GetImageTask extends GenericTask{

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			String url = null;

			if (mUrlList.size() > 0){
				synchronized(mUrlList){
					url = mUrlList.get(0);
					mUrlList.remove(0);
				}	
				try {
					mImageManager.put(url);
				} catch (IOException e) {
					Log.e(TAG,  url + " get failed!");
				}
				
				TaskParams p = new TaskParams();
				p.put("url", url);
				p.put("bitmap", mImageManager.get(url));
				publishProgress(p);
			}
			return TaskResult.OK;
		}
		
	}
}
