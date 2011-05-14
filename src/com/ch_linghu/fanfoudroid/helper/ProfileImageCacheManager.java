package com.ch_linghu.fanfoudroid.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ch_linghu.fanfoudroid.TwitterApplication;


public class ProfileImageCacheManager {
	private static final String TAG="ProfileImageCacheManager";
	
	private static final int PROGRESS = 1;
	
	private ImageManager mImageManager = new ImageManager(TwitterApplication.mContext);
	private ArrayList<String> mUrlList = new ArrayList<String>();
	private HashMap<String, ProfileImageCacheCallback> mCallbackMap = new HashMap<String, ProfileImageCacheCallback>();
	
	private GetImageTask mTask = new GetImageTask();
	
	public Bitmap get(String url, ProfileImageCacheCallback callback){
		Bitmap bitmap = ImageManager.mDefaultBitmap;
		if(mImageManager.isContains(url)){
			bitmap = mImageManager.get(url);
		}else{
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
			if (!mUrlList.contains(url)){
				mUrlList.add(url);
				mUrlList.notifyAll();
			}
		}
	}
	
	private void doGetImage(String url){
		if (url != null){
			putUrl(url);
		}
		
        //start thread if it's not started yet
        if(mTask.getState()==Thread.State.NEW)
        	mTask.start();
	}
	
	private class GetImageTask extends Thread {
        public void run() {
            try {
                while(true)
                {
        			String url = null;
        			if (mUrlList.size() == 0){
        				synchronized(mUrlList){
        					mUrlList.wait();
        				}	
        			}
        			
        			if (mUrlList.size() > 0){
        				synchronized(mUrlList){
        					url = mUrlList.get(0);
        					mUrlList.remove(url);
        				}	
        				
        				Bitmap bitmap = ImageManager.mDefaultBitmap;
        				try {
        					bitmap = mImageManager.safeGet(url);
        				} catch (IOException e) {
        					Log.e(TAG,  url + " get failed!");
        				}

        				//use handler to process callback
        				Message m = handler.obtainMessage(PROGRESS);
        				Bundle bundle = m.getData();
        				bundle.putString("url", url);
        				bundle.putParcelable("bitmap", bitmap);
        				
        				handler.sendMessage(m);
        				
        			}
                }
            } catch (InterruptedException e) {
                //allow thread to exit
            }
        }
    }
	
	Handler handler = new Handler(){
        public void handleMessage(Message msg) {  
            switch (msg.what) {  
            case PROGRESS:
            	Bundle bundle = msg.getData();
            	String url = bundle.getString("url");
            	Bitmap bitmap = (Bitmap)(bundle.get("bitmap"));

            	//callback
				ProfileImageCacheCallback callback = mCallbackMap.get(url);
				mCallbackMap.remove(url);
				if (callback != null){
					callback.refresh(url, bitmap);
				}
            	
                break;  
            }  
        }		
	};
}
