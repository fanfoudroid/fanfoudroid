package com.ch_linghu.android.fanfoudroid;

import java.util.HashSet;

import com.ch_linghu.android.fanfoudroid.data.db.TwitterDbAdapter;
import com.ch_linghu.android.fanfoudroid.helper.ImageManager;
import com.ch_linghu.android.fanfoudroid.helper.Preferences;

import tk.sandin.android.fanfoudroid.weibo.Weibo;
import android.app.Application;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

public class TwitterApplication extends Application {
  
  public static final String TAG = "TwitterApplication";
  
  // public ?
  public static ImageManager mImageManager;
  public static TwitterDbAdapter mDb; 
  public static TwitterApi mApi;
  public static Weibo nApi; // new API

  @Override
  public void onCreate() {
    super.onCreate();

    mImageManager = new ImageManager(this);
    mDb = new TwitterDbAdapter(this);
    mDb.open();
    mApi = new TwitterApi();
    
    
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);        

    String username = preferences.getString(Preferences.USERNAME_KEY, "");
    String password = preferences.getString(Preferences.PASSWORD_KEY, "");
    
    // Init API with username and password
    nApi = new Weibo(username, password);
    
    if (TwitterApi.isValidCredentials(username, password)) {
      mApi.setCredentials(username, password);
    }
  }

  @Override
  public void onTerminate() {
    cleanupImages();
    mDb.close();
    
    super.onTerminate();
  }
  
  private void cleanupImages() {
    HashSet<String> keepers = new HashSet<String>();
    
    Cursor cursor = mDb.fetchAllTweets();
    
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
    
}
