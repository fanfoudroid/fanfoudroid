package com.ch_linghu.fanfoudroid.helper.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterApplication;

public class PhotoHelper {
	private static final String TAG = "ImageHelper";


	private static Pattern PHOTO_PAGE_LINK = Pattern
			.compile("http://fanfou.com(/photo/[-a-zA-Z0-9+&@#%?=~_|!:,.;]*[-a-zA-Z0-9+&@#%=~_|])");
	private static Pattern PHOTO_SRC_LINK = Pattern
			.compile("src=\"(http:\\/\\/photo\\.fanfou\\.com\\/.*?)\"");

	/**
	 * 获得消息中的照片页面链接
	 * 
	 * @param text
	 *            消息文本
	 * @param size
	 *            照片尺寸
	 * @return 照片页面的链接，若不存在，则返回null
	 */
	public static String getPhotoPageLink(String text, String size) {
		Matcher m = PHOTO_PAGE_LINK.matcher(text);
		if (m.find()) {
			String THUMBNAIL = TwitterApplication.mContext
					.getString(R.string.pref_photo_preview_type_thumbnail);
			String MIDDLE = TwitterApplication.mContext
					.getString(R.string.pref_photo_preview_type_middle);
			String ORIGINAL = TwitterApplication.mContext
					.getString(R.string.pref_photo_preview_type_original);
			if (size.equals(THUMBNAIL) || size.equals(MIDDLE)) {
				return "http://m.fanfou.com" + m.group(1);
			} else if (size.endsWith(ORIGINAL)) {
				return m.group(0);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * 获得照片页面中的照片链接
	 * 
	 * @param pageHtml
	 *            照片页面文本
	 * @return 照片链接，若不存在，则返回null
	 */
	public static String getPhotoURL(String pageHtml) {
		Matcher m = PHOTO_SRC_LINK.matcher(pageHtml);
		if (m.find()) {
			return m.group(1);
		} else {
			return null;
		}
	}
}
