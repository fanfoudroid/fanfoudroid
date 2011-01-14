package com.ch_linghu.fanfoudroid.data;

import com.ch_linghu.fanfoudroid.helper.Utils;

public class User {

  public String id = "";
  public String name = "";
  public String screenName = "";
  public String location = "";
  public String description = "";
  public String profileImageUrl = "";
  public String url = "";

  public static User create(com.ch_linghu.fanfoudroid.weibo.User u){
    User user = new User();
    
    user.id = u.getId();    
    user.name = Utils.decodeTwitterJson(u.getName());
    user.screenName = Utils.decodeTwitterJson(u.getScreenName());
    user.location = Utils.decodeTwitterJson(u.getLocation());
    user.description = Utils.decodeTwitterJson(u.getDescription());
    user.profileImageUrl = u.getProfileImageURL().toString();
    user.url = u.getURL().toString();
        
    return user;
  }  
}
