package com.ch_linghu.fanfoudroid;

import java.util.HashSet;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.ch_linghu.fanfoudroid.data.db.StatusDatabase;
import com.ch_linghu.fanfoudroid.data.db.StatusTablesInfo.StatusTable;
import com.ch_linghu.fanfoudroid.helper.ImageManager;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.weibo.Weibo;

public class TwitterApplication extends Application {
  
  public static final String TAG = "TwitterApplication";
  
  public static ImageManager mImageManager;
  public static StatusDatabase mDb;
  public static Weibo mApi; // new API
  public static Context mContext;
  

  @Override
  public void onCreate() {
    super.onCreate();
    

    mContext = this.getApplicationContext();
    mImageManager = new ImageManager(this);
    mApi = new Weibo();
    mDb = StatusDatabase.getInstance(this);
    
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);        
    String username = preferences.getString(Preferences.USERNAME_KEY, "");
    String password = preferences.getString(Preferences.PASSWORD_KEY, "");
    
    if (Weibo.isValidCredentials(username, password)) {
        mApi.setCredentials(username, password); // Setup API and HttpClient 
    }
    
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
    
    Cursor cursor = mDb.fetchAllTweets(StatusTable.TYPE_HOME);
    
    if (cursor.moveToFirst()) {
      int imageIndex = cursor.getColumnIndexOrThrow(
          StatusTable.FIELD_PROFILE_IMAGE_URL);
      do {
        keepers.add(cursor.getString(imageIndex));
      } while (cursor.moveToNext());
    }
    
    cursor.close();
    
    cursor = mDb.fetchAllDms(-1);
    
    if (cursor.moveToFirst()) {
      int imageIndex = cursor.getColumnIndexOrThrow(
            StatusTable.FIELD_PROFILE_IMAGE_URL);
      do {
        keepers.add(cursor.getString(imageIndex));
      } while (cursor.moveToNext());
    }
    
    cursor.close();
    
    mImageManager.cleanup(keepers);
  }
}
