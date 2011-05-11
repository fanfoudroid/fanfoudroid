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

package com.ch_linghu.fanfoudroid.data.db;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ch_linghu.fanfoudroid.data.Dm;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.helper.Utils;

/**
 * @deprecated 已废弃
 *
 */
public class TwitterDbAdapter {
  private static final String TAG = "TwitterDbAdapter";

  public static final String TABLE_TWEET = "tweets";
  public static final String TABLE_MENTION = "mentions";
  public static final String TABLE_FAVORITE = "favorites";
  public static final String TABLE_DIRECTMESSAGE = "dms";
  public static final String TABLE_FOLLOWER = "followers";
  
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
  //public static final String KEY_IS_REPLY = "is_reply";
  public static final String KEY_PREV_ID = "prev_id";

  public static final String[] TWEET_COLUMNS = new String[] { KEY_ID, KEY_USER,
      KEY_TEXT, KEY_PROFILE_IMAGE_URL, KEY_IS_UNREAD, KEY_CREATED_AT,
      KEY_FAVORITED, KEY_IN_REPLY_TO_STATUS_ID, KEY_IN_REPLY_TO_USER_ID,
      KEY_IN_REPLY_TO_SCREEN_NAME,
      KEY_SOURCE, KEY_USER_ID, KEY_PREV_ID };

  public static final String[] DM_COLUMNS = new String[] { KEY_ID, KEY_USER,
      KEY_TEXT, KEY_PROFILE_IMAGE_URL, KEY_IS_UNREAD, KEY_IS_SENT,
      KEY_CREATED_AT, KEY_USER_ID };

  public static final String[] FOLLOWER_COLUMNS = new String[] { KEY_ID };

  private DatabaseHelper mDbHelper;
  private SQLiteDatabase mDb;

  private static final String DATABASE_NAME = "data";

  private static final int DATABASE_VERSION = 2;

  // NOTE: the twitter ID is used as the row ID.
  // Furthermore, if a row already exists, an insert will replace
  // the old row upon conflict.
  private static final String TWEET_TABLE_CREATE = "create table "
      + TABLE_TWEET + " (" + KEY_ID
      + " text primary key on conflict replace, " + KEY_USER
      + " text not null, " + KEY_TEXT + " text not null, "
      + KEY_PROFILE_IMAGE_URL + " text not null, " + KEY_IS_UNREAD
      + " boolean not null, " + KEY_CREATED_AT + " date not null, "
      + KEY_FAVORITED + " text, "
      + KEY_IN_REPLY_TO_STATUS_ID + " text, "
      + KEY_IN_REPLY_TO_USER_ID + " text, "
      + KEY_IN_REPLY_TO_SCREEN_NAME + " text, "
      + KEY_SOURCE + " text not null, " + KEY_USER_ID + " text, " + KEY_PREV_ID + " text)";

  private static final String MENTION_TABLE_CREATE = "create table "
      + TABLE_MENTION + " (" + KEY_ID
      + " text primary key on conflict replace, " + KEY_USER
      + " text not null, " + KEY_TEXT + " text not null, "
      + KEY_PROFILE_IMAGE_URL + " text not null, " + KEY_IS_UNREAD
      + " boolean not null, " + KEY_CREATED_AT + " date not null, "
      + KEY_FAVORITED + " text, "
      + KEY_IN_REPLY_TO_STATUS_ID + " text, "
      + KEY_IN_REPLY_TO_USER_ID + " text, "
      + KEY_IN_REPLY_TO_SCREEN_NAME + " text, "
      + KEY_SOURCE + " text not null, " + KEY_USER_ID + " text, " + KEY_PREV_ID + " text)";

