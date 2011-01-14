package com.ch_linghu.fanfoudroid.data.db;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.ch_linghu.fanfoudroid.data.Tweet;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    // Database
    private static final String DATABASE_NAME = "status";
    private static final int DATABASE_VERSION = 1;
    
    // Table - status
    public static final class StatusTable implements BaseColumns {
	    
	    public static final String TABLE_NAME = "status";
	    public static final int TABEL_STATUS_LENGTH = 20;
	    public static final String FIELD_STATUS_ID = "status_id";
	    public static final String FIELD_USER_ID = "uid";
	    public static final String FIELD_USER_SCREEN_NAME = "screen_name";
	    public static final String FIELD_PROFILE_IMAGE_URL = "profile_image_url";
	    public static final String FIELD_CREATED_AT = "created_at";
	    public static final String FIELD_TEXT = "text";
	    public static final String FIELD_SOURCE = "source";
	    public static final String FIELD_TRUNCATED = "truncated";
	    public static final String FIELD_IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
	    public static final String FIELD_IN_REPLY_TO_USER_ID = "in_reply_to_user_id";
	    public static final String FIELD_IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";
	    public static final String FIELD_FAVORITED =  "favorited";
	    public static final String FIELD_IS_UNREAD = "is_unread";
	//    private static final String FIELD_PHOTO_URL = "photo_url";
	//    private double latitude = -1;
	//    private double longitude = -1;
	//    private String thumbnail_pic;
	//    private String bmiddle_pic;
	//    private String original_pic;
	    
	    public static final String[] TABLE_STATUS_COLUMNS = new String[] {_ID, FIELD_STATUS_ID, FIELD_USER_SCREEN_NAME,
	        FIELD_TEXT, FIELD_PROFILE_IMAGE_URL, FIELD_IS_UNREAD, FIELD_CREATED_AT,
	        FIELD_FAVORITED, FIELD_IN_REPLY_TO_STATUS_ID, FIELD_IN_REPLY_TO_USER_ID,
	        FIELD_IN_REPLY_TO_SCREEN_NAME, FIELD_TRUNCATED,
	        FIELD_SOURCE, FIELD_USER_ID};
	    
	    private static final String STATUS_TABLE_CREATE = "CREATE TABLE "
	        + TABLE_NAME + " (" 
	        + _ID + " INTEGER PRIMARY KEY,"
	        + FIELD_STATUS_ID + " text, " 
	        + FIELD_USER_ID + " text not null, "
	        + FIELD_USER_SCREEN_NAME + " text not null, "
	        + FIELD_TEXT + " text not null, "
	        + FIELD_PROFILE_IMAGE_URL + " text not null, "
	        + FIELD_IS_UNREAD + " boolean not null, "
	        + FIELD_CREATED_AT + " date not null, "
	        + FIELD_FAVORITED + " text, "
	        + FIELD_IN_REPLY_TO_STATUS_ID + " text, "
	        + FIELD_IN_REPLY_TO_USER_ID + " text, "
	        + FIELD_IN_REPLY_TO_SCREEN_NAME + " text, "
	        + FIELD_SOURCE + " text not null, " 
	        + FIELD_TRUNCATED + " boolean not null); "
	        + " CREATE INDEX status_id_idx ON "
	        + TABLE_NAME + " ( " 
	        + FIELD_STATUS_ID  + " ) "; 
    }

    // Construct
    public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	public DatabaseHelper(Context context,String name){
		this(context, name, DATABASE_VERSION);
	}
	public DatabaseHelper(Context context){
		this(context, DATABASE_NAME, DATABASE_VERSION);
	}
	public DatabaseHelper(Context context, int version){
		this(context, DATABASE_NAME, null, version);
	}
	public DatabaseHelper(Context context, String name, int version){
		this(context, name, null, version);
	}

	@Override
    public void onCreate(SQLiteDatabase db) {
//		Log.i(TAG, STATUS_TABLE_CREATE);
		Log.i(TAG, "Create Database.");
        db.execSQL(StatusTable.STATUS_TABLE_CREATE);
    }

	@Override
	public synchronized void close() {
		Log.i(TAG, "Close Database.");
		super.close();
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		Log.i(TAG, "Open Database.");
		super.onOpen(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "Upgrade Database.");
//		db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATUS);
	}
	
	/**
	 * 使tweet表的行数保持在TABLE_STATUS_LENGTH 
	 */
	private void tidyTweetTable() {
		SQLiteDatabase mDb = getWritableDatabase();
		
		String sql = "DELETE FROM " + StatusTable.TABLE_NAME 
				   + " WHERE " + StatusTable._ID +  " NOT IN (SELECT " 
				   + StatusTable._ID + " FROM " + StatusTable.TABLE_NAME 
				   + " ORDER BY " + StatusTable._ID + " DESC LIMIT " 
				   + StatusTable.TABEL_STATUS_LENGTH +")";
		mDb.execSQL(sql);
	}
	
	public final static DateFormat DB_DATE_FORMATTER = new SimpleDateFormat(
		      "yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
	
	/**
	 * 向tweet表中写入一行数据
	 * @param tweet 需要写入的单条消息
	 * @return
	 */
	private long insertTweet(Tweet tweet, boolean isUnread) {
		SQLiteDatabase mDb = getWritableDatabase();
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(StatusTable.FIELD_STATUS_ID, tweet.id);
	    initialValues.put(StatusTable.FIELD_TEXT, tweet.text);
	    initialValues.put(StatusTable.FIELD_USER_ID, tweet.userId);
	    initialValues.put(StatusTable.FIELD_USER_SCREEN_NAME, tweet.screenName);
	    initialValues.put(StatusTable.FIELD_PROFILE_IMAGE_URL, tweet.profileImageUrl);
	    initialValues.put(StatusTable.FIELD_FAVORITED, tweet.favorited);
	    initialValues.put(StatusTable.FIELD_IN_REPLY_TO_STATUS_ID, tweet.inReplyToStatusId);
	    initialValues.put(StatusTable.FIELD_IN_REPLY_TO_USER_ID, tweet.inReplyToUserId);
	    initialValues.put(StatusTable.FIELD_IN_REPLY_TO_SCREEN_NAME, tweet.inReplyToScreenName);
	    //initialValues.put(FIELD_IS_REPLY, tweet.isReply());
	    initialValues.put(StatusTable.FIELD_CREATED_AT, DB_DATE_FORMATTER.format(tweet.createdAt));
	    initialValues.put(StatusTable.FIELD_SOURCE, tweet.source);
	    initialValues.put(StatusTable.FIELD_IS_UNREAD, isUnread);
	    initialValues.put(StatusTable.FIELD_TRUNCATED, tweet.truncated); //TODO: truncated
		
	    Log.i(TAG, "Insert a status into datebase : " + tweet.toString());
		return mDb.insert(StatusTable.TABLE_NAME, null, initialValues);
	}
	
	/**
	 * 向tweet表中写入N行数据
	 * @param tweets 需要写入的消息列表
	 * @return
	 */
	public void putTweets(List<Tweet> tweets, boolean isUnread) {
		if (0 == tweets.size()) return;
		
		SQLiteDatabase mDb = getWritableDatabase();
		
		try {
			mDb.beginTransaction();

			for (int i = tweets.size() - 1 ; i >= 0; i--) {
				Tweet tweet = tweets.get(i); 
				insertTweet(tweet, isUnread);
			}

			mDb.setTransactionSuccessful();
			tidyTweetTable();
		} finally {
			mDb.endTransaction();
		}
	}

	/**
	 * 取出所有消息
	 * @param tableName
	 * @return
	 */
	public Cursor fetchAllTweets(String tableName) {
		SQLiteDatabase mDb = getReadableDatabase();
		
		return mDb.query(tableName, StatusTable.TABLE_STATUS_COLUMNS, null, null, null, null,
				StatusTable.FIELD_CREATED_AT + " DESC");
	}
	
	/**
	 * 取出本地最新消息ID
	 * @param tableName
	 * @return
	 */
	public String fetchMaxId(String tableName) {
		SQLiteDatabase mDb = getReadableDatabase();
		
	    Cursor mCursor = mDb.rawQuery("SELECT " + StatusTable.FIELD_STATUS_ID + " FROM "
	        + StatusTable.TABLE_NAME + " ORDER BY " + StatusTable.FIELD_CREATED_AT + " DESC LIMIT 1", null);

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
	
}
