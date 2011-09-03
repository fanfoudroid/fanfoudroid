package com.ch_linghu.fanfoudroid.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.ch_linghu.fanfoudroid.FanfouWidget;
import com.ch_linghu.fanfoudroid.FanfouWidgetSmall;
import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.app.Preferences;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.db.StatusTable;
import com.ch_linghu.fanfoudroid.db.TwitterDatabase;

public class WidgetService extends Service {
	protected static final String TAG = "WidgetService";

	private int position = 0;

	private List<Tweet> tweets;

	private TwitterDatabase getDb() {
		return TwitterApplication.mDb;
	}

	public String getUserId() {
		return TwitterApplication.getMyselfId(false);
	}

	private void fetchMessages() {
		if (tweets == null) {
			tweets = new ArrayList<Tweet>();

		} else {
			tweets.clear();
		}
		Cursor cursor = getDb().fetchAllTweets(getUserId(),
				StatusTable.TYPE_HOME);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					Tweet tweet = StatusTable.parseCursor(cursor);
					tweets.add(tweet);
				} while (cursor.moveToNext());
			}
		}
	}

	public RemoteViews buildUpdate(Context context) {

		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_initial_layout);
		updateViews
				.setTextViewText(R.id.status_text, tweets.get(position).text);
		// updateViews.setOnClickPendingIntent(viewId, pendingIntent)

		position++;
		return updateViews;

	}

	private Handler handler = new Handler();

	private Runnable mTask = new Runnable() {

		@Override
		public void run() {

			Log.d(TAG, "tweets size=" + tweets.size() + "  position="
					+ position);
			if (position >= tweets.size()) {
				position = 0;
			}
			ComponentName fanfouWidget = new ComponentName(WidgetService.this,
					FanfouWidget.class);
			AppWidgetManager manager = AppWidgetManager
					.getInstance(getBaseContext());
			manager.updateAppWidget(fanfouWidget,
					buildUpdate(WidgetService.this));
			handler.postDelayed(mTask, 10000);

			ComponentName fanfouWidgetSmall = new ComponentName(
					WidgetService.this, FanfouWidgetSmall.class);
			AppWidgetManager manager2 = AppWidgetManager
					.getInstance(getBaseContext());
			manager2.updateAppWidget(fanfouWidgetSmall,
					buildUpdate(WidgetService.this));
			handler.postDelayed(mTask, 10000);
		}

	};

	public static void schedule(Context context) {
		SharedPreferences preferences = TwitterApplication.mPref;

		if (!preferences.getBoolean(Preferences.CHECK_UPDATES_KEY, false)) {
			Log.d(TAG, "Check update preference is false.");
			return;
		}

		String intervalPref = preferences
				.getString(
						Preferences.CHECK_UPDATE_INTERVAL_KEY,
						context.getString(R.string.pref_check_updates_interval_default));
		int interval = Integer.parseInt(intervalPref);

		Intent intent = new Intent(context, WidgetService.class);
		PendingIntent pending = PendingIntent.getService(context, 0, intent, 0);
		Calendar c = new GregorianCalendar();
		c.add(Calendar.MINUTE, interval);

		DateFormat df = new SimpleDateFormat("h:mm a");
		Log.d(TAG, "Scheduling alarm at " + df.format(c.getTime()));

		AlarmManager alarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pending);
		alarm.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pending);
	}

	/**
	 * @see android.app.Service#onBind(Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Put your code here
		return null;
	}

	/**
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		Log.d(TAG, "WidgetService onCreate");
		schedule(WidgetService.this);

	}

	/**
	 * @see android.app.Service#onStart(Intent,int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "WidgetService onStart");

		fetchMessages();
		handler.removeCallbacks(mTask);
		handler.postDelayed(mTask, 10000);

	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "WidgetService Stop ");
		handler.removeCallbacks(mTask);// 当服务结束时，删除线程
		super.onDestroy();
	}
}
