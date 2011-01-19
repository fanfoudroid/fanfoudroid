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
import com.ch_linghu.fanfoudroid.helper.Utils;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "status_db";
    private static final int DATABASE_VERSION = 1;
    
    /**
     * Table - status
     * @author lds
     *
     */
    public static final class StatusTable implements BaseColumns {
	    public static final int TYPE_HOME = 1;
	    public static final int TYPE_MENTION = 2;
	    public static final int TYPE_USER = 3;
	    
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
	    public static final String FIELD_STATUS_TYPE = "status_type";
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
	        FIELD_SOURCE, FIELD_USER_ID, FIELD_STATUS_TYPE};
	    
	    private static final String STATUS_TABLE_CREATE = "CREATE TABLE "
	        + TABLE_NAME + " (" 
	        + _ID + " INTEGER PRIMARY KEY,"
	        + FIELD_STATUS_ID + " text, " 
	        + FIELD_STATUS_TYPE + " text not null, "
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
	    
	    /**
	     * 将游标解析为一条Tweet
	     * 
	     * @param cursor 返回前会关闭游标
	     * @return
	     */
	    public static Tweet parseTweetFromCursor(Cursor cursor) {
	        Tweet tweet = new Tweet();
	        tweet.id = cursor.getString(cursor.getColumnIndex(FIELD_STATUS_ID));
	        tweet.createdAt = Utils.parseDateTimeFromSqlite(cursor.getString(cursor.getColumnIndex(FIELD_CREATED_AT)));
	        tweet.favorited = cursor.getString(cursor.getColumnIndex(FIELD_FAVORITED));
	        tweet.screenName = cursor.getString(cursor.getColumnIndex(FIELD_USER_SCREEN_NAME));
	        tweet.userId = cursor.getString(cursor.getColumnIndex(FIELD_USER_ID));
	        tweet.text = cursor.getString(cursor.getColumnIndex(FIELD_TEXT));
	        tweet.source = cursor.getString(cursor.getColumnIndex(FIELD_SOURCE));
	        tweet.profileImageUrl = cursor.getString(cursor.getColumnIndex(FIELD_PROFILE_IMAGE_URL));
	        tweet.inReplyToScreenName = cursor.getString(cursor.getColumnIndex(FIELD_IN_REPLY_TO_SCREEN_NAME));
	        tweet.inReplyToStatusId = cursor.getString(cursor.getColumnIndex(FIELD_IN_REPLY_TO_STATUS_ID));
	        tweet.inReplyToUserId = cursor.getString(cursor.getColumnIndex(FIELD_IN_REPLY_TO_USER_ID));
	        tweet.truncated = cursor.getString(cursor.getColumnIndex(FIELD_TRUNCATED));
	        tweet.setStatusType(cursor.getString(cursor.getColumnIndex(FIELD_STATUS_TYPE)) );
	        Log.i("LDS", "c " + cursor.getString(cursor.getColumnIndex(FIELD_STATUS_TYPE)));
	        
	        cursor.close(); 
	        return tweet;
	    }
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
	 * 取出一条消息
	 * 
	 * @param tweetId
	 * @return
	 */
	public Tweet query(String tweetId) {
		SQLiteDatabase Db = getWritableDatabase();
	    
		Cursor cursor = Db.query(StatusTable.TABLE_NAME, StatusTable.TABLE_STATUS_COLUMNS, 
		        StatusTable.FIELD_STATUS_ID + "=?", new String[]{tweetId},
		        null, null, null);
		
		Tweet tweet = null;

        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                tweet = StatusTable.parseTweetFromCursor(cursor); // and close cursor
            }
        }

        return tweet;
	}
	
	/**
	 * 将某一类型的消息数量保持在TABLE_STATUS_LENGTH 
	 * 
	 * @param type
	 */
	private void tidyTable(int type) {
		SQLiteDatabase mDb = getWritableDatabase();
		
		String sql = "DELETE FROM " + StatusTable.TABLE_NAME 
				   + " WHERE " + StatusTable.FIELD_STATUS_TYPE 
				   + " LIKE '%" + type + "%' AND " 
				   + StatusTable._ID +  " NOT IN " 
				       +" (SELECT " + StatusTable._ID
				       + " FROM " + StatusTable.TABLE_NAME
				       + " WHERE " + StatusTable.FIELD_STATUS_TYPE 
				       + " LIKE '%" + type + "%' " 
				       + " ORDER BY " + StatusTable._ID + " DESC LIMIT " 
				       + StatusTable.TABEL_STATUS_LENGTH +")";
		Log.d(TAG, sql);
		mDb.execSQL(sql);
	}
	
	public final static DateFormat DB_DATE_FORMATTER = new SimpleDateFormat(
		      "yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
	
	/**
	 * 向tweet表中写入一行数据
	 * 
	 * @param tweet 需要写入的单条消息
	 * @return the row ID of the newly inserted row, or -1 if an error occurred 
	 */
	private long insertTweet(Tweet tweet, boolean isUnread, int type) {
		SQLiteDatabase Db = getWritableDatabase();
		
		// 数据库中有一条同样内容的消息, 但类型可能不同
		Tweet result = query(tweet.id);
		if (null != result) {
		    
		    String DBstatusType = result.getStatusType();
		    Log.i("LDS", "DBTYPE " + DBstatusType);
//		    String statusType = tweet.getStatusType();
		    if(DBstatusType.contains(type+"")) {
		        // 类型也相同, 直接退出
		        Log.i(TAG, "Tweet is Exists : " + tweet.toString());
		        return -1;
		    } else {
		        // 类型不同, 仅需要更新type字段即可
		        ContentValues values = new ContentValues();
		        values.put(StatusTable.FIELD_STATUS_TYPE, 
		                DBstatusType + type + ",");
		        updateTweet(tweet.id, values);
		        return 0;
		    }
		}
		
		// 插入一条新消息
		ContentValues initialValues = new ContentValues();
		initialValues.put(StatusTable.FIELD_STATUS_TYPE, type +",");
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
		
		long id = Db.insert(StatusTable.TABLE_NAME, null, initialValues);
		
		if (-1 == id) {
		    Log.e(TAG, "cann't insert the tweet : " + tweet.toString());
		} else {
		    Log.i(TAG, "Insert a status into datebase : " + tweet.toString());
		}
		
		return id;
	}
	
	/**
	 * 更新一条消息
	 * 
	 * @param tweetId
	 * @param key
	 * @param value
	 * @return the number of rows affected 
	 */
	public int updateTweet(String tweetId, ContentValues values) {
        Log.i(TAG, "Update Tweet  : " + tweetId + " " + values.toString());
        
		SQLiteDatabase Db = getWritableDatabase();
		
        return Db.update(StatusTable.TABLE_NAME, values, 
                StatusTable.FIELD_STATUS_ID + "=?", new String[]{tweetId} );
	}
	
	
	/**
	 * 写入N条消息
	 * 
	 * @param tweets 需要写入的消息List
	 * @return
	 */
	public void putTweets(List<Tweet> tweets, boolean isUnread, int type) {
		if (0 == tweets.size()) return;
		
		SQLiteDatabase db = getWritableDatabase();
		
		try {
			db.beginTransaction();

			for (int i = tweets.size() - 1 ; i >= 0; i--) {
				Tweet tweet = tweets.get(i); 
				insertTweet(tweet, isUnread, type);
			}

			tidyTable(type); // 保持数量
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * 取出某一类型的所有消息
	 * 
	 * @param tableName
	 * @return
	 */
	public Cursor fetchAllTweets(String tableName, int type) {
		SQLiteDatabase mDb = getReadableDatabase();
		
		return mDb.query(tableName,
		        StatusTable.TABLE_STATUS_COLUMNS, 
		        StatusTable.FIELD_STATUS_TYPE +" LIKE ?", 
		        new String[]{"%" + type + "%"}, null, null,
				StatusTable.FIELD_CREATED_AT + " DESC");
	}
	
    /**
     * 清空某类型的所有信息
     * 
     * @param tableName
     * @return the number of rows affected if a whereClause is passed in, 0
     *         otherwise. To remove all rows and get a count pass "1" as the
     *         whereClause.
     */
    public int dropAllTweets(String tableName, int type) {
        SQLiteDatabase mDb = getReadableDatabase();
        
        return mDb.delete(tableName,
                StatusTable.FIELD_STATUS_TYPE +" LIKE ?", 
                new String[]{"%" + type + "%"});
    }
	
	/**
	 * 取出本地最新消息ID
	 * 
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