  private static final String FAVORITE_TABLE_CREATE = "create table "
      + TABLE_FAVORITE + " (" + KEY_ID
      + " text primary key on conflict replace, " + KEY_USER
      + " text not null, " + KEY_TEXT + " text not null, "
      + KEY_PROFILE_IMAGE_URL + " text not null, " + KEY_IS_UNREAD
      + " boolean not null, " + KEY_CREATED_AT + " date not null, "
      + KEY_FAVORITED + " text, "
      + KEY_IN_REPLY_TO_STATUS_ID + " text, "
      + KEY_IN_REPLY_TO_USER_ID + " text, "
      + KEY_IN_REPLY_TO_SCREEN_NAME + " text, "
      + KEY_SOURCE + " text not null, " + KEY_USER_ID + " text, " + KEY_PREV_ID + " text)";

 
  private static final String DM_TABLE_CREATE = "create table " + TABLE_DIRECTMESSAGE
      + " (" + KEY_ID + " text primary key on conflict replace, " + KEY_USER
      + " text not null, " + KEY_TEXT + " text not null, "
      + KEY_PROFILE_IMAGE_URL + " text not null, " + KEY_IS_UNREAD
      + " boolean not null, " + KEY_IS_SENT + " boolean not null, "
      + KEY_CREATED_AT + " date not null, " + KEY_USER_ID + " text)";

