/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ch_linghu.fanfoudroid.service;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.ch_linghu.fanfoudroid.DmActivity;
import com.ch_linghu.fanfoudroid.FanfouWidget;
import com.ch_linghu.fanfoudroid.FanfouWidgetSmall;
import com.ch_linghu.fanfoudroid.MentionActivity;
import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterActivity;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.app.Preferences;
import com.ch_linghu.fanfoudroid.data.Dm;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.db.StatusTable;
import com.ch_linghu.fanfoudroid.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.fanfou.Paging;
import com.ch_linghu.fanfoudroid.fanfou.Weibo;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.util.TextHelper;

public class TwitterService extends Service {
	private static final String TAG = "TwitterService";

	private NotificationManager mNotificationManager;

	private ArrayList<Tweet> mNewTweets;
	private ArrayList<Tweet> mNewMentions;
	private ArrayList<Dm> mNewDms;

	private GenericTask mRetrieveTask;

	public String getUserId() {
		return TwitterApplication.getMyselfId(false);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// fetchMessages();
		// handler.postDelayed(mTask, 10000);
		Log.d(TAG, "Start Once");
		return super.onStartCommand(intent, flags, startId);
	}

	private TaskListener mRetrieveTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				SharedPreferences preferences = TwitterApplication.mPref;

				boolean needCheck = preferences.getBoolean(
						Preferences.CHECK_UPDATES_KEY, false);
				boolean timeline_only = preferences.getBoolean(
						Preferences.TIMELINE_ONLY_KEY, false);
				boolean replies_only = preferences.getBoolean(
						Preferences.REPLIES_ONLY_KEY, true);
				boolean dm_only = preferences.getBoolean(
						Preferences.DM_ONLY_KEY, true);

