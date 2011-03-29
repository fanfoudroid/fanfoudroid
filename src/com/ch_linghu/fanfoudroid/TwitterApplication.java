package com.ch_linghu.fanfoudroid;

import java.util.HashSet;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.ch_linghu.fanfoudroid.data.db.StatusTable;
import com.ch_linghu.fanfoudroid.data.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.helper.ImageManager;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.ProfileImageCacheManager;
import com.ch_linghu.fanfoudroid.weibo.Configuration;
import com.ch_linghu.fanfoudroid.weibo.Weibo;

public class TwitterApplication extends Application {

    public static final String TAG = "TwitterApplication";
    
    //public static ImageManager mImageManager;
    public static ProfileImageCacheManager mProfileImageCacheManager;
    public static TwitterDatabase mDb;
    public static Weibo mApi; // new API
    public static Context mContext;
    public static SharedPreferences mPref;
    
    public static int networkType = 0;

    private final static boolean DEBUG = Configuration.getDebug();

    
	// 获取登录用户id
    public static String getMyselfId(){
		return mPref.getString(Preferences.CURRENT_USER_ID,
				TwitterApplication.mApi.getUserId());
    }

    @Override
    public void onCreate() {
    	//FIXME: StrictMode类在1.6以下的版本中没有，会导致类加载失败。
    	//       因此将这些代码设成关闭状态，仅在做性能调试时才打开。
//        //NOTE: StrictMode模式需要2.3 API支持。
//        if (DEBUG){
//        	StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//        		.detectAll()
//        		.penaltyLog()
//        		.build());
//        	StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//        		.detectAll()
//        		.penaltyLog()
//        		.build());
//        }


    	super.onCreate();
        
        mContext = this.getApplicationContext();
        //mImageManager = new ImageManager(this);
        mProfileImageCacheManager = new ProfileImageCacheManager();
        mApi = new Weibo();
        mDb = TwitterDatabase.getInstance(this);

        mPref = PreferenceManager
                .getDefaultSharedPreferences(this);
        String username = mPref.getString(Preferences.USERNAME_KEY, "");
        String password = mPref.getString(Preferences.PASSWORD_KEY, "");

        if (Weibo.isValidCredentials(username, password)) {
            mApi.setCredentials(username, password); // Setup API and HttpClient
        }

        // 为cmwap用户设置代理上网
        String type = getNetworkType();
        if (null != type && type.equalsIgnoreCase("cmwap")) {
            Toast.makeText(this, "您当前正在使用cmwap网络上网.", Toast.LENGTH_SHORT);
            mApi.getHttpClient().setProxy("10.0.0.172", 80, "http");
        }
    }

    public String getNetworkType() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        NetworkInfo mobNetInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return  mobNetInfo.getExtraInfo(); // 接入点名称: 此名称可被用户任意更改 如: cmwap, cmnet, internet ...
    }

    @Override
    public void onTerminate() {
        cleanupImages();
        mDb.close();
         Toast.makeText(this, "exit app", Toast.LENGTH_LONG);

        super.onTerminate();
    }

    private void cleanupImages() {
        HashSet<String> keepers = new HashSet<String>();

        Cursor cursor = mDb.fetchAllTweets(StatusTable.TYPE_HOME);

        if (cursor.moveToFirst()) {
            int imageIndex = cursor
                    .getColumnIndexOrThrow(StatusTable.FIELD_PROFILE_IMAGE_URL);
            do {
                keepers.add(cursor.getString(imageIndex));
            } while (cursor.moveToNext());
        }

        cursor.close();

        cursor = mDb.fetchAllDms(-1);

        if (cursor.moveToFirst()) {
            int imageIndex = cursor
                    .getColumnIndexOrThrow(StatusTable.FIELD_PROFILE_IMAGE_URL);
            do {
                keepers.add(cursor.getString(imageIndex));
            } while (cursor.moveToNext());
        }

        cursor.close();

        mProfileImageCacheManager.getImageManager().cleanup(keepers);
    }
}
