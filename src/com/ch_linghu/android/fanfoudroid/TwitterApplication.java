package com.ch_linghu.android.fanfoudroid;

import java.util.HashSet;

import android.app.Application;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

public class TwitterApplication extends Application {
  
  public static final String TAG = "TwitterApplication";
  
  public static ImageManager mImageManager;
  public static TwitterDbAdapter mDb; 
  public static TwitterApi mApi;

  @Override
  public void onCreate() {
    super.onCreate();
    Log.i(TAG, "Application onCreate.");

    mImageManager = new ImageManager(this);
    mDb = new TwitterDbAdapter(this);
    mDb.open();
    
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);        

    String username = preferences.getString(Preferences.USERNAME_KEY, "");
    String password = preferences.getString(Preferences.PASSWORD_KEY, "");
    
    if (TwitterApi.isValidCredentials(username, password)) {
      mApi = new TwitterApi(username, password);
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
