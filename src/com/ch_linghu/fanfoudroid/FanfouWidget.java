package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.db.StatusTable;
import com.ch_linghu.fanfoudroid.data.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.service.TwitterService;

import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.appwidget.AppWidgetManager;

public class FanfouWidget extends AppWidgetProvider {
	public final String TAG="com.ch_linghu.fanfoudroid.FanfouWidget";
	private List<Tweet> tweets;
	private int position = 0;

	@Override
	public void onUpdate(Context context, AppWidgetManager appwidgetmanager,
			int[] appWidgetIds) {
		Log.i(TAG, "onUpdate");
	

		 
		/*position = 0;
		fetchMessages();
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new MyTime(context, appwidgetmanager), 1,
				6000);
		super.onUpdate(context, appwidgetmanager, appWidgetIds);
		*/


	}

	private class MyTime extends TimerTask {
		RemoteViews remoteViews;
		AppWidgetManager appWidgetManager;
		ComponentName thisWidget;
		Context c;

		public MyTime(Context context, AppWidgetManager appWidgetManager) {
			this.appWidgetManager = appWidgetManager;
	
			this.c = context;
			thisWidget = new ComponentName(context, FanfouWidget.class);
		}

		@Override
		public void run() {
			position++;
			this.appWidgetManager.updateAppWidget(thisWidget, buildUpdate(this.c));
		}

	}

	public RemoteViews buildUpdate(Context context) {

		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_initial_layout);
		updateViews
				.setTextViewText(R.id.status_text, tweets.get(position).text);
		return updateViews;

	}

	public TwitterDatabase getDb() {
		return TwitterApplication.mDb;
	}

	public String getUserId() {
		return TwitterApplication.getMyselfId();
	}

	private void fetchMessages() {
		if (tweets == null) {
			tweets = new ArrayList<Tweet>();

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

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.i(TAG, "onDeleted");
	}

	@Override
	public void onEnabled(Context context) {
		Log.i(TAG, "onEnabled");
		tweets = new ArrayList<Tweet>();
	}

	@Override
	public void onDisabled(Context context) {
		Log.i(TAG, "onDisabled");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive");
		Intent i=new Intent(context,TwitterService.class);
		context.startService(i);
	}
}
