package com.ch_linghu.fanfoudroid.app;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.app.LazyImageLoader.ImageLoaderCallback;

public class SimpleImageLoader {

	public static void display(final ImageView imageView, String url) {
		imageView.setTag(url);
		imageView.setImageBitmap(TwitterApplication.mImageLoader.get(url,
				createImageViewCallback(imageView, url)));
	}

	public static ImageLoaderCallback createImageViewCallback(
			final ImageView imageView, String url) {
		return new ImageLoaderCallback() {
			@Override
			public void refresh(String url, Bitmap bitmap) {
				if (url.equals(imageView.getTag())) {
					imageView.setImageBitmap(bitmap);
				}
			}
		};
	}
}
