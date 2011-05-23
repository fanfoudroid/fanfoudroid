package com.ch_linghu.fanfoudroid.app;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.app.LazyImageLoader.ImageLoaderCallback;

public class SimpleImageLoader {
    
	static HashMap<String, ArrayList<ImageView>> viewMap = new HashMap<String, ArrayList<ImageView>>();
	
    public static void display(final ImageView imageView, String url) {
        imageView.setTag(url);
        imageView.setImageBitmap(TwitterApplication.mImageLoader
                .get(url, createImageViewCallback(imageView, url)));
    }
    
    public static ImageLoaderCallback createImageViewCallback(final ImageView imageView, String url)
    {
    	if (!viewMap.containsKey(url)){
    		ArrayList<ImageView> viewList = new ArrayList<ImageView>();
    		viewMap.put(url, viewList);
    	}
    	viewMap.get(url).add(imageView);
    	
        return new ImageLoaderCallback() {
            @Override
            public void refresh(String url, Bitmap bitmap) {
            	for(ImageView imageView : viewMap.get(url)){
	                if (url.equals(imageView.getTag())) {
	                    imageView.setImageBitmap(bitmap);
	                }
            	}
            }
        };
    }
}
