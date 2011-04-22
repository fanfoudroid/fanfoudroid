package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.db.StatusTable;
import com.ch_linghu.fanfoudroid.data.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.helper.ProfileImageCacheCallback;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.service.TwitterService;
import com.ch_linghu.fanfoudroid.service.WidgetService;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.RemoteViews;
import android.appwidget.AppWidgetManager;

public class FanfouWidget extends AppWidgetProvider {
	public final String TAG = "com.ch_linghu.fanfoudroid.FanfouWidget";
	public final String NEXTACTION = "com.ch_linghu.fanfoudroid.FanfouWidget.NEXT";
	public final String PREACTION = "com.ch_linghu.fanfoudroid.FanfouWidget.PRE";
	private static List<Tweet> tweets;
	private static int position = 0;

	class CacheCallback implements ProfileImageCacheCallback{
		private RemoteViews updateViews;
		
		CacheCallback(RemoteViews updateViews){
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
		Log.i(TAG, "onUpdate");
		// if (!isRunning(context, WidgetService.class.getName())) {
		// Intent i = new Intent(context, WidgetService.class);
		// context.startService(i);
		// }
		
		fetchMessages();
		position = 0;
		refreshView(context, NEXTACTION);

	}

	private TwitterDatabase getDb() {
		return TwitterApplication.mDb;
	}

	public String getUserId() {
		return TwitterApplication.getMyselfId();
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
		Log.i(TAG, "Tweets size " + tweets.size());
	}

	private void refreshView(Context context, String action) {

		if (action.equals(NEXTACTION)) {
			--position;
		} else if (action.equals(PREACTION)) {
			++position;
		}
		Log.i(TAG, "Tweets size =" + tweets.size());
		if (position >= tweets.size() || position < 0) {
			position = 0;
		}
		ComponentName fanfouWidget = new ComponentName(context,
				FanfouWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(fanfouWidget, buildUpdate(context));
	}

	public RemoteViews buildUpdate(Context context) {

		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_initial_layout);

		Tweet t = tweets.get(position);

		updateViews.setTextViewText(R.id.status_screen_name, t.screenName+":");
		updateViews.setTextViewText(R.id.status_text, Utils.getSimpleTweetText(t.text));

		updateViews.setTextViewText(R.id.tweet_source, context.getString(R.string.tweet_source_prefix) + t.source);
		updateViews.setTextViewText(R.id.tweet_created_at, Utils.getRelativeDate(t.createdAt));
		
		updateViews.setImageViewBitmap(R.id.status_image,
				TwitterApplication.mProfileImageCacheManager.get(
						t.profileImageUrl, new CacheCallback(updateViews)));

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
		PendingIntent piwrite = PendingIntent.getActivity(context, 0, write,PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.write_message, piwrite);
		
		Intent home = TwitterActivity.createIntent(context);
		PendingIntent pihome = PendingIntent.getActivity(context, 0, home, PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.logo_image, pihome);
		
		Intent status = StatusActivity.createIntent(t);
		PendingIntent pistatus=PendingIntent.getActivity(context, 0, status, PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.main_body, pistatus);
		
		return updateViews;

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "OnReceive");
//		Log.i(TAG, "tweets is null?" + tweets);
//		Log.i(TAG, "Position is " + position);
		String action = intent.getAction();
		if (NEXTACTION.equals(action) || PREACTION.equals(action)) {
			Log.i(TAG, "action is" + intent.getAction());
			refreshView(context, intent.getAction());
		} else {
			super.onReceive(context, intent);
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
		Log.i(TAG, "onDeleted");
	}

	@Override
	public void onEnabled(Context context) {
		Log.i(TAG, "onEnabled");
		
		TwitterService.setWidgetStatus(true);
	}

	@Override
	public void onDisabled(Context context) {
		Log.i(TAG, "onDisabled");

		TwitterService.setWidgetStatus(false);
	}
}
