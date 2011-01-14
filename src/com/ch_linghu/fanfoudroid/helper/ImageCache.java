package com.ch_linghu.fanfoudroid.helper;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterApplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public interface ImageCache {
	public static Bitmap mDefaultBitmap = Utils.drawableToBitmap(TwitterApplication.mContext.getResources().getDrawable(R.drawable.user_default_photo));
	public Bitmap get(String url);

	public void put(String url, Bitmap bitmap);
}