				if (needCheck) {
					if (timeline_only) {
						processNewTweets();
					}
					if (replies_only) {
						processNewMentions();
					}
					if (dm_only) {
						processNewDms();
					}
				}
			}
			// 原widget
			try {
				Intent intent = new Intent(TwitterService.this,
						FanfouWidget.class);
				intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				PendingIntent pi = PendingIntent.getBroadcast(
						TwitterService.this, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				pi.send();
			} catch (CanceledException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
			// 小widget
//			try {
//				Intent intent = new Intent(TwitterService.this,
//						FanfouWidgetSmall.class);
//				intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//				PendingIntent pi = PendingIntent.getBroadcast(
//						TwitterService.this, 0, intent,
//						PendingIntent.FLAG_UPDATE_CURRENT);
//				pi.send();
//			} catch (CanceledException e) {
//				Log.e(TAG, e.getMessage());
//				e.printStackTrace();
//			}
			stopSelf();
		}

		@Override
		public String getName() {
			return "ServiceRetrieveTask";
		}
	};

	private WakeLock mWakeLock;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private TwitterDatabase getDb() {
		return TwitterApplication.mDb;
	}

	private Weibo getApi() {
		return TwitterApplication.mApi;
	}

	@Override
	public void onCreate() {
		Log.v(TAG, "Server Created");
		super.onCreate();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire();

		boolean needCheck = TwitterApplication.mPref.getBoolean(
				Preferences.CHECK_UPDATES_KEY, false);
		boolean widgetIsEnabled = TwitterService.widgetIsEnabled;
		Log.v(TAG, "Check Updates is " + needCheck + "/wg:" + widgetIsEnabled);
		if (!needCheck && !widgetIsEnabled) {
			Log.d(TAG, "Check update preference is false.");
			stopSelf();
			return;
		}

		if (!getApi().isLoggedIn()) {
			Log.d(TAG, "Not logged in.");
			stopSelf();
			return;
		}

		schedule(TwitterService.this);

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		mNewTweets = new ArrayList<Tweet>();
		mNewMentions = new ArrayList<Tweet>();
		mNewDms = new ArrayList<Dm>();

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mRetrieveTask = new RetrieveTask();
			mRetrieveTask.setListener(mRetrieveTaskListener);
			mRetrieveTask.execute((TaskParams[]) null);
		}
	}

	private void processNewTweets() {
		int count = mNewTweets.size();
		if (count <= 0) {
			return;
		}

		Tweet latestTweet = mNewTweets.get(0);

		String title;
		String text;

		if (count == 1) {
			title = latestTweet.screenName;
			text = TextHelper.getSimpleTweetText(latestTweet.text);
		} else {
			title = getString(R.string.service_new_twitter_updates);
			text = getString(R.string.service_x_new_tweets);
			text = MessageFormat.format(text, count);
		}

		PendingIntent intent = PendingIntent.getActivity(this, 0,
				TwitterActivity.createIntent(this), 0);

		notify(intent, TWEET_NOTIFICATION_ID, R.drawable.notify_tweet,
				TextHelper.getSimpleTweetText(latestTweet.text), title, text);
	}

	private void processNewMentions() {
		int count = mNewMentions.size();
		if (count <= 0) {
			return;
		}

		Tweet latestTweet = mNewMentions.get(0);

		String title;
		String text;

		if (count == 1) {
			title = latestTweet.screenName;
			text = TextHelper.getSimpleTweetText(latestTweet.text);
		} else {
			title = getString(R.string.service_new_mention_updates);
			text = getString(R.string.service_x_new_mentions);
			text = MessageFormat.format(text, count);
		}

		PendingIntent intent = PendingIntent.getActivity(this, 0,
				MentionActivity.createIntent(this), 0);

		notify(intent, MENTION_NOTIFICATION_ID, R.drawable.notify_mention,
				TextHelper.getSimpleTweetText(latestTweet.text), title, text);
	}

	private static int TWEET_NOTIFICATION_ID = 0;
	private static int DM_NOTIFICATION_ID = 1;
	private static int MENTION_NOTIFICATION_ID = 2;

	private void notify(PendingIntent intent, int notificationId,
			int notifyIconId, String tickerText, String title, String text) {
		Notification notification = new Notification(notifyIconId, tickerText,
				System.currentTimeMillis());

		notification.setLatestEventInfo(this, title, text, intent);

		notification.flags = Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_ONLY_ALERT_ONCE
				| Notification.FLAG_SHOW_LIGHTS;

		notification.ledARGB = 0xFF84E4FA;
		notification.ledOnMS = 5000;
		notification.ledOffMS = 5000;

		String ringtoneUri = TwitterApplication.mPref.getString(
				Preferences.RINGTONE_KEY, null);

		if (ringtoneUri == null) {
			notification.defaults |= Notification.DEFAULT_SOUND;
		} else {
			notification.sound = Uri.parse(ringtoneUri);
		}

		if (TwitterApplication.mPref.getBoolean(Preferences.VIBRATE_KEY, false)) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}

		mNotificationManager.notify(notificationId, notification);
	}

	private void processNewDms() {
		int count = mNewDms.size();
		if (count <= 0) {
			return;
		}

		Dm latest = mNewDms.get(0);

		String title;
		String text;

		if (count == 1) {
			title = latest.screenName;
			text = TextHelper.getSimpleTweetText(latest.text);
		} else {
			title = getString(R.string.service_new_direct_message_updates);
			text = getString(R.string.service_x_new_direct_messages);
			text = MessageFormat.format(text, count);
		}

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				DmActivity.createIntent(), 0);

		notify(pendingIntent, DM_NOTIFICATION_ID, R.drawable.notify_dm,
				TextHelper.getSimpleTweetText(latest.text), title, text);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Service Destroy.");

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			mRetrieveTask.cancel(true);
		}

		mWakeLock.release();
		super.onDestroy();
	}

	public static void schedule(Context context) {
		SharedPreferences preferences = TwitterApplication.mPref;

		boolean needCheck = preferences.getBoolean(
				Preferences.CHECK_UPDATES_KEY, false);
		boolean widgetIsEnabled = TwitterService.widgetIsEnabled;
		if (!needCheck && !widgetIsEnabled) {
			Log.d(TAG, "Check update preference is false.");
			return;
		}

		String intervalPref = preferences
				.getString(
						Preferences.CHECK_UPDATE_INTERVAL_KEY,
						context.getString(R.string.pref_check_updates_interval_default));
		int interval = Integer.parseInt(intervalPref);
		// interval = 1; //for debug

		Intent intent = new Intent(context, TwitterService.class);
		PendingIntent pending = PendingIntent.getService(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		Calendar c = new GregorianCalendar();
		c.add(Calendar.MINUTE, interval);

		DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
		Log.d(TAG, "Schedule, next run at " + df.format(c.getTime()));

		AlarmManager alarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pending);
		if (needCheck) {
			alarm.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pending);
		} else {
			// only for widget
			alarm.set(AlarmManager.RTC, c.getTimeInMillis(), pending);
		}
	}

	public static void unschedule(Context context) {
		Intent intent = new Intent(context, TwitterService.class);
		PendingIntent pending = PendingIntent.getService(context, 0, intent, 0);
		AlarmManager alarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Log.d(TAG, "Cancelling alarms.");
		alarm.cancel(pending);
	}

	private static boolean widgetIsEnabled = false;

	public static void setWidgetStatus(boolean isEnabled) {
		widgetIsEnabled = isEnabled;
	}

	public static boolean isWidgetEnabled() {
		return widgetIsEnabled;
	}

	private class RetrieveTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			SharedPreferences preferences = TwitterApplication.mPref;

			boolean timeline_only = preferences.getBoolean(
					Preferences.TIMELINE_ONLY_KEY, false);
			boolean replies_only = preferences.getBoolean(
					Preferences.REPLIES_ONLY_KEY, true);
			boolean dm_only = preferences.getBoolean(Preferences.DM_ONLY_KEY,
					true);

			Log.d(TAG, "Widget Is Enabled? " + TwitterService.widgetIsEnabled);
			if (timeline_only || TwitterService.widgetIsEnabled) {
				String maxId = getDb()
						.fetchMaxTweetId(TwitterApplication.getMyselfId(false),
								StatusTable.TYPE_HOME);
				Log.d(TAG, "Max id is:" + maxId);

				List<com.ch_linghu.fanfoudroid.fanfou.Status> statusList;

				try {
					if (maxId != null) {
						statusList = getApi().getFriendsTimeline(
								new Paging(maxId));
					} else {
						statusList = getApi().getFriendsTimeline();
					}
				} catch (HttpException e) {
					Log.e(TAG, e.getMessage(), e);
					return TaskResult.IO_ERROR;
				}

				for (com.ch_linghu.fanfoudroid.fanfou.Status status : statusList) {
					if (isCancelled()) {
						return TaskResult.CANCELLED;
					}

					Tweet tweet;

					tweet = Tweet.create(status);

					mNewTweets.add(tweet);

					Log.d(TAG, mNewTweets.size() + " new tweets.");

					int count = getDb().addNewTweetsAndCountUnread(mNewTweets,
							TwitterApplication.getMyselfId(false),
							StatusTable.TYPE_HOME);

					if (count <= 0) {
						return TaskResult.FAILED;
					}
				}

				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}

			if (replies_only) {
				String maxMentionId = getDb().fetchMaxTweetId(
						TwitterApplication.getMyselfId(false),
						StatusTable.TYPE_MENTION);
				Log.d(TAG, "Max mention id is:" + maxMentionId);

				List<com.ch_linghu.fanfoudroid.fanfou.Status> statusList;

				try {
					if (maxMentionId != null) {
						statusList = getApi().getMentions(
								new Paging(maxMentionId));
					} else {
						statusList = getApi().getMentions();
					}
				} catch (HttpException e) {
					Log.e(TAG, e.getMessage(), e);
					return TaskResult.IO_ERROR;
				}

				int unReadMentionsCount = 0;
				for (com.ch_linghu.fanfoudroid.fanfou.Status status : statusList) {
					if (isCancelled()) {
						return TaskResult.CANCELLED;
					}

					Tweet tweet = Tweet.create(status);
					mNewMentions.add(tweet);

					unReadMentionsCount = getDb().addNewTweetsAndCountUnread(
							mNewMentions, TwitterApplication.getMyselfId(false),
							StatusTable.TYPE_MENTION);

					if (unReadMentionsCount <= 0) {
						return TaskResult.FAILED;
					}
				}
				Log.v(TAG, "Got mentions " + unReadMentionsCount + "/"
						+ mNewMentions.size());

				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}

			if (dm_only) {
				String maxId = getDb().fetchMaxDmId(false);
				Log.d(TAG, "Max DM id is:" + maxId);

				List<com.ch_linghu.fanfoudroid.fanfou.DirectMessage> dmList;

				try {
					if (maxId != null) {
						dmList = getApi().getDirectMessages(new Paging(maxId));
					} else {
						dmList = getApi().getDirectMessages();
					}
				} catch (HttpException e) {
					Log.e(TAG, e.getMessage(), e);
					return TaskResult.IO_ERROR;
				}

				for (com.ch_linghu.fanfoudroid.fanfou.DirectMessage directMessage : dmList) {
					if (isCancelled()) {
						return TaskResult.CANCELLED;
					}

					Dm dm;

					dm = Dm.create(directMessage, false);

					mNewDms.add(dm);
					Log.d(TAG, mNewDms.size() + " new DMs.");

					int count = 0;

					TwitterDatabase db = getDb();

					if (db.fetchDmCount() > 0) {
						count = db.addNewDmsAndCountUnread(mNewDms);
					} else {
						Log.d(TAG, "No existing DMs. Don't notify.");
						db.addDms(mNewDms, false);
					}

					if (count <= 0) {
						return TaskResult.FAILED;
					}
				}

				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}
			return TaskResult.OK;
		}
	}
}
