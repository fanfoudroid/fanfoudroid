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
		putUrl(url);
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
			// TODO: 这里的循环机制可以考虑进行优化, 可使用列队等待的形式进行批量下载图片操作,
			// 因为仅靠循环来进行, 下载的速度会大大慢于一次循环的周期. 也就说在前一张图片刚开始进行下载时,
			// 循环已经可以走了几遍了, 因此造成:
			// Image is missing: ..
			// 两条调试信息反复出现, 因为下载操作还在未完成的情况下又进行了对同一张图片的get操作,
			// 就是因为循环速度大大快于下载速度所致, 在
			// Fetching image: ..
			// 调试信息出来前, 反复的在进行重复的get操作.
			while (mUrlList.size() > 0){
				synchronized(mUrlList){
					url = mUrlList.get(0);
					mUrlList.remove(0);
				}
				
				try {
					mImageManager.put(url);
				} catch (IOException e) {
					Log.e(TAG,  url + " get failed!");
					continue;
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
