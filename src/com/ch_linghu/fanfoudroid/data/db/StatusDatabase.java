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
import android.util.Log;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.db.StatusTableInfo.StatusTable;

/**
 * A Database which contains all statuses and direct-messages, use
 * getInstane(Context) to get a new instance
 * 
 */
public class StatusDatabase {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "status_db";
    private static final int DATABASE_VERSION = 1;

    private static StatusDatabase instance = null;
    private DatabaseHelper mOpenHelper = null;
    private Context mContext = null;

    /**
     * SQLiteOpenHelper
     * 
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        // Construct
        public DatabaseHelper(Context context, String name,
                CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public DatabaseHelper(Context context, String name) {
            this(context, name, DATABASE_VERSION);
        }

        public DatabaseHelper(Context context) {
            this(context, DATABASE_NAME, DATABASE_VERSION);
        }

        public DatabaseHelper(Context context, int version) {
            this(context, DATABASE_NAME, null, version);
        }

        public DatabaseHelper(Context context, String name, int version) {
            this(context, name, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG, "Create Database.");
            // Log.i(TAG, StatusTable.STATUS_TABLE_CREATE);
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
            // db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATUS);
        }
    }

    private StatusDatabase(Context context) {
        mContext = context;
        mOpenHelper = new DatabaseHelper(context);
    }

    public static synchronized StatusDatabase getInstance(Context context) {
        if (null == instance) {
            return new StatusDatabase(context);
        }
        return instance;
    }
    
    public void close() {
        if (null != instance) {
            mOpenHelper.close();
            instance = null;
        }
    }

    /**
     * 取出一条消息
     * 
     * @param tweetId
     * @return 将Cursor转换过的Tweet对象
     */
    public Tweet queryTweet(String tweetId) {
        SQLiteDatabase Db = mOpenHelper.getWritableDatabase();

        Cursor cursor = Db.query(StatusTable.TABLE_NAME,
                StatusTable.TABLE_STATUS_COLUMNS, StatusTable._ID + "=?",
                new String[] { tweetId }, null, null, null);

        Tweet tweet = null;

        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                tweet = StatusTable.parseCursor(cursor); 
            }
        }

        cursor.close();
        return tweet;
    }
    
    /**
     * 快速检查某条消息是否存在(指定类型)
     * 
     * @param tweetId
     * @param type
     * @return is exists
     */
    public boolean isExists(String tweetId, int type) {
        SQLiteDatabase Db = mOpenHelper.getWritableDatabase();
        boolean result = false;
        
        Cursor cursor = Db.query(StatusTable.TABLE_NAME,
                new String[] {StatusTable._ID}, StatusTable._ID + " =? AND " 
                + StatusTable.FIELD_STATUS_TYPE + " = " + type,
                new String[] { tweetId }, null, null, null);
        
        if (cursor != null && cursor.getCount() > 0) {
            result = true;
        }

        cursor.close();
        return result;
    }

    /**
     * 删除一条消息
     * 
     * @param tweetId
     * @return the number of rows affected if a whereClause is passed in, 0
     *         otherwise. To remove all rows and get a count pass "1" as the
     *         whereClause.
     */
    public int deleteTweet(String tweetId) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        return db.delete(StatusTable.TABLE_NAME, StatusTable._ID + " = "
                + tweetId, null);
    }

    /**
     * 将某一类型的消息数量保持在TABLE_STATUS_LENGTH
     * 
     * @param type
     */
    private void tidyTable(int type) {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

        String sql = "DELETE FROM " + StatusTable.TABLE_NAME + " WHERE "
                + StatusTable.FIELD_STATUS_TYPE + " = " + type
                + " AND " + StatusTable._ID + " NOT IN "
                + " (SELECT " + StatusTable._ID // 子句
                + " FROM " + StatusTable.TABLE_NAME 
                + " WHERE " + StatusTable.FIELD_STATUS_TYPE + " = " + type + " " 
                + " ORDER BY " + StatusTable.FIELD_CREATED_AT + " DESC LIMIT "
                + StatusTable.MAX_STATUS_NUM + ")";
         Log.d(TAG, sql);
        mDb.execSQL(sql);
    }

    public final static DateFormat DB_DATE_FORMATTER = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);

    /**
     * 向Status表中写入一行数据, 此方法为私有方法, 外部插入数据请使用 putTweets()
     * 
     * @param tweet 需要写入的单条消息
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    private long insertTweet(Tweet tweet, int type, boolean isUnread) {
        SQLiteDatabase Db = mOpenHelper.getWritableDatabase();
        
        if (isExists(tweet.id, type)) {
            Log.i(TAG, tweet.id + "is exists.");
            return -1;
        }

        // 插入一条新消息
        ContentValues initialValues = new ContentValues();
        initialValues.put(StatusTable.FIELD_STATUS_TYPE, type);
        initialValues.put(StatusTable._ID, tweet.id);
        initialValues.put(StatusTable.FIELD_TEXT, tweet.text);
        initialValues.put(StatusTable.FIELD_USER_ID, tweet.userId);
        initialValues.put(StatusTable.FIELD_USER_SCREEN_NAME, tweet.screenName);
        initialValues.put(StatusTable.FIELD_PROFILE_IMAGE_URL,
                tweet.profileImageUrl);
        initialValues.put(StatusTable.FIELD_FAVORITED, tweet.favorited);
        initialValues.put(StatusTable.FIELD_IN_REPLY_TO_STATUS_ID,
                tweet.inReplyToStatusId);
        initialValues.put(StatusTable.FIELD_IN_REPLY_TO_USER_ID,
                tweet.inReplyToUserId);
        initialValues.put(StatusTable.FIELD_IN_REPLY_TO_SCREEN_NAME,
                tweet.inReplyToScreenName);
        // initialValues.put(FIELD_IS_REPLY, tweet.isReply());
        initialValues.put(StatusTable.FIELD_CREATED_AT,
                DB_DATE_FORMATTER.format(tweet.createdAt));
        initialValues.put(StatusTable.FIELD_SOURCE, tweet.source);
        initialValues.put(StatusTable.FIELD_IS_UNREAD, isUnread);
        initialValues.put(StatusTable.FIELD_TRUNCATED, tweet.truncated);
        // TODO: truncated

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
     * @param values 需要更新字段的键值对
     * @return the number of rows affected
     */
    public int updateTweet(String tweetId, ContentValues values) {
        Log.i(TAG, "Update Tweet  : " + tweetId + " " + values.toString());

        SQLiteDatabase Db = mOpenHelper.getWritableDatabase();

        return Db.update(StatusTable.TABLE_NAME, values,
                StatusTable._ID + "=?", new String[] { tweetId });
    }

    /**
     * 写入N条消息
     * 
     * @param tweets 需要写入的消息List
     * @return
     */
    public void putTweets(List<Tweet> tweets, int type, boolean isUnread) {
        if (0 == tweets.size())
            return;

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            for (int i = tweets.size() - 1; i >= 0; i--) {
                Tweet tweet = tweets.get(i);
                insertTweet(tweet, type, isUnread);
            }

            tidyTable(type); // 保持总量
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 取出某一类型的所有消息
     * 
     * @param tableName
     * @return a cursor
     */
    public Cursor fetchAllTweets(int type) {
        SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

        return mDb.query(StatusTable.TABLE_NAME,
                StatusTable.TABLE_STATUS_COLUMNS,
                StatusTable.FIELD_STATUS_TYPE + " = " + type,
                null, null, null,
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
    public int dropAllTweets(int type) {
        SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

        return mDb.delete(StatusTable.TABLE_NAME, 
                StatusTable.FIELD_STATUS_TYPE + " = " + type, null);
    }

    /**
     * 取出本地某类型最新消息ID
     * 
     * @param tableName
     * @return The newest Status Id
     */
    public String fetchMaxTweetId(int type) {
        return fetchMaxOrMixTweetId(type, true);
    }
    
    /**
     * 取出本地某类型最新消息ID
     * 
     * @param tableName
     * @return The oldest Status Id
     */
    public String fetchMixTweetId(int type) {
        return fetchMaxOrMixTweetId(type, false);
    }
    
    private String fetchMaxOrMixTweetId(int type, boolean isMax) {
        SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();
        
        String sql = "SELECT " + StatusTable._ID 
            + " FROM " + StatusTable.TABLE_NAME 
            + " WHERE " + StatusTable.FIELD_STATUS_TYPE + "=" + type
            + " ORDER BY " + StatusTable.FIELD_CREATED_AT;
        if (isMax) sql += " DESC ";
        Cursor mCursor = mDb.rawQuery(sql + " LIMIT 1", null);

        String result = null;

        if (mCursor == null) {
            return result;
        }

        mCursor.moveToFirst();
        if (mCursor.getCount() == 0) {
            result = null;
        } else {
            result = mCursor.getString(0);
        }
        mCursor.close();

        return result;
    }

    /**
     * Set isFavorited
     * 
     * @param tweetId
     * @param isFavorited
     * @return Is Succeed
     */
    public boolean setFavorited(String tweetId, boolean isFavorited) {
        ContentValues values = new ContentValues();
        values.put(StatusTable.FIELD_FAVORITED, isFavorited);
        int i = updateTweet(tweetId, values);

        return (i > 0) ? true : false;
    }

}
