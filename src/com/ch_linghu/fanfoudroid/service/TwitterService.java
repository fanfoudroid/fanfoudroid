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

import java.io.IOException;
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
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.data.Dm;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.db.TwitterDbAdapter;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.DmActivity;
import com.ch_linghu.fanfoudroid.MentionActivity;
import com.ch_linghu.fanfoudroid.TwitterActivity;
import com.ch_linghu.fanfoudroid.weibo.Paging;
import com.ch_linghu.fanfoudroid.weibo.Weibo;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;
import com.google.android.photostream.UserTask;

public class TwitterService extends Service {
	private static final String TAG = "TwitterService";

	private SharedPreferences mPreferences;

	private NotificationManager mNotificationManager;

	private ArrayList<Tweet> mNewTweets;
	private ArrayList<Tweet> mNewMentions;
	private ArrayList<Dm> mNewDms;

	private UserTask<SharedPreferences, Void, RetrieveResult> mRetrieveTask;

	private WakeLock mWakeLock;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private TwitterDbAdapter getDb() {
		return TwitterApplication.mDb;
	}

	private Weibo getApi() {
		return TwitterApplication.nApi;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire();

		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		if (!mPreferences.getBoolean(Preferences.CHECK_UPDATES_KEY, false)) {
			Log.i(TAG, "Check update preference is false.");
			stopSelf();
			return;
		}

		if (!getApi().getHttpClient().isLoggedIn()) {
			Log.i(TAG, "Not logged in.");
			stopSelf();
			return;
		}

		schedule(TwitterService.this);

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		mNewTweets = new ArrayList<Tweet>();
		mNewMentions = new ArrayList<Tweet>();
		mNewDms = new ArrayList<Dm>();

		mRetrieveTask = new RetrieveTask().execute(new SharedPreferences[] {mPreferences});
	}

