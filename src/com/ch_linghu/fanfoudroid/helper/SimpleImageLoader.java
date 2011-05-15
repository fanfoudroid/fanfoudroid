package com.ch_linghu.fanfoudroid.helper;

import com.ch_linghu.fanfoudroid.TwitterApplication;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class SimpleImageLoader {
    
    public static void display(final ImageView imageView, String url) {
        imageView.setTag(url);
        imageView.setImageBitmap(TwitterApplication.mProfileImageCacheManager
                .get(url, createImageViewCallback(imageView, url)));
    }
    
    public static ProfileImageCacheCallback createImageViewCallback(final ImageView imageView, String url)
    {
        return new ProfileImageCacheCallback() {
            @Override
            public void refresh(String url, Bitmap bitmap) {
                if (url.equals(imageView.getTag())) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        };
    }
}
