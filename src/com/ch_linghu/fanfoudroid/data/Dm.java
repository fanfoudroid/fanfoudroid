package com.ch_linghu.fanfoudroid.data;

import org.json.JSONException;
import com.ch_linghu.fanfoudroid.weibo.DirectMessage;
import com.ch_linghu.fanfoudroid.weibo.User;

import com.ch_linghu.fanfoudroid.helper.Utils;

public class Dm extends Message {
  @SuppressWarnings("unused")
  private static final String TAG = "Dm";

  public boolean isSent;

  public static Dm create(DirectMessage directMessage, boolean isSent){
    Dm dm = new Dm();

    dm.id = directMessage.getId();
    dm.text = Utils.decodeTwitterJson(directMessage.getText());
    dm.createdAt = directMessage.getCreatedAt();
    dm.isSent = isSent;

    User user = dm.isSent ? directMessage.getRecipient()
        : directMessage.getSender();
    dm.screenName = Utils.decodeTwitterJson(user.getScreenName());
    dm.userId = user.getId();    
    dm.profileImageUrl = user.getProfileImageURL().toString();

    return dm;
  }
}