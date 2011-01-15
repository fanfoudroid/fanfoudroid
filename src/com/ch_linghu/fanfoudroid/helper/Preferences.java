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

package com.ch_linghu.fanfoudroid.helper;

public class Preferences {
  public static final String USERNAME_KEY = "username";
  public static final String PASSWORD_KEY = "password";
  public static final String CHECK_UPDATES_KEY = "check_updates";
  public static final String CHECK_UPDATE_INTERVAL_KEY =
      "check_update_interval";
  public static final String VIBRATE_KEY = "vibrate";
  public static final String TIMELINE_ONLY_KEY = "timeline_only";
  public static final String REPLIES_ONLY_KEY = "replies_only";
  public static final String DM_ONLY_KEY = "dm_only";
  
  public static String RINGTONE_KEY = "ringtone";
  public static final String RINGTONE_DEFAULT_KEY =
      "content://settings/system/notification_sound";  
  
  public static final String LAST_TWEET_REFRESH_KEY = "last_tweet_refresh";
  public static final String LAST_DM_REFRESH_KEY = "last_dm_refresh";
  public static final String LAST_FOLLOWERS_REFRESH_KEY =
      "last_followers_refresh";

  public static final String TWITTER_ACTIVITY_STATE_KEY =
      "twitter_activity_state";
  
  public static final String RT_PREFIX_KEY = "rt_prefix";
  public static final String RT_INSERT_APPEND = "rt_insert_append"; //转发时光标放置在开始还是结尾 
}
