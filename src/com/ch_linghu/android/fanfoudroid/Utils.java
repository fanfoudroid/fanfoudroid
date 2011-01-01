/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ch_linghu.android.fanfoudroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class Utils {

  private static final String TAG = "Utils";

  public static boolean isEmpty(String s) {
    return s == null || s.length() == 0;
  }

  private static HashMap<String, String> _userLinkMapping = new HashMap<String, String>();
  
  public static String stringifyStream(InputStream is) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();
    String line;

    while ((line = reader.readLine()) != null) {
      sb.append(line + "\n");
    }

    return sb.toString();
  }

  // Twitter JSON encodes < and > to prevent XSS attacks on websites.
  public static String decodeTwitterJson(String s) {
    return s.replace("&lt;", "<").replace("&gt;", ">");
  }
  
//Wed Dec 15 02:53:36 +0000 2010
  public static final DateFormat TWITTER_DATE_FORMATTER = new SimpleDateFormat(
      "E MMM d HH:mm:ss Z yyyy", Locale.US);

  public static final DateFormat TWITTER_SEARCH_API_DATE_FORMATTER = new SimpleDateFormat(
      "E, d MMM yyyy HH:mm:ss Z", Locale.US);

  public static final Date parseDateTime(String dateString) {
    try {
    	Log.d(TAG, String.format("in parseDateTime, dateString=%s", dateString));
      return TWITTER_DATE_FORMATTER.parse(dateString);
    } catch (ParseException e) {
      Log.w(TAG, "Could not parse Twitter date string: " + dateString);
      return null;
    }
  }

  public static final Date parseSearchApiDateTime(String dateString) {
    try {
      return TWITTER_SEARCH_API_DATE_FORMATTER.parse(dateString);
    } catch (ParseException e) {
      Log.w(TAG, "Could not parse Twitter search date string: " + dateString);
      return null;
    }
  }

  public static final DateFormat AGO_FULL_DATE_FORMATTER = new SimpleDateFormat(
      "h:mm a MMM d");

  public static String getRelativeDate(Date date) {
    Date now = new Date();

    // Seconds.
    long diff = (now.getTime() - date.getTime()) / 1000;

    if (diff < 0) {
      diff = 0;
    }

    if (diff < 60) {
      return diff + "秒前";
    }

    // Minutes.
    diff /= 60;

    if (diff < 60) {
      return "大约" + diff + "分钟前";
    }

    // Hours.
    diff /= 60;

    if (diff < 24) {
      return "大约" + diff + "小时前";
    }

    return AGO_FULL_DATE_FORMATTER.format(date);
  }

  public static long getNowTime() {
    return Calendar.getInstance().getTime().getTime();
  }

  private static final Pattern NAME_MATCHER = Pattern.compile("@.+?\\s");
  private static final Linkify.MatchFilter NAME_MATCHER_MATCH_FILTER = new Linkify.MatchFilter() {
    @Override
    public final boolean acceptMatch(final CharSequence s, final int start,
        final int end) {

		String name = s.subSequence(start+1, end).toString().trim();
		boolean result = _userLinkMapping.containsKey(name);
		return result;
    }
  };
  
  private static final Linkify.TransformFilter NAME_MATCHER_TRANSFORM_FILTER = new Linkify.TransformFilter() {
	
	@Override
	public String transformUrl(Matcher match, String url) {
		// TODO Auto-generated method stub
		String name = url.subSequence(1, url.length()).toString().trim();
		return _userLinkMapping.get(name);
	}
};

  private static final String TWITTA_USER_URL = "twitta://users/";

  public static void linkifyUsers(TextView view) {
    Linkify.addLinks(view, NAME_MATCHER, TWITTA_USER_URL,
        NAME_MATCHER_MATCH_FILTER, NAME_MATCHER_TRANSFORM_FILTER);
  }

  private static final Pattern TAG_MATCHER = Pattern.compile("#\\w+#");

  private static final Linkify.TransformFilter TAG_MATCHER_TRANSFORM_FILTER =
      new Linkify.TransformFilter() {
    @Override
    public final String transformUrl(Matcher match, String url) {
      String result = url.substring(1, url.length()-1);
      return "%23" + result + "%23";
    }
  };

  private static final String TWITTA_SEARCH_URL = "twitta://search/";

  public static void linkifyTags(TextView view) {
    Linkify.addLinks(view, TAG_MATCHER, TWITTA_SEARCH_URL,
        null, TAG_MATCHER_TRANSFORM_FILTER);
  }

  public static boolean isTrue(Bundle bundle, String key) {
    return bundle != null && bundle.containsKey(key) && bundle.getBoolean(key);
  }
  
  public static String preprocessText(String text){
	  //处理HTML格式返回的用户链接
	  Pattern p = Pattern.compile("@<a href=\"http:\\/\\/fanfou\\.com\\/(.*?)\" class=\"former\">(.*?)<\\/a>");
	  Matcher m = p.matcher(text);
	  while(m.find()){
		  _userLinkMapping.put(m.group(2), m.group(1));
		  Log.d(TAG, String.format("Found mapping! %s=%s", m.group(2), m.group(1)));
	  }
	  return text.replaceAll("<.*?>", "");
  }

  public static void setTweetText(TextView textView, String text) {
    String processedText = preprocessText(text);
	textView.setText(processedText, BufferType.SPANNABLE);
    Linkify.addLinks(textView, Linkify.WEB_URLS);
    Utils.linkifyUsers(textView);
    Utils.linkifyTags(textView);
    _userLinkMapping.clear();
  }
}
