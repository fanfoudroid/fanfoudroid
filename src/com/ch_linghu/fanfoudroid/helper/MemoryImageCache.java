package com.ch_linghu.fanfoudroid.helper;

import java.util.HashMap;


import android.graphics.Bitmap;

public class MemoryImageCache implements ImageCache {

  private HashMap<String, Bitmap> mCache;

  public MemoryImageCache() {
    mCache = new HashMap<String, Bitmap>();
  }

  @Override
  public Bitmap get(String url) {
    synchronized(this) {
      Bitmap bitmap = mCache.get(url);
      if (bitmap == null){
    	  return mDefaultBitmap;
      }else{
    	  return bitmap;
      }
    }
  }

  @Override
  public void put(String url, Bitmap bitmap) {
    synchronized(this) {
      mCache.put(url, bitmap);
    }
  }

  public void putAll(MemoryImageCache imageCache) {
    synchronized(this) {
      // TODO: is this thread safe?
      mCache.putAll(imageCache.mCache);
    }
  }

}
