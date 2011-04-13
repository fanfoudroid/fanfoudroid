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
import com.ch_linghu.fanfoudroid.service.WidgetService;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.appwidget.AppWidgetManager;

public class FanfouWidget extends AppWidgetProvider {
	public final String TAG = "com.ch_linghu.fanfoudroid.FanfouWidget";
	private List<Tweet> tweets;
	private int position = 0;

	@Override
	public void onUpdate(Context context, AppWidgetManager appwidgetmanager,
			int[] appWidgetIds) {
		Log.i(TAG, "onUpdate");
		if (!isRunning(context, WidgetService.class.getName())) {
			Intent i = new Intent(context, WidgetService.class);
			context.startService(i);
		}

		/*
		 * position = 0; fetchMessages(); Timer timer = new Timer();
		 * timer.scheduleAtFixedRate(new MyTime(context, appwidgetmanager), 1,
		 * 6000); super.onUpdate(context, appwidgetmanager, appWidgetIds);
		 */

	}

	public boolean isRunning(Context c, String serviceName) {
		ActivityManager myAM = (ActivityManager) c
				.getSystemService(Context.ACTIVITY_SERVICE);

		ArrayList<RunningServiceInfo> runningServices = (ArrayList<RunningServiceInfo>) myAM
				.getRunningServices(40);
		// 获取最多40个当前正在运行的服务，放进ArrList里,以现在手机的处理能力，要是超过40个服务，估计已经卡死，所以不用考虑超过40个该怎么办
		int servicesSize=runningServices.size();
		for (int i = 0; i <servicesSize ; i++)// 循环枚举对比
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
		Intent i = new Intent(context, WidgetService.class);
		context.startService(i);
	}

	@Override
	public void onDisabled(Context context) {
		Log.i(TAG, "onDisabled");
		Intent i = new Intent(context, WidgetService.class);
		context.stopService(i);

	}
}