	private void processNewTweets() {
		if (mNewTweets.size() <= 0) {
			return;
		}

		Log.i(TAG, mNewTweets.size() + " new tweets.");

		int count = getDb().addNewTweetsAndCountUnread(TwitterDbAdapter.TABLE_TWEET, mNewTweets);

		for (Tweet tweet : mNewTweets) {
			if (!Utils.isEmpty(tweet.profileImageUrl)) {
				// Fetch image to cache.
				try {
					TwitterApplication.mImageManager.put(tweet.profileImageUrl);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}

		if (count <= 0) {
			return;
		}

		Tweet latestTweet = mNewTweets.get(0);

		String title;
		String text;

		if (count == 1) {
			title = latestTweet.screenName;
			text = latestTweet.text.replaceAll("<.*?>", "");
		} else {
			title = getString(R.string.service_new_twitter_updates);
			text = getString(R.string.service_x_new_tweets);
			text = MessageFormat.format(text, count);
		}

		PendingIntent intent = PendingIntent.getActivity(this, 0,
				TwitterActivity.createIntent(this), 0);

		notify(intent, TWEET_NOTIFICATION_ID, R.drawable.notify_tweet,
				latestTweet.text, title, text);
	}

	private void processNewMentions() {
		if (mNewMentions.size() <= 0) {
			return;
		}

		Log.i(TAG, mNewMentions.size() + " new mentions.");

		int count = getDb().addNewTweetsAndCountUnread(TwitterDbAdapter.TABLE_MENTION, mNewMentions);

		for (Tweet tweet : mNewMentions) {
			if (!Utils.isEmpty(tweet.profileImageUrl)) {
				// Fetch image to cache.
				try {
					TwitterApplication.mImageManager.put(tweet.profileImageUrl);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}

		if (count <= 0) {
			return;
		}

		Tweet latestTweet = mNewMentions.get(0);

		String title;
		String text;

		if (count == 1) {
			title = latestTweet.screenName;
			text = latestTweet.text.replaceAll("<.*?>", "");
		} else {
			title = getString(R.string.service_new_mention_updates);
			text = getString(R.string.service_x_new_mentions);
			text = MessageFormat.format(text, count);
		}

		PendingIntent intent = PendingIntent.getActivity(this, 0,
				MentionActivity.createIntent(this), 0);

		notify(intent, MENTION_NOTIFICATION_ID, R.drawable.notify_tweet,
				latestTweet.text, title, text);
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

		String ringtoneUri = mPreferences.getString(Preferences.RINGTONE_KEY,
				null);

		if (ringtoneUri == null) {
			notification.defaults |= Notification.DEFAULT_SOUND;
		} else {
			notification.sound = Uri.parse(ringtoneUri);
		}

		if (mPreferences.getBoolean(Preferences.VIBRATE_KEY, false)) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}

		mNotificationManager.notify(notificationId, notification);
	}

	private void processNewDms() {
		if (mNewDms.size() <= 0) {
			return;
		}

		Log.i(TAG, mNewDms.size() + " new DMs.");

		int count = 0;

		TwitterDbAdapter db = getDb();

		if (db.fetchDmCount() > 0) {
			count = db.addNewDmsAndCountUnread(mNewDms);
		} else {
			Log.i(TAG, "No existing DMs. Don't notify.");
			db.addDms(mNewDms, false);
		}

		for (Dm dm : mNewDms) {
			if (!Utils.isEmpty(dm.profileImageUrl)) {
				// Fetch image to cache.
				try {
					TwitterApplication.mImageManager.put(dm.profileImageUrl);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}

		if (count <= 0) {
			return;
		}

		Dm latest = mNewDms.get(0);

		String title;
		String text;

		if (count == 1) {
			title = latest.screenName;
			text = latest.text.replaceAll("<.*?>", "");
		} else {
			title = getString(R.string.service_new_direct_message_updates);
			text = getString(R.string.service_x_new_direct_messages);
			text = MessageFormat.format(text, count);
		}

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				DmActivity.createIntent(), 0);

		notify(pendingIntent, DM_NOTIFICATION_ID, R.drawable.notify_dm,
				latest.text, title, text);
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "IM DYING!!!");

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == UserTask.Status.RUNNING) {
			mRetrieveTask.cancel(true);
		}

		mWakeLock.release();

		super.onDestroy();
	}

	public static void schedule(Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);

		if (!preferences.getBoolean(Preferences.CHECK_UPDATES_KEY, false)) {
			Log.i(TAG, "Check update preference is false.");
			return;
		}

		String intervalPref = preferences
				.getString(
						Preferences.CHECK_UPDATE_INTERVAL_KEY,
						context
								.getString(R.string.pref_check_updates_interval_default));
		int interval = Integer.parseInt(intervalPref);

		Intent intent = new Intent(context, TwitterService.class);
		PendingIntent pending = PendingIntent.getService(context, 0, intent, 0);
		Calendar c = new GregorianCalendar();
		c.add(Calendar.MINUTE, interval);

		DateFormat df = new SimpleDateFormat("h:mm a");
		Log.i(TAG, "Scheduling alarm at " + df.format(c.getTime()));

		AlarmManager alarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pending);
		alarm.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pending);
	}

	public static void unschedule(Context context) {
		Intent intent = new Intent(context, TwitterService.class);
		PendingIntent pending = PendingIntent.getService(context, 0, intent, 0);
		AlarmManager alarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Log.i(TAG, "Cancelling alarms.");
		alarm.cancel(pending);
	}

	private enum RetrieveResult {
		OK, IO_ERROR, AUTH_ERROR, CANCELLED
	}

	private class RetrieveTask extends UserTask<SharedPreferences, Void, RetrieveResult> {
		@Override
		public RetrieveResult doInBackground(SharedPreferences... params) {
			SharedPreferences preferences = params[0];

			boolean timeline_only = preferences.getBoolean(Preferences.TIMELINE_ONLY_KEY, false);
			boolean replies_only = preferences.getBoolean(Preferences.REPLIES_ONLY_KEY, true);
			boolean dm_only = preferences.getBoolean(Preferences.DM_ONLY_KEY, true);
			
			if (timeline_only){
				String maxId = getDb().fetchMaxId(TwitterDbAdapter.TABLE_TWEET);
				Log.i(TAG, "Max id is:" + maxId);
	
				List<com.ch_linghu.fanfoudroid.weibo.Status> statusList;
	
				try {
					if (maxId != null){
						statusList = getApi().getFriendsTimeline(new Paging(maxId));
					}else{
						statusList = getApi().getFriendsTimeline();
					}
				} catch (WeiboException e) {
					Log.e(TAG, e.getMessage(), e);
					return RetrieveResult.IO_ERROR;
				}
	
				for (com.ch_linghu.fanfoudroid.weibo.Status status : statusList) {
					if (isCancelled()) {
						return RetrieveResult.CANCELLED;
					}
	
					Tweet tweet;
	
					tweet = Tweet.create(status);
	
					mNewTweets.add(tweet);
				}
	
				if (isCancelled()) {
					return RetrieveResult.CANCELLED;
				}
			}
			
			if (replies_only){
				String maxMentionId = getDb().fetchMaxId(TwitterDbAdapter.TABLE_MENTION);
				Log.i(TAG, "Max mention id is:" + maxMentionId);

				List<com.ch_linghu.fanfoudroid.weibo.Status> statusList;
	
				try {
					if (maxMentionId != null){
						statusList = getApi().getMentions(new Paging(maxMentionId));
					}else{
						statusList = getApi().getMentions();						
					}
				} catch (WeiboException e) {
					Log.e(TAG, e.getMessage(), e);
					return RetrieveResult.IO_ERROR;
				}
	
				for (com.ch_linghu.fanfoudroid.weibo.Status status : statusList) {
					if (isCancelled()) {
						return RetrieveResult.CANCELLED;
					}
	
					Tweet tweet;

					tweet = Tweet.create(status);
	
					mNewMentions.add(tweet);
				}
	
				if (isCancelled()) {
					return RetrieveResult.CANCELLED;
				}
			}
			
			if (dm_only){
				String maxId = getDb().fetchMaxDmId(false);
				Log.i(TAG, "Max DM id is:" + maxId);
	
				List<com.ch_linghu.fanfoudroid.weibo.DirectMessage> dmList;
				
				try {
					if (maxId != null){
						dmList = getApi().getDirectMessages(new Paging(maxId));
					}else{
						dmList = getApi().getDirectMessages();
					}
				} catch (WeiboException e) {
					Log.e(TAG, e.getMessage(), e);
					return RetrieveResult.IO_ERROR;
				}
	
				for (com.ch_linghu.fanfoudroid.weibo.DirectMessage directMessage : dmList) {
					if (isCancelled()) {
						return RetrieveResult.CANCELLED;
					}
	
					Dm dm;

					dm = Dm.create(directMessage, false);
	
					mNewDms.add(dm);
				}
	
				if (isCancelled()) {
					return RetrieveResult.CANCELLED;
				}
			}
			return RetrieveResult.OK;
		}

		@Override
		public void onPostExecute(RetrieveResult result) {
			if (result == RetrieveResult.OK) {
				processNewTweets();
				processNewMentions();
				processNewDms();
			}

			stopSelf();
		}
	}

}
