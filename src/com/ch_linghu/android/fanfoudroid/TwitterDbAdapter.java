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

package com.ch_linghu.android.fanfoudroid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TwitterDbAdapter {
  private static final String TAG = "TwitterDbAdapter";

  public static final String KEY_ID = "_id";
  public static final String KEY_USER = "user";
  public static final String KEY_TEXT = "text";
  public static final String KEY_FAVORITED = "favorited";
  public static final String KEY_IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
  public static final String KEY_IN_REPLY_TO_USER_ID = "in_reply_to_user_id";
  public static final String KEY_IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";
  public static final String KEY_PROFILE_IMAGE_URL = "profile_image_url";
  public static final String KEY_IS_UNREAD = "is_unread";
  public static final String KEY_CREATED_AT = "created_at";
  public static final String KEY_SOURCE = "source";
  public static final String KEY_IS_SENT = "is_sent";
  public static final String KEY_USER_ID = "user_id";
  public static final String KEY_IS_REPLY = "is_reply";

  public static final String[] TWEET_COLUMNS = new String[] { KEY_ID, KEY_USER,
      KEY_TEXT, KEY_PROFILE_IMAGE_URL, KEY_IS_UNREAD, KEY_CREATED_AT,
      KEY_FAVORITED, KEY_IN_REPLY_TO_STATUS_ID, KEY_IN_REPLY_TO_USER_ID,
      KEY_IN_REPLY_TO_SCREEN_NAME,
      KEY_SOURCE, KEY_USER_ID, KEY_IS_REPLY };

  public static final String[] DM_COLUMNS = new String[] { KEY_ID, KEY_USER,
      KEY_TEXT, KEY_PROFILE_IMAGE_URL, KEY_IS_UNREAD, KEY_IS_SENT,
      KEY_CREATED_AT, KEY_USER_ID };

  public static final String[] FOLLOWER_COLUMNS = new String[] { KEY_ID };

  private DatabaseHelper mDbHelper;
  private SQLiteDatabase mDb;

  private static final String DATABASE_NAME = "data";

  private static final String TWEET_TABLE = "tweets";
  private static final String MENTION_TABLE = "mentions";
  private static final String DM_TABLE = "dms";
  private static final String FOLLOWER_TABLE = "followers";

  private static final int DATABASE_VERSION = 2;

  // NOTE: the twitter ID is used as the row ID.
  // Furthermore, if a row already exists, an insert will replace
  // the old row upon conflict.
  private static final String TWEET_TABLE_CREATE = "create table "
      + TWEET_TABLE + " (" + KEY_ID
      + " text primary key on conflict replace, " + KEY_USER
      + " text not null, " + KEY_TEXT + " text not null, "
      + KEY_PROFILE_IMAGE_URL + " text not null, " + KEY_IS_UNREAD
      + " boolean not null, " + KEY_CREATED_AT + " date not null, "
      + KEY_FAVORITED + " text, "
      + KEY_IN_REPLY_TO_STATUS_ID + " text, "
      + KEY_IN_REPLY_TO_USER_ID + " text, "
      + KEY_IN_REPLY_TO_SCREEN_NAME + " text, "
      + KEY_SOURCE + " text not null, " + KEY_USER_ID + " text, " + KEY_IS_REPLY + " boolean not null)";

  private static final String MENTION_TABLE_CREATE = "create table "
      + MENTION_TABLE + " (" + KEY_ID
      + " text primary key on conflict replace, " + KEY_USER
      + " text not null, " + KEY_TEXT + " text not null, "
      + KEY_PROFILE_IMAGE_URL + " text not null, " + KEY_IS_UNREAD
      + " boolean not null, " + KEY_CREATED_AT + " date not null, "
      + KEY_FAVORITED + " text, "
      + KEY_IN_REPLY_TO_STATUS_ID + " text, "
      + KEY_IN_REPLY_TO_USER_ID + " text, "
      + KEY_IN_REPLY_TO_SCREEN_NAME + " text, "
      + KEY_SOURCE + " text not null, " + KEY_USER_ID + " text, " + KEY_IS_REPLY + " boolean not null)";

  private static final String DM_TABLE_CREATE = "create table " + DM_TABLE
      + " (" + KEY_ID + " text primary key on conflict replace, " + KEY_USER
      + " text not null, " + KEY_TEXT + " text not null, "
      + KEY_PROFILE_IMAGE_URL + " text not null, " + KEY_IS_UNREAD
      + " boolean not null, " + KEY_IS_SENT + " boolean not null, "
      + KEY_CREATED_AT + " date not null, " + KEY_USER_ID + " text)";

  private static final String FOLLOWER_TABLE_CREATE = "create table "
      + FOLLOWER_TABLE + " (" + KEY_ID
      + " text primary key on conflict replace)";

  private final Context mContext;

  private static class DatabaseHelper extends SQLiteOpenHelper {
    DatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(TWEET_TABLE_CREATE);
      db.execSQL(MENTION_TABLE_CREATE);
      db.execSQL(DM_TABLE_CREATE);
      db.execSQL(FOLLOWER_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
          + newVersion + " which destroys all old data");
      db.execSQL("DROP TABLE IF EXISTS " + TWEET_TABLE);
      db.execSQL("DROP TABLE IF EXISTS " + MENTION_TABLE);
      db.execSQL("DROP TABLE IF EXISTS " + DM_TABLE);
      db.execSQL("DROP TABLE IF EXISTS " + FOLLOWER_TABLE);
      onCreate(db);
    }
  }

  public TwitterDbAdapter(Context context) {
    this.mContext = context;
  }

  public TwitterDbAdapter open() throws SQLException {
    mDbHelper = new DatabaseHelper(mContext);
    mDb = mDbHelper.getWritableDatabase();

    return this;
  }

  public void close() {
    mDbHelper.close();
  }

  public final static DateFormat DB_DATE_FORMATTER = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);

  // TODO: move all these to the model.
  public long createTweet(Tweet tweet, boolean isUnread) {
    ContentValues initialValues = new ContentValues();
    initialValues.put(KEY_ID, tweet.id);
    initialValues.put(KEY_USER, tweet.screenName);
    initialValues.put(KEY_TEXT, tweet.text);
    initialValues.put(KEY_PROFILE_IMAGE_URL, tweet.profileImageUrl);
    initialValues.put(KEY_FAVORITED, tweet.favorited);
    initialValues.put(KEY_IN_REPLY_TO_STATUS_ID, tweet.inReplyToStatusId);
    initialValues.put(KEY_IN_REPLY_TO_USER_ID, tweet.inReplyToUserId);
    initialValues.put(KEY_IN_REPLY_TO_SCREEN_NAME, tweet.inReplyToScreenName);
    initialValues.put(KEY_IS_UNREAD, isUnread);
    initialValues.put(KEY_IS_REPLY, tweet.isReply());
    initialValues
        .put(KEY_CREATED_AT, DB_DATE_FORMATTER.format(tweet.createdAt));
    initialValues.put(KEY_SOURCE, tweet.source);
    initialValues.put(KEY_USER_ID, tweet.userId);

    return mDb.insert(TWEET_TABLE, null, initialValues);
  }
  
  public long createMention(Tweet tweet, boolean isUnread) {
	    ContentValues initialValues = new ContentValues();
	    initialValues.put(KEY_ID, tweet.id);
	    initialValues.put(KEY_USER, tweet.screenName);
	    initialValues.put(KEY_TEXT, tweet.text);
	    initialValues.put(KEY_PROFILE_IMAGE_URL, tweet.profileImageUrl);
	    initialValues.put(KEY_FAVORITED, tweet.favorited);
	    initialValues.put(KEY_IN_REPLY_TO_STATUS_ID, tweet.inReplyToStatusId);
	    initialValues.put(KEY_IN_REPLY_TO_USER_ID, tweet.inReplyToUserId);
	    initialValues.put(KEY_IN_REPLY_TO_SCREEN_NAME, tweet.inReplyToScreenName);
	    initialValues.put(KEY_IS_UNREAD, isUnread);
	    initialValues.put(KEY_IS_REPLY, tweet.isReply());
	    initialValues
	        .put(KEY_CREATED_AT, DB_DATE_FORMATTER.format(tweet.createdAt));
	    initialValues.put(KEY_SOURCE, tweet.source);
	    initialValues.put(KEY_USER_ID, tweet.userId);

	    return mDb.insert(MENTION_TABLE, null, initialValues);
	  }
  
  public long updateTweet(Tweet tweet){
	  String id = tweet.id;

	    ContentValues initialValues = new ContentValues();
	    initialValues.put(KEY_ID, tweet.id);
	    initialValues.put(KEY_USER, tweet.screenName);
	    initialValues.put(KEY_TEXT, tweet.text);
	    initialValues.put(KEY_PROFILE_IMAGE_URL, tweet.profileImageUrl);
	    initialValues.put(KEY_FAVORITED, tweet.favorited);
	    initialValues.put(KEY_IN_REPLY_TO_STATUS_ID, tweet.inReplyToStatusId);
	    initialValues.put(KEY_IN_REPLY_TO_USER_ID, tweet.inReplyToUserId);
	    initialValues.put(KEY_IN_REPLY_TO_SCREEN_NAME, tweet.inReplyToScreenName);
	    initialValues.put(KEY_IS_REPLY, tweet.isReply());
	    initialValues
	        .put(KEY_CREATED_AT, DB_DATE_FORMATTER.format(tweet.createdAt));
	    initialValues.put(KEY_SOURCE, tweet.source);
	    initialValues.put(KEY_USER_ID, tweet.userId);
	  
	  int result1 = mDb.update(TWEET_TABLE, initialValues, KEY_ID+"=?", new String[]{id});
	  int result2 = mDb.update(MENTION_TABLE, initialValues, KEY_ID+"=?", new String[]{id});
	  
	  return result1 > result2 ? result1 : result2;
  }

  
  public long createDm(Dm dm, boolean isUnread) {
    ContentValues initialValues = new ContentValues();
    initialValues.put(KEY_ID, dm.id);
    initialValues.put(KEY_USER, dm.screenName);
    initialValues.put(KEY_TEXT, dm.text);
    initialValues.put(KEY_PROFILE_IMAGE_URL, dm.profileImageUrl);
    initialValues.put(KEY_IS_UNREAD, isUnread);
    initialValues.put(KEY_IS_SENT, dm.isSent);
    initialValues.put(KEY_CREATED_AT, DB_DATE_FORMATTER.format(dm.createdAt));
    initialValues.put(KEY_USER_ID, dm.userId);

    return mDb.insert(DM_TABLE, null, initialValues);
  }

  public long createFollower(String userId) {
    ContentValues initialValues = new ContentValues();
    initialValues.put(KEY_ID, userId);
    return mDb.insert(FOLLOWER_TABLE, null, initialValues);
  }

  public void syncFollowers(List<String> followers) {
    try {
      mDb.beginTransaction();

      deleteAllFollowers();

      for (String userId : followers) {
        createFollower(userId);
      }

      mDb.setTransactionSuccessful();
    } finally {
      mDb.endTransaction();
    }
  }

  public int addNewTweetsAndCountUnread(List<Tweet> tweets) {
    addTweets(tweets, true);

    return fetchUnreadCount();
  }


  public int addNewMentionsAndCountUnread(List<Tweet> tweets) {
    addMentions(tweets, true);

    return fetchUnreadMentionCount();
  }

  public Cursor fetchAllTweets() {
    return mDb.query(TWEET_TABLE, TWEET_COLUMNS, null, null, null, null, KEY_CREATED_AT
        + " DESC");
  }

  public Cursor fetchMentions() {
    return mDb.query(MENTION_TABLE, TWEET_COLUMNS, null, null,
        null, null, KEY_CREATED_AT + " DESC");
  }

  public Cursor fetchAllDms() {
    return mDb.query(DM_TABLE, DM_COLUMNS, null, null, null, null, KEY_CREATED_AT
        + " DESC");
  }

  public Cursor fetchAllFollowers() {
    return mDb.query(FOLLOWER_TABLE, FOLLOWER_COLUMNS, null, null, null, null,
        null);
  }

  public Cursor getFollowerUsernames(String filter) {
    String likeFilter = '%' + filter + '%';

    // TODO: clean this up.
    return mDb
        .rawQuery(
            "SELECT user_id AS _id, user FROM (SELECT user_id, user FROM tweets INNER JOIN followers on tweets.user_id = followers._id UNION SELECT user_id, user FROM dms INNER JOIN followers on dms.user_id = followers._id) WHERE user LIKE ? ORDER BY user COLLATE NOCASE",
            new String[] { likeFilter });
  }

  public boolean isFollower(long userId) {
    Cursor cursor = mDb.query(FOLLOWER_TABLE, FOLLOWER_COLUMNS, KEY_ID + "="
        + userId, null, null, null, null);

    boolean result = false;

    if (cursor != null && cursor.moveToFirst()) {
      result = true;
    }

    cursor.close();

    return result;
  }

  public void clearData() {
    // TODO: just wipe the database.
    deleteAllTweets();
    deleteAllDms();
    deleteAllFollowers();
  }
  
  public boolean destoryStatus(String status_id) {
	    String where = KEY_ID + "='" + status_id + "'";
	    return mDb.delete(TWEET_TABLE, where , null) > 0;
  }

  public boolean deleteAllTweets() {
	    return mDb.delete(TWEET_TABLE, null, null) > 0;
	  }

  public boolean deleteAllMentions() {
	    return mDb.delete(MENTION_TABLE, null, null) > 0;
	  }

  public boolean deleteAllDms() {
    return mDb.delete(DM_TABLE, null, null) > 0;
  }

  public boolean deleteAllFollowers() {
    return mDb.delete(FOLLOWER_TABLE, null, null) > 0;
  }

  public boolean deleteDm(String id) {
    return mDb.delete(DM_TABLE, String.format("%s = '%s'", KEY_ID, id), null) > 0;
    
  }

  public void markAllTweetsRead() {
	    ContentValues values = new ContentValues();
	    values.put(KEY_IS_UNREAD, 0);
	    mDb.update(TWEET_TABLE, values, null, null);
	  }

  public void markAllMentionsRead() {
	    ContentValues values = new ContentValues();
	    values.put(KEY_IS_UNREAD, 0);
	    mDb.update(MENTION_TABLE, values, null, null);
	  }

  public void markAllDmsRead() {
    ContentValues values = new ContentValues();
    values.put(KEY_IS_UNREAD, 0);
    mDb.update(DM_TABLE, values, null, null);
  }

  public String fetchMaxId() {
    Cursor mCursor = mDb.rawQuery("SELECT " + KEY_ID + " FROM "
        + TWEET_TABLE + " ORDER BY " + KEY_CREATED_AT + " DESC LIMIT 1", null);

    String result = null;

    if (mCursor == null) {
      return result;
    }
    

    mCursor.moveToFirst();
    if (mCursor.getCount() == 0){
    	result = null;
    }else{
    	result = mCursor.getString(0);
    }
    mCursor.close();

    return result;
  }

  public int fetchUnreadCount() {
    Cursor mCursor = mDb.rawQuery("SELECT COUNT(" + KEY_ID + ") FROM "
        + TWEET_TABLE + " WHERE " + KEY_IS_UNREAD + " = 1", null);

    int result = 0;

    if (mCursor == null) {
      return result;
    }

    mCursor.moveToFirst();
    result = mCursor.getInt(0);
    mCursor.close();

    return result;
  }

  public String fetchMaxMentionId() {
	    Cursor mCursor = mDb.rawQuery("SELECT " + KEY_ID + " FROM "
	        + MENTION_TABLE + " ORDER BY " + KEY_CREATED_AT + " DESC LIMIT 1", null);

	    String result = null;

	    if (mCursor == null) {
	      return result;
	    }

	    mCursor.moveToFirst();
	    if (mCursor.getCount() == 0){
	    	result = null;
	    }else{
	    	result = mCursor.getString(0);
	    }
	    mCursor.close();

	    return result;
	  }

	  public int fetchUnreadMentionCount() {
	    Cursor mCursor = mDb.rawQuery("SELECT COUNT(" + KEY_ID + ") FROM "
	        + MENTION_TABLE + " WHERE " + KEY_IS_UNREAD + " = 1", null);

	    int result = 0;

	    if (mCursor == null) {
	      return result;
	    }

	    mCursor.moveToFirst();
	    result = mCursor.getInt(0);
	    mCursor.close();

	    return result;
	  }  
  public String fetchMaxDmId(boolean isSent) {
    Cursor mCursor = mDb.rawQuery("SELECT " + KEY_ID + " FROM " + DM_TABLE
        + " WHERE " + KEY_IS_SENT + " = ? "
        + " ORDER BY " + KEY_CREATED_AT + " DESC   LIMIT 1", new String[] { isSent ? "1" : "0" });

    String result = null;

    if (mCursor == null) {
      return result;
    }

    mCursor.moveToFirst();
    if (mCursor.getCount() == 0){
    	result = null;
    }else{
    	result = mCursor.getString(0);
    }
    mCursor.close();

    return result;
  }

  public int addNewDmsAndCountUnread(List<Dm> dms) {
    addDms(dms, true);

    return fetchUnreadDmCount();
  }

  int fetchDmCount() {
    Cursor mCursor = mDb.rawQuery("SELECT COUNT(" + KEY_ID + ") FROM "
        + DM_TABLE, null);

    int result = 0;

    if (mCursor == null) {
      return result;
    }

    mCursor.moveToFirst();
    result = mCursor.getInt(0);
    mCursor.close();

    return result;
  }

  private int fetchUnreadDmCount() {
    Cursor mCursor = mDb.rawQuery("SELECT COUNT(" + KEY_ID + ") FROM "
        + DM_TABLE + " WHERE " + KEY_IS_UNREAD + " = 1", null);

    int result = 0;

    if (mCursor == null) {
      return result;
    }

    mCursor.moveToFirst();
    result = mCursor.getInt(0);
    mCursor.close();

    return result;
  }

  public void addTweets(List<Tweet> tweets, boolean isUnread) {
    try {
      mDb.beginTransaction();

      for (Tweet tweet : tweets) {
        createTweet(tweet, isUnread);
      }

      limitRows(TWEET_TABLE, TwitterApi.RETRIEVE_LIMIT);
      mDb.setTransactionSuccessful();
    } finally {
      mDb.endTransaction();
    }
  }

  public void addMentions(List<Tweet> tweets, boolean isUnread) {
	    try {
	      mDb.beginTransaction();

	      for (Tweet tweet : tweets) {
	        createMention(tweet, isUnread);
	      }

	      limitRows(MENTION_TABLE, TwitterApi.RETRIEVE_LIMIT);
	      mDb.setTransactionSuccessful();
	    } finally {
	      mDb.endTransaction();
	    }
	  }
  
  public void addDms(List<Dm> dms, boolean isUnread) {
    try {
      mDb.beginTransaction();

      for (Dm dm : dms) {
        createDm(dm, isUnread);
      }

      limitRows(DM_TABLE, TwitterApi.RETRIEVE_LIMIT);
      mDb.setTransactionSuccessful();
    } finally {
      mDb.endTransaction();
    }
  }

  public int limitRows(String tablename, int limit) {
    Cursor cursor = mDb.rawQuery("SELECT " + KEY_ID + " FROM " + tablename
        + " ORDER BY " + KEY_ID + " DESC LIMIT 1 OFFSET ?",
        new String[] { limit - 1 + "" });

    int deleted = 0;

    if (cursor != null && cursor.moveToFirst()) {
      long limitId = cursor.getLong(0);
      deleted = mDb.delete(tablename, KEY_ID + "<" + limitId, null);
    }

    cursor.close();

    return deleted;
  }

}
