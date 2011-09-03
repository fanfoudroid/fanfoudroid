package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.ch_linghu.fanfoudroid.app.LazyImageLoader.ImageLoaderCallback;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.db.StatusTable;
import com.ch_linghu.fanfoudroid.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.service.TwitterService;
import com.ch_linghu.fanfoudroid.util.DateTimeHelper;
import com.ch_linghu.fanfoudroid.util.TextHelper;
import com.ch_linghu.fanfoudroid.R;

public class FanfouWidget extends AppWidgetProvider {
	public final String TAG = "FanfouWidget";
	public final String NEXTACTION = "com.ch_linghu.fanfoudroid.FanfouWidget.NEXT";
	public final String PREACTION = "com.ch_linghu.fanfoudroid.FanfouWidget.PREV";
	private static List<Tweet> tweets;
	private SensorManager sensorManager;
	private static int position = 0;

	class CacheCallback implements ImageLoaderCallback {
		private RemoteViews updateViews;

		CacheCallback(RemoteViews updateViews) {
			this.updateViews = updateViews;
		}

		@Override
		public void refresh(String url, Bitmap bitmap) {
			updateViews.setImageViewBitmap(R.id.status_image, bitmap);
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appwidgetmanager,
			int[] appWidgetIds) {
		Log.d(TAG, "onUpdate");
		TwitterService.setWidgetStatus(true);
		// if (!isRunning(context, WidgetService.class.getName())) {
		// Intent i = new Intent(context, WidgetService.class);
		// context.startService(i);
		// }

		update(context);

	}

	private void update(Context context) {

		fetchMessages();
		position = 0;
		refreshView(context, NEXTACTION);
	}

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
		Log.d(TAG, "Tweets size " + tweets.size());
	}

	private void refreshView(Context context) {
		ComponentName fanfouWidget = new ComponentName(context,
				FanfouWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(fanfouWidget, buildLogin(context));
	}

	private RemoteViews buildLogin(Context context) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_initial_layout);
		updateViews.setTextViewText(R.id.status_text,
				TextHelper.getSimpleTweetText("请登录"));

		updateViews.setTextViewText(R.id.status_screen_name, "");

		updateViews.setTextViewText(R.id.tweet_source, "");
		updateViews.setTextViewText(R.id.tweet_created_at, "");
		return updateViews;
	}

	private void refreshView(Context context, String action) {
		// 某些情况下，tweets会为null
		if (tweets == null) {
			fetchMessages();
		}
		// 防止引发IndexOutOfBoundsException
		if (tweets.size() != 0) {
			if (action.equals(NEXTACTION)) {
				--position;
			} else if (action.equals(PREACTION)) {
				++position;
			}
			// Log.d(TAG, "Tweets size =" + tweets.size());
			if (position >= tweets.size() || position < 0) {
				position = 0;
			}
			// Log.d(TAG, "position=" + position);
			ComponentName fanfouWidget = new ComponentName(context,
					FanfouWidget.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			manager.updateAppWidget(fanfouWidget, buildUpdate(context));
		}
	}

	public RemoteViews buildUpdate(Context context) {

		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_initial_layout);

		Tweet t = tweets.get(position);
		Log.d(TAG, "tweet=" + t);

		updateViews.setTextViewText(R.id.status_screen_name, t.screenName);
		updateViews.setTextViewText(R.id.status_text,
				TextHelper.getSimpleTweetText(t.text));

		updateViews.setTextViewText(R.id.tweet_source,
				context.getString(R.string.tweet_source_prefix) + t.source);
		updateViews.setTextViewText(R.id.tweet_created_at,
				DateTimeHelper.getRelativeDate(t.createdAt));

		updateViews.setImageViewBitmap(R.id.status_image,
				TwitterApplication.mImageLoader.get(t.profileImageUrl,
						new CacheCallback(updateViews)));

		Intent inext = new Intent(context, FanfouWidget.class);
		inext.setAction(NEXTACTION);
		PendingIntent pinext = PendingIntent.getBroadcast(context, 0, inext,
				PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.btn_next, pinext);
		Intent ipre = new Intent(context, FanfouWidget.class);
		ipre.setAction(PREACTION);
		PendingIntent pipre = PendingIntent.getBroadcast(context, 0, ipre,
				PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.btn_pre, pipre);

		Intent write = WriteActivity.createNewTweetIntent("");
		PendingIntent piwrite = PendingIntent.getActivity(context, 0, write,
				PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.write_message, piwrite);

		Intent home = TwitterActivity.createIntent(context);
		PendingIntent pihome = PendingIntent.getActivity(context, 0, home,
				PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.logo_image, pihome);

		Intent status = StatusActivity.createIntent(t);
		PendingIntent pistatus = PendingIntent.getActivity(context, 0, status,
				PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.main_body, pistatus);

		return updateViews;

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "OnReceive");

		// FIXME: NullPointerException
		Log.i(TAG, context.getApplicationContext().toString());
		if (!TwitterApplication.mApi.isLoggedIn()) {
			refreshView(context);
		} else {
			super.onReceive(context, intent);
			String action = intent.getAction();

			if (NEXTACTION.equals(action) || PREACTION.equals(action)) {
				refreshView(context, intent.getAction());
			} else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
				update(context);
			}
		}
	}

	/**
	 * 
	 * @param c
	 * @param serviceName
	 * @return
	 */
	@Deprecated
	public boolean isRunning(Context c, String serviceName) {
		ActivityManager myAM = (ActivityManager) c
				.getSystemService(Context.ACTIVITY_SERVICE);

		ArrayList<RunningServiceInfo> runningServices = (ArrayList<RunningServiceInfo>) myAM
				.getRunningServices(40);
		// 获取最多40个当前正在运行的服务，放进ArrList里,以现在手机的处理能力，要是超过40个服务，估计已经卡死，所以不用考虑超过40个该怎么办
		int servicesSize = runningServices.size();
		for (int i = 0; i < servicesSize; i++)// 循环枚举对比
		{
			if (runningServices.get(i).service.getClassName().toString()
					.equals(serviceName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.d(TAG, "onDeleted");
	}

	@Override
	public void onEnabled(Context context) {
		Log.d(TAG, "onEnabled");

		TwitterService.setWidgetStatus(true);

	}

	@Override
	public void onDisabled(Context context) {
		Log.d(TAG, "onDisabled");

		TwitterService.setWidgetStatus(false);
	}
}
