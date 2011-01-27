package com.ch_linghu.fanfoudroid.helper;

import android.graphics.Bitmap;

public interface ProfileImageCacheCallback {
	void refresh(String url, Bitmap bitmap);
}
