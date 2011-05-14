package com.ch_linghu.fanfoudroid.helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ch_linghu.fanfoudroid.TwitterApplication;

public class ProfileImageCacheManager {
    private static final String TAG = "ProfileImageCacheManager";
    public static final int HANDLER_MESSAGE_ID = 1;
    public static final String EXTRA_BITMAP = "extra_bitmap";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";

    private ImageManager mImageManager = new ImageManager(TwitterApplication.mContext);
    private BlockingQueue<String> mUrlList = new ArrayBlockingQueue<String>(50);
    private HashMap<String, ProfileImageCacheCallback> mCallbackMap = new HashMap<String, ProfileImageCacheCallback>();

    private GetImageTask mTask = new GetImageTask();

    public Bitmap get(String url, ProfileImageCacheCallback callback) {
        Bitmap bitmap = ImageManager.mDefaultBitmap;
        if(mImageManager.isContains(url)){
            bitmap = mImageManager.get(url);
        } else {
            // bitmap不存在，启动Task进行下载
            mCallbackMap.put(url, callback);
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
        if (mTask.getState() == Thread.State.NEW) {
            mTask.start();
        }
    }

    private class GetImageTask extends Thread {
        private volatile boolean mTaskTerminated = false;

        public void run() {
            try {
                while (!mTaskTerminated) {
                    String url = mUrlList.take(); // blocking
                    Bitmap bitmap = ImageManager.mDefaultBitmap;
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
                Log.e(TAG, e.getMessage());
            } finally {
                mTaskTerminated = true;
            }
        }

        public void shutDown() throws InterruptedException {
            mTaskTerminated = true;
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_MESSAGE_ID:
                    Bundle bundle = msg.getData();
                    String url = bundle.getString(EXTRA_IMAGE_URL);
                    Bitmap bitmap = (Bitmap) (bundle.get(EXTRA_BITMAP));

                    // callback
                    ProfileImageCacheCallback callback = mCallbackMap.get(url);
                    mCallbackMap.remove(url);
                    if (callback != null) {
                        callback.refresh(url, bitmap); // FIXME: 刷新list adapter 会造成UI不流畅, 测试直接使用 imageView.setImageDrawable() 效果比较好
                    }
                    break;
                default:
                    // do nothing.
            }
        }
    };
}


