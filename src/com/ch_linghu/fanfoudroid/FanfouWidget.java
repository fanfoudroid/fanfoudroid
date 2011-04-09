package com.ch_linghu.fanfoudroid;

import java.util.Date;

import com.ch_linghu.fanfoudroid.service.TwitterService;

import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.widget.RemoteViews;
import android.appwidget.AppWidgetManager;

public class FanfouWidget extends AppWidgetProvider {
	private String messageText;

	@Override
	public void onUpdate(Context context, AppWidgetManager appwidgetmanager,
			int[] appWidgetIds) {
		ComponentName fanfouWidget = new ComponentName(context,
				FanfouWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(fanfouWidget, buildUpdate(context));

	}

	public RemoteViews buildUpdate(Context context) {

		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_initial_layout);
		updateViews.setTextViewText(R.id.message, messageText);
		return updateViews;

	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Put your code here
	}

	@Override
	public void onEnabled(Context context) {
		// TODO Put your code here
	}

	@Override
	public void onDisabled(Context context) {
		// TODO Put your code here
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Put your code here
	}
}
