package com.ch_linghu.fanfoudroid;

import java.util.HashSet;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.ch_linghu.fanfoudroid.data.db.StatusTable;
import com.ch_linghu.fanfoudroid.data.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.ProfileImageCacheManager;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.weibo.Configuration;
import com.ch_linghu.fanfoudroid.weibo.Weibo;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey="dHowMk5LMXQweVJkWGthb1E1T1NUUHc6MQ",
    mode = ReportingInteractionMode.NOTIFICATION,
    resNotifTickerText = R.string.crash_notif_ticker_text,
    resNotifTitle = R.string.crash_notif_title,
    resNotifText = R.string.crash_notif_text,
    resNotifIcon = android.R.drawable.stat_notify_error, // optional. default is a warning sign
    resDialogText = R.string.crash_dialog_text,
    resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
    resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
    resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
    resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
)
public class TwitterApplication extends Application {

	public static final String TAG = "TwitterApplication";

	// public static ImageManager mImageManager;
	public static ProfileImageCacheManager mProfileImageCacheManager;
	public static TwitterDatabase mDb;
	public static Weibo mApi; // new API
	public static Context mContext;
	public static SharedPreferences mPref;

	public static int networkType = 0;

	private final static boolean DEBUG = Configuration.getDebug();

	// 获取登录用户id, 据肉眼观察，刚注册的用户系统分配id都是~开头的，因为不知道
	// 用户何时去修改这个ID，目前只有把所有以~开头的ID在每次需要UserId时都去服务器
	// 取一次数据，看看新的ID是否已经设定，判断依据是是否以~开头。这么判断会把有些用户
	// 就是把自己ID设置的以~开头的造成,每次都需要去服务器取数。
	// 只是简单处理了mPref没有CURRENT_USER_ID的情况，因为用户在登陆时，肯定会记一个CURRENT_USER_ID
	// 到mPref.
	public static String getMyselfId() {
		String lastestUserID = "";
		if (!mPref.contains(Preferences.CURRENT_USER_ID)) {
			try {
				lastestUserID = TwitterApplication.mApi.showUser(
						TwitterApplication.mApi.getUserId()).getId();
			} catch (HttpException e) {
				e.printStackTrace();
				// mPref没有CURRENT_USER_ID属于异常
				return null;
			}
			TwitterApplication.mPref.edit().putString(
					Preferences.CURRENT_USER_ID, lastestUserID);
			TwitterApplication.mPref.edit().commit();
		} else if (mPref.contains(Preferences.CURRENT_USER_ID)
				&& !mPref.getString(Preferences.CURRENT_USER_ID, "~")
						.startsWith("~")) {
			lastestUserID = mPref.getString(Preferences.CURRENT_USER_ID,
					lastestUserID);
		} else if (mPref.contains(Preferences.CURRENT_USER_ID)
				&& mPref.getString(Preferences.CURRENT_USER_ID, "~")
						.startsWith("~")) {
			try {
				lastestUserID = TwitterApplication.mApi.showUser(
						TwitterApplication.mApi.getUserId()).getId();
			} catch (HttpException e) {
				e.printStackTrace();
				return mPref.getString(Preferences.CURRENT_USER_ID, "~");
			}
			if (!lastestUserID.startsWith("~")) {
				TwitterApplication.mPref.edit().putString(
						Preferences.CURRENT_USER_ID, lastestUserID);
				TwitterApplication.mPref.edit().commit();
			}
		}
		return lastestUserID;
	}

	@Override
	public void onCreate() {
		// FIXME: StrictMode类在1.6以下的版本中没有，会导致类加载失败。
		// 因此将这些代码设成关闭状态，仅在做性能调试时才打开。
		// //NOTE: StrictMode模式需要2.3 API支持。
		// if (DEBUG){
		// StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		// .detectAll()
		// .penaltyLog()
		// .build());
		// StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		// .detectAll()
		// .penaltyLog()
		// .build());
		// }

		super.onCreate();

		mContext = this.getApplicationContext();
		// mImageManager = new ImageManager(this);
		mProfileImageCacheManager = new ProfileImageCacheManager();
		mApi = new Weibo();
		mDb = TwitterDatabase.getInstance(this);

		mPref = PreferenceManager.getDefaultSharedPreferences(this);
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
		return mobNetInfo.getExtraInfo(); // 接入点名称: 此名称可被用户任意更改 如: cmwap, cmnet,
											// internet ...
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
