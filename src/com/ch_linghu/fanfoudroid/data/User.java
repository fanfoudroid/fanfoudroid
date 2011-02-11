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
  public String lastStatus;

  public static User create(com.ch_linghu.fanfoudroid.weibo.User u){
    User user = new User();
    
    user.id = u.getId();    
    user.name = Utils.getSimpleTweetText(u.getName());
    user.screenName = Utils.getSimpleTweetText(u.getScreenName());
    user.location = Utils.getSimpleTweetText(u.getLocation());
    user.description = Utils.getSimpleTweetText(u.getDescription());
    user.profileImageUrl = u.getProfileImageURL().toString();
    if (u.getURL() != null){
    	user.url = u.getURL().toString();
    }
    user.lastStatus = u.getStatusText();
        
    return user;
  }  
}
