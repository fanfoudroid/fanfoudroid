package com.ch_linghu.fanfoudroid;

import java.util.HashSet;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.ch_linghu.fanfoudroid.data.db.TwitterDbAdapter;
import com.ch_linghu.fanfoudroid.helper.ImageManager;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.weibo.User;
import com.ch_linghu.fanfoudroid.weibo.Weibo;

public class TwitterApplication extends Application {
  
  public static final String TAG = "TwitterApplication";
  
  // public ?
  public static ImageManager mImageManager;
  public static TwitterDbAdapter mDb; 
  public static Weibo nApi; // new API
  public static Context mContext;
  private User mUser; // current user

  @Override
  public void onCreate() {
    super.onCreate();

    mImageManager = new ImageManager(this);
    mDb = new TwitterDbAdapter(this);
    mDb.open();
    
    
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);        

    String username = preferences.getString(Preferences.USERNAME_KEY, "");
    String password = preferences.getString(Preferences.PASSWORD_KEY, "");
    
    // Init API with username and password
    nApi = new Weibo(username, password);
    
    mContext = this.getApplicationContext();
  }

  @Override
  public void onTerminate() {
    cleanupImages();
    mDb.close();
//    Toast.makeText(this, "exit app", Toast.LENGTH_LONG);
    
    super.onTerminate();
  }
  
  private void cleanupImages() {
    HashSet<String> keepers = new HashSet<String>();
    
    Cursor cursor = mDb.fetchAllTweets(TwitterDbAdapter.TABLE_TWEET);
    
    if (cursor.moveToFirst()) {
      int imageIndex = cursor.getColumnIndexOrThrow(
          TwitterDbAdapter.KEY_PROFILE_IMAGE_URL);
      do {
        keepers.add(cursor.getString(imageIndex));
      } while (cursor.moveToNext());
    }
    
    cursor.close();
    
    cursor = mDb.fetchAllDms();
    
    if (cursor.moveToFirst()) {
      int imageIndex = cursor.getColumnIndexOrThrow(
          TwitterDbAdapter.KEY_PROFILE_IMAGE_URL);
      do {
        keepers.add(cursor.getString(imageIndex));
      } while (cursor.moveToNext());
    }
    
    cursor.close();
    
    mImageManager.cleanup(keepers);
  }
  
  public User getUser() {
      return mUser;
  }
  
  public void setUser(User user) {
      mUser = user;
  }
    
}
