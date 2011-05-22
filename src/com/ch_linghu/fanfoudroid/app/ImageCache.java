package com.ch_linghu.fanfoudroid.app;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterApplication;

import android.graphics.Bitmap;

public interface ImageCache {
	public static Bitmap mDefaultBitmap = ImageManager.drawableToBitmap(TwitterApplication.mContext.getResources().getDrawable(R.drawable.user_default_photo));
	public Bitmap get(String url);

	public void put(String url, Bitmap bitmap);
}
