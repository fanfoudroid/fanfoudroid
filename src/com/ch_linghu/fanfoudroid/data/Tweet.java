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

package com.ch_linghu.fanfoudroid.data;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.helper.Utils;

public class Tweet extends Message {
  private static final String TAG = "Tweet";

  public String source;

  public boolean isReply() {
    // TODO: this is so wrong.
    String username = TwitterApplication.mApi.getUsername();
    Pattern namePattern = Pattern.compile("\\B\\@\\Q" + username + "\\E\\b");
    Matcher matcher = namePattern.matcher(text);

    return matcher.find();
  }

  public static Tweet create(JSONObject jsonObject) throws JSONException {
    Tweet tweet = new Tweet();

    tweet.id = jsonObject.getString("id") + "";
    tweet.text = Utils.decodeTwitterJson(jsonObject.getString("text"));
    tweet.createdAt = Utils.parseDateTime(jsonObject.getString("created_at"));
    tweet.favorited = jsonObject.getString("favorited");
    tweet.inReplyToStatusId = jsonObject.getString("in_reply_to_status_id");
    tweet.inReplyToUserId = jsonObject.getString("in_reply_to_user_id");
    tweet.inReplyToScreenName = jsonObject.getString("in_reply_to_screen_name");
    
    JSONObject user = jsonObject.getJSONObject("user");
    tweet.screenName = Utils.decodeTwitterJson(user.getString("screen_name"));
    tweet.profileImageUrl = user.getString("profile_image_url");
    tweet.userId = user.getString("id");
    
    tweet.source = Utils.decodeTwitterJson(jsonObject.getString("source")).
        replaceAll("\\<.*?>", "");

    return tweet;
  }

  public static Tweet createFromSearchApi(JSONObject jsonObject) throws JSONException {
    Tweet tweet = new Tweet();

    tweet.id = jsonObject.getString("id") + "";
    tweet.text = Utils.decodeTwitterJson(jsonObject.getString("text"));
    tweet.createdAt = Utils.parseSearchApiDateTime(jsonObject.getString("created_at"));
    tweet.favorited = jsonObject.getString("favorited");
    tweet.inReplyToStatusId = jsonObject.getString("in_reply_to_status_id");
    tweet.inReplyToUserId = jsonObject.getString("in_reply_to_user_id");
    tweet.inReplyToScreenName = jsonObject.getString("in_reply_to_screen_name");

    tweet.screenName = Utils.decodeTwitterJson(jsonObject.getString("from_user"));
    tweet.profileImageUrl = jsonObject.getString("profile_image_url");
    tweet.userId = jsonObject.getString("from_user_id");
    tweet.source = Utils.decodeTwitterJson(jsonObject.getString("source")).
        replaceAll("\\<.*?>", "");

    return tweet;
  }

  public static String buildMetaText(StringBuilder builder,
      Date createdAt, String source, String replyTo) {
    builder.setLength(0);

    builder.append(Utils.getRelativeDate(createdAt));
    builder.append(" ");

    builder.append("发送自 ");
    builder.append(source);
    
	if (!Utils.isEmpty(replyTo)) {
		builder.append(" 回复 ");
		builder.append(replyTo);
	}

    return builder.toString();
  }

}
