package com.ch_linghu.android.fanfoudroid;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

  public String id = "";
  public String name = "";
  public String screenName = "";
  public String location = "";
  public String description = "";
  public String profileImageUrl = "";
  public String url = "";

  public static User create(JSONObject jsonObject) throws JSONException {
    User user = new User();
    
    user.id = jsonObject.getString("id");    
    user.name = Utils.decodeTwitterJson(jsonObject.getString("name"));
    user.screenName = Utils.decodeTwitterJson(jsonObject.getString("screen_name"));
    user.location = Utils.decodeTwitterJson(jsonObject.getString("location"));
    user.description = Utils.decodeTwitterJson(jsonObject.getString("description"));
    user.profileImageUrl = jsonObject.getString("profile_image_url");
    user.url = jsonObject.getString("url");
        
    return user;
  }  
}
