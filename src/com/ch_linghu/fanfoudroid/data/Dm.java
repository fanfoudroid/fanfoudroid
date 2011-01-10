package com.ch_linghu.fanfoudroid.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.ch_linghu.fanfoudroid.helper.Utils;

public class Dm extends Message {
  @SuppressWarnings("unused")
  private static final String TAG = "Dm";

  public boolean isSent;

  public static Dm create(JSONObject jsonObject, boolean isSent)
      throws JSONException {
    Dm dm = new Dm();

    dm.id = jsonObject.getString("id") + "";
    dm.text = Utils.decodeTwitterJson(jsonObject.getString("text"));
    dm.createdAt = Utils.parseDateTime(jsonObject.getString("created_at"));
    dm.isSent = isSent;

    JSONObject user = dm.isSent ? jsonObject.getJSONObject("recipient")
        : jsonObject.getJSONObject("sender");
    dm.screenName = Utils.decodeTwitterJson(user.getString("screen_name"));
    dm.userId = user.getString("id");    
    dm.profileImageUrl = user.getString("profile_image_url");

    return dm;
  }
}