package com.ch_linghu.fanfoudroid.app;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ch_linghu.fanfoudroid.TwitterApplication;

class CallbackManager{
	private static final String TAG = "CallbackManager";
	private HashMap<String, ArrayList<LazyImageLoader.ImageLoaderCallback>> mCallbackMap;
	
	public CallbackManager(){
		mCallbackMap = new HashMap<String, ArrayList<LazyImageLoader.ImageLoaderCallback>>();
	}
	
	public void put(String url, LazyImageLoader.ImageLoaderCallback callback){
		Log.d(TAG, "url="+url);
		if (!mCallbackMap.containsKey(url)){
			Log.d(TAG, "url does not exist, add list to map");
			mCallbackMap.put(url, new ArrayList<LazyImageLoader.ImageLoaderCallback>());
		}
		
		mCallbackMap.get(url).add(callback);
		Log.d(TAG, "Add callback to list, count(url)=" + mCallbackMap.get(url).size());
	}
	
	public void call(String url, Bitmap bitmap){
		Log.d(TAG, "call url=" + url);
		ArrayList<LazyImageLoader.ImageLoaderCallback> callbackList = mCallbackMap.get(url);
		if (callbackList == null){
			//FIXME: 有时会到达这里，原因我还没想明白
			Log.d(TAG, "callbackList=null");				
			return;
		}
		for (LazyImageLoader.ImageLoaderCallback callback : callbackList){
			if(callback != null){
				callback.refresh(url, bitmap);
			}
		}

		callbackList.clear();
		mCallbackMap.remove(url);
	}
	
	
}
public class LazyImageLoader {
    private static final String TAG = "ProfileImageCacheManager";
    public static final int HANDLER_MESSAGE_ID = 1;
    public static final String EXTRA_BITMAP = "extra_bitmap";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";

    private ImageManager mImageManager = new ImageManager(TwitterApplication.mContext);
    private BlockingQueue<String> mUrlList = new ArrayBlockingQueue<String>(50);
    private CallbackManager mCallbackManager = new CallbackManager();

    private GetImageTask mTask = new GetImageTask();

    public Bitmap get(String url, ImageLoaderCallback callback) {
        Bitmap bitmap = ImageCache.mDefaultBitmap;
        if(mImageManager.isContains(url)){
            bitmap = mImageManager.get(url);
        } else {
            // bitmap不存在，启动Task进行下载
            mCallbackManager.put(url, callback);
            doGetImage(url);
        }
        return bitmap;
    }

    // Low-level interface to get ImageManager
    public ImageManager getImageManager() {
        return mImageManager;
    }

    private void putUrl(String url) throws InterruptedException {
        if (!mUrlList.contains(url)) {
            mUrlList.put(url);
        }
    }

    private void doGetImage(String url) {
        if (url != null) {
            try {
                putUrl(url);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // start thread if it's not started yet
        Log.i("LDS", mTask.getState() +"");
        
        State state = mTask.getState();
        if (Thread.State.NEW == state) { 
            mTask.start();
        } else if (Thread.State.TERMINATED == state) {
            mTask = new GetImageTask(); // restart thread
            mTask.start();
        }
    }

    private class GetImageTask extends Thread {
        private volatile boolean mTaskTerminated = false;
        private static final int TIMEOUT = 3*60; 
        private boolean isPermanent = true;
        
        @Override
		public void run() {
            try {
                while ( !mTaskTerminated ) {
                    String url;
                    if (isPermanent) {
                        url = mUrlList.take();
                    } else {
                        url = mUrlList.poll(TIMEOUT, TimeUnit.SECONDS); // waiting
                        if (null == url) { break; } // no more, shutdown
                    }
                    
                    Bitmap bitmap = ImageCache.mDefaultBitmap;
                    bitmap = mImageManager.safeGet(url);

                    // use handler to process callback
                    Message m = handler.obtainMessage(HANDLER_MESSAGE_ID);
                    Bundle bundle = m.getData();
                    bundle.putString(EXTRA_IMAGE_URL, url);
                    bundle.putParcelable(EXTRA_BITMAP, bitmap);
                    handler.sendMessage(m);
                }
            } catch (IOException ioe) {
                Log.e(TAG, "Get Image failed, " + ioe.getMessage());
            } catch (InterruptedException e) {
                Log.w(TAG, e.getMessage());
            } finally {
                Log.v(TAG, "Get image task terminated.");
                mTaskTerminated = true;
            }
        }
            
        public boolean isPermanent() {
            return isPermanent;
        }

        public void setPermanent(boolean isPermanent) {
            this.isPermanent = isPermanent;
        }

        public void shutDown() throws InterruptedException {
            mTaskTerminated = true;
        }
    }

    Handler handler = new Handler() {
        @Override
		public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_MESSAGE_ID:
                    Bundle bundle = msg.getData();
                    String url = bundle.getString(EXTRA_IMAGE_URL);
                    Bitmap bitmap = (Bitmap) (bundle.get(EXTRA_BITMAP));

                    // callback
                    mCallbackManager.call(url, bitmap);
                    break;
                default:
                    // do nothing.
            }
        }
    };
    
    public interface ImageLoaderCallback {
        void refresh(String url, Bitmap bitmap);
    }
}