  private static final String FOLLOWER_TABLE_CREATE = "create table "
      + TABLE_FOLLOWER + " (" + KEY_ID
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
      db.execSQL(FAVORITE_TABLE_CREATE);
      db.execSQL(DM_TABLE_CREATE);
      db.execSQL(FOLLOWER_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
          + newVersion + " which destroys all old data");
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_TWEET);
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENTION);
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITE);
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIRECTMESSAGE);
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLLOWER);
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
  
  public void resetDatabase(){
	  mDbHelper.onUpgrade(mDb, 1, 2);
  }

  public final static DateFormat DB_DATE_FORMATTER = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss.SSS");

  // TODO: move all these to the model.
  public long createTweet(String tableName, Tweet tweet, String prevId, boolean isUnread) {
	Log.d(TAG, "Insert tweet to table " + tableName + " : " + tweet.toString());

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
    //initialValues.put(KEY_IS_REPLY, tweet.isReply());
    if (!Utils.isEmpty(prevId)){
    	initialValues.put(KEY_PREV_ID, prevId);
    }
    initialValues
        .put(KEY_CREATED_AT, DB_DATE_FORMATTER.format(tweet.createdAt));
    initialValues.put(KEY_SOURCE, tweet.source);
    initialValues.put(KEY_USER_ID, tweet.userId);

	//如果已经存在就更新,否则插入
	if (isTweetExists(tableName, tweet.id)){
		Log.d(TAG, String.format("[update]tweet.id=%s", tweet.id));
		return mDb.update(tableName, initialValues, KEY_ID+"=?", new String[]{tweet.id});
	}else{
		Log.d(TAG, String.format("[insert]tweet.id=%s", tweet.id));
		return mDb.insert(tableName, null, initialValues);
	}
  }
  
  public long updateTweet(String tableName, Tweet tweet){
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
	    //initialValues.put(KEY_IS_REPLY, tweet.isReply());
	    initialValues
	        .put(KEY_CREATED_AT, DB_DATE_FORMATTER.format(tweet.createdAt));
	    initialValues.put(KEY_SOURCE, tweet.source);
	    initialValues.put(KEY_USER_ID, tweet.userId);
	  
	  return mDb.update(tableName, initialValues, KEY_ID+"=?", new String[]{id});
  }
  
  public boolean destoryStatus(String tableName, String status_id) {		
	  String where = KEY_ID + "='" + status_id + "'";		
	  return mDb.delete(tableName, where , null) > 0;		
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

    return mDb.insert(TABLE_DIRECTMESSAGE, null, initialValues);
  }

  public long createFollower(String userId) {
    ContentValues initialValues = new ContentValues();
    initialValues.put(KEY_ID, userId);
    return mDb.insert(TABLE_FOLLOWER, null, initialValues);
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

  public int addNewTweetsAndCountUnread(String tableName, List<Tweet> tweets) {
    addTweets(tableName, tweets, true);

    return fetchUnreadCount(tableName);
  }

  public Cursor fetchAllTweets(String tableName) {
    return mDb.query(tableName, TWEET_COLUMNS, null, null, null, null, KEY_CREATED_AT
        + " DESC");
  }

  public Cursor fetchAllDms() {
    return mDb.query(TABLE_DIRECTMESSAGE, DM_COLUMNS, null, null, null, null, KEY_CREATED_AT
        + " DESC");
  }

  public Cursor fetchInboxDms() {
	    return mDb.query(TABLE_DIRECTMESSAGE, DM_COLUMNS, KEY_IS_SENT + " = ?", new String[]{"0"}, null, null, KEY_CREATED_AT
	        + " DESC");
	  }

  public Cursor fetchSendboxDms() {
	    return mDb.query(TABLE_DIRECTMESSAGE, DM_COLUMNS, KEY_IS_SENT + " = ?", new String[]{"1"}, null, null, KEY_CREATED_AT
	        + " DESC");
	  }

  public Cursor fetchAllFollowers() {
    return mDb.query(TABLE_FOLLOWER, FOLLOWER_COLUMNS, null, null, null, null,
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
    Cursor cursor = mDb.query(TABLE_FOLLOWER, FOLLOWER_COLUMNS, KEY_ID + "="
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
    deleteAllTweets(TABLE_TWEET);
    deleteAllTweets(TABLE_MENTION);
    deleteAllDms();
    deleteAllFollowers();
  }

  public boolean deleteAllTweets(String tableName) {
	    return mDb.delete(tableName, null, null) > 0;
	  }

  public boolean deleteAllDms() {
    return mDb.delete(TABLE_DIRECTMESSAGE, null, null) > 0;
  }

  public boolean deleteAllFollowers() {
    return mDb.delete(TABLE_FOLLOWER, null, null) > 0;
  }

  public boolean deleteDm(String id) {
    return mDb.delete(TABLE_DIRECTMESSAGE, String.format("%s = '%s'", KEY_ID, id), null) > 0;
    
  }

  public void markAllTweetsRead(String tableName) {
	    ContentValues values = new ContentValues();
	    values.put(KEY_IS_UNREAD, 0);
	    mDb.update(tableName, values, null, null);
	  }

  public void markAllDmsRead() {
    ContentValues values = new ContentValues();
    values.put(KEY_IS_UNREAD, 0);
    mDb.update(TABLE_DIRECTMESSAGE, values, null, null);
  }

  public String fetchMaxId(String tableName) {
    Cursor mCursor = mDb.rawQuery("SELECT " + KEY_ID + " FROM "
        + tableName + " ORDER BY " + KEY_CREATED_AT + " DESC LIMIT 1", null);

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

  public int fetchUnreadCount(String tableName) {
    Cursor mCursor = mDb.rawQuery("SELECT COUNT(" + KEY_ID + ") FROM "
        + tableName + " WHERE " + KEY_IS_UNREAD + " = 1", null);

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
    Cursor mCursor = mDb.rawQuery("SELECT " + KEY_ID + " FROM " + TABLE_DIRECTMESSAGE
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

  public int fetchDmCount() {
    Cursor mCursor = mDb.rawQuery("SELECT COUNT(" + KEY_ID + ") FROM "
        + TABLE_DIRECTMESSAGE, null);

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
        + TABLE_DIRECTMESSAGE + " WHERE " + KEY_IS_UNREAD + " = 1", null);

    int result = 0;

    if (mCursor == null) {
      return result;
    }

    mCursor.moveToFirst();
    result = mCursor.getInt(0);
    mCursor.close();

    return result;
  }

  public void addTweets(String tableName, List<Tweet> tweets, boolean isUnread) {
    try {
      mDb.beginTransaction();

      Tweet prevTweet = null;
      for (Tweet tweet : tweets) {
    	if (prevTweet != null){
    		createTweet(tableName, prevTweet, tweet.id, isUnread);
    	}
        prevTweet = tweet;
      }
      //add the last tweet with previd is empty
      if (prevTweet != null){
    	  createTweet(tableName, prevTweet, "", isUnread);
      }

      //limitRows(tableName, TwitterApi.RETRIEVE_LIMIT);
      mDb.setTransactionSuccessful();
    } finally {
      mDb.endTransaction();
    }
  }

  public Tweet getTweet(String tableName, String id){
	  Cursor cursor = mDb.query(tableName, TWEET_COLUMNS, 
			  KEY_ID + " = ?", new String[]{id}, 
			  null, null, null);
	  if (cursor != null && cursor.moveToFirst()){
		  Tweet tweet = new Tweet();
		  tweet.id = cursor.getString(cursor.getColumnIndex(KEY_ID));
		  tweet.text = cursor.getString(cursor.getColumnIndex(KEY_TEXT));
		  tweet.source = cursor.getString(cursor.getColumnIndex(KEY_SOURCE));
		  tweet.favorited = cursor.getString(cursor.getColumnIndex(KEY_FAVORITED));
		  tweet.createdAt = Utils.parseDateTimeFromSqlite(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT)));
		  tweet.profileImageUrl = cursor.getString(cursor.getColumnIndex(KEY_PROFILE_IMAGE_URL));
		  tweet.screenName = cursor.getString(cursor.getColumnIndex(KEY_USER));
		  tweet.userId = cursor.getString(cursor.getColumnIndex(KEY_USER_ID));
		  tweet.inReplyToScreenName = cursor.getString(cursor.getColumnIndex(KEY_IN_REPLY_TO_SCREEN_NAME));
		  tweet.inReplyToUserId = cursor.getString(cursor.getColumnIndex(KEY_IN_REPLY_TO_USER_ID));
		  tweet.inReplyToStatusId = cursor.getString(cursor.getColumnIndex(KEY_IN_REPLY_TO_STATUS_ID));
		  
		  tweet.prevId = cursor.getString(cursor.getColumnIndex(KEY_PREV_ID));
		  cursor.close();

		  return tweet;
	  }else{
		  return null;
	  }
  }
  
  public void addTweets(List<Tweet> tweets, String id, boolean isUnread) {
	    try {
	      mDb.beginTransaction();
	      
	      String tableName = TABLE_TWEET;

	      Tweet prevTweet = getTweet(tableName, id);
	      for (Tweet tweet : tweets) {
	    	if (prevTweet != null){
	    		createTweet(tableName, prevTweet, tweet.id, isUnread);
	    	}
	        prevTweet = tweet;
	      }
	      //add the last tweet with previd is empty
	      if (prevTweet != null){
	    	  createTweet(tableName, prevTweet, null, isUnread);
	      }

	      //limitRows(TABLE_TWEET, TwitterApi.RETRIEVE_LIMIT);
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

      //limitRows(TABLE_DIRECTMESSAGE, TwitterApi.RETRIEVE_LIMIT);
      mDb.setTransactionSuccessful();
    } finally {
      mDb.endTransaction();
    }
  }
  
  //检查指定ID的消息是否存在于数据库
  public boolean isTweetExists(String tableName, String id){
	  Cursor cursor = mDb.rawQuery("SELECT COUNT(*) " 
			                      + " FROM " + tableName 
			                      + " WHERE " + KEY_ID + " = ?",
			                      new String[]{id});
	  if (cursor != null && cursor.moveToFirst()){
		  int count = cursor.getInt(0);
		  cursor.close();
		  if (count > 0){
			  Log.d(TAG, String.format("[isTweetExists], id=%s, tableName=%s, count = %d, return true", id, tableName, count));
			  return true;
		  }else{
			  Log.d(TAG, String.format("[isTweetExists], id=%s, tableName=%s, count = %d, return false", id, tableName, count));
			  return false;
		  }
	  }else{
		  Log.d(TAG, "cursor=null, return false");
		  return false;
	  }
  }
  
  //获取指定ID的消息的前一条消息的ID
  public String getPrevTweetID(String tableName, String id){
	  Cursor cursor = mDb.rawQuery("SELECT " + KEY_PREV_ID 
              + " FROM " + tableName 
              + " WHERE " + KEY_ID + " = ?",
              new String[]{id});
	  if (cursor != null && cursor.moveToFirst()){
		  String result = cursor.getString(0);
		  cursor.close();
		  return result;
	  }else{
		  return "";
	  }
  }
  
  //获取从指定ID开始的更早的N条消息
  public List<Tweet> fetchMoreTweetsSinceId(String tableName, String id, int limit){
	  String prevId = getPrevTweetID(tableName, id);
	  if(Utils.isEmpty(prevId)){
		  return null;
	  }else{
		  int index = 0;
		  List<Tweet> tweetList = new ArrayList<Tweet>();
		  do{
			  Tweet tweet = getTweet(tableName, prevId);
						  
			  prevId = tweet.prevId;
						  
			  tweetList.add(tweet);
			  index++;
		  }while(index < limit && !Utils.isEmpty(prevId));
		  return tweetList;
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
