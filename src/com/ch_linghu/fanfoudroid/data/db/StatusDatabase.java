package com.ch_linghu.fanfoudroid.data.db;

import java.io.File;
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

import com.ch_linghu.fanfoudroid.data.Dm;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.db.StatusTablesInfo.FollowTable;
import com.ch_linghu.fanfoudroid.data.db.StatusTablesInfo.MessageTable;
import com.ch_linghu.fanfoudroid.data.db.StatusTablesInfo.StatusTable;

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
            db.execSQL(StatusTable.CREATE_TABLE);
            db.execSQL(MessageTable.CREATE_TABLE);
            db.execSQL(FollowTable.CREATE_TABLE);
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
            dropAllTables(db);
        }

        private void dropAllTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + StatusTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MessageTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + FollowTable.TABLE_NAME);
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

    // 测试用
    public SQLiteOpenHelper getSQLiteOpenHelper() {
        return mOpenHelper;
    }

    public void close() {
        if (null != instance) {
            mOpenHelper.close();
            instance = null;
        }
    }

    /**
     * 清空所有表中数据, 谨慎使用
     * 
     */
    public void clearData() {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        db.execSQL("DELETE FROM " + StatusTable.TABLE_NAME);
        db.execSQL("DELETE FROM " + MessageTable.TABLE_NAME);
        db.execSQL("DELETE FROM " + FollowTable.TABLE_NAME);
    }

    /**
     * 直接删除数据库文件, 调试用
     * 
     * @return true if this file was deleted, false otherwise.
     * @deprecated
     */
    private boolean deleteDatabase() {
        File dbFile = mContext.getDatabasePath(DATABASE_NAME);
        return dbFile.delete();
    }

    /**
     * 取出某类型的一条消息
     * 
     * @param tweetId
     * @param type of status
     *              <li>StatusTable.TYPE_HOME</li>
     *              <li>StatusTable.TYPE_MENTION</li>
     *              <li>StatusTable.TYPE_USER</li>
     *              <li>StatusTable.TYPE_FAVORITE</li>
     *              <li>-1 means all types</li>
     * @return 将Cursor转换过的Tweet对象
     */
    public Tweet queryTweet(String tweetId, int type) {
        SQLiteDatabase Db = mOpenHelper.getWritableDatabase();

        String selection = StatusTable._ID + "=? ";
        if (-1 != type) {
            selection += " AND " + StatusTable.FIELD_STATUS_TYPE + "=" + type;
        }

        Cursor cursor = Db.query(StatusTable.TABLE_NAME,
                StatusTable.TABLE_COLUMNS, selection, new String[] { tweetId },
                null, null, null);

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
     *            <li>StatusTable.TYPE_HOME</li>
     *            <li>StatusTable.TYPE_MENTION</li>
     *            <li>StatusTable.TYPE_USER</li>
     *            <li>StatusTable.TYPE_FAVORITE</li>
     * @return is exists
     */
    public boolean isExists(String tweetId, int type) {
        SQLiteDatabase Db = mOpenHelper.getWritableDatabase();
        boolean result = false;

        Cursor cursor = Db.query(StatusTable.TABLE_NAME,
                new String[] { StatusTable._ID }, StatusTable._ID + " =? AND "
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

        return db.delete(StatusTable.TABLE_NAME, StatusTable._ID + "=?",
                new String[] { tweetId });
    }

    /**
     * 删除超过MAX_ROW_NUM垃圾数据
     * 
     * @param type
     *            <li>StatusTable.TYPE_HOME</li>
     *            <li>StatusTable.TYPE_MENTION</li>
     *            <li>StatusTable.TYPE_USER</li>
     *            <li>StatusTable.TYPE_FAVORITE</li>
     *            <li>-1 means all types</li>
     */
    public void gc(int type) {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

        String sql = "DELETE FROM " + StatusTable.TABLE_NAME
                + " WHERE " + StatusTable.FIELD_STATUS_TYPE + " = " + type 
                + " AND " + StatusTable._ID + " NOT IN " 
                + " (SELECT " + StatusTable._ID // 子句
                + " FROM " + StatusTable.TABLE_NAME;
        if (type != -1) {
            sql += " WHERE " + StatusTable.FIELD_STATUS_TYPE + " = " + type + " ";
        }
        sql += " ORDER BY " + StatusTable.FIELD_CREATED_AT + " DESC LIMIT ";
        sql += StatusTable.MAX_ROW_NUM + ")";

        Log.d(TAG, sql);
        mDb.execSQL(sql);
    }

    public final static DateFormat DB_DATE_FORMATTER = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);

    /**
     * 向Status表中写入一行数据, 此方法为私有方法, 外部插入数据请使用 putTweets()
     * 
     * @param tweet
     *            需要写入的单条消息
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
     * @param values
     *            ContentValues 需要更新字段的键值对
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
     * @param tweets
     *            需要写入的消息List
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

            // gc(type); // 保持总量
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 取出某一类型的所有消息 只返回最新的StatusTable.MAX_ROW_NUM条,超过部分将视为垃圾数据无法取出
     * 
     * @param tableName
     * @return a cursor
     */
    public Cursor fetchAllTweets(int type) {
        SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

        return mDb.query(StatusTable.TABLE_NAME, StatusTable.TABLE_COLUMNS,
                StatusTable.FIELD_STATUS_TYPE + " = " + type, null, null, null,
                StatusTable.FIELD_CREATED_AT + " DESC LIMIT "
                        + StatusTable.MAX_ROW_NUM);
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

        return mDb.delete(StatusTable.TABLE_NAME, StatusTable.FIELD_STATUS_TYPE
                + " = " + type, null);
    }

    /**
     * 取出本地某类型最新消息ID
     * 
     * @param type
     * @return The newest Status Id
     */
    public String fetchMaxTweetId(int type) {
        return fetchMaxOrMixTweetId(type, true);
    }

    /**
     * 取出本地某类型最新消息ID
     * 
     * @param tableName
     * @deprecated 废弃
     * @return The oldest Status Id
     */
    public String fetchMixTweetId(int type) {
        return fetchMaxOrMixTweetId(type, false);
    }

    private String fetchMaxOrMixTweetId(int type, boolean isMax) {
        SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

        String sql = "SELECT " + StatusTable._ID + " FROM "
                + StatusTable.TABLE_NAME + " WHERE "
                + StatusTable.FIELD_STATUS_TYPE + "=" + type + " ORDER BY "
                + StatusTable.FIELD_CREATED_AT;
        if (isMax)
            sql += " DESC ";

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
     * Count unread tweet
     * 
     * @param tableName
     * @return
     */
    public int fetchUnreadCount(int type) {
        SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

        Cursor mCursor = mDb.rawQuery("SELECT COUNT(" + StatusTable._ID + ")"
                + " FROM " + StatusTable.TABLE_NAME + " WHERE "
                + StatusTable.FIELD_STATUS_TYPE + " = " + type + " AND "
                + StatusTable.FIELD_IS_UNREAD + " = 1 LIMIT "
                + StatusTable.MAX_ROW_NUM, null);

        int result = 0;

        if (mCursor == null) {
            return result;
        }

        mCursor.moveToFirst();
        result = mCursor.getInt(0);
        mCursor.close();

        return result;
    }

    public int addNewTweetsAndCountUnread(List<Tweet> tweets, int type) {
        putTweets(tweets, type, true);

        return fetchUnreadCount(type);
    }

    /**
     * Set isFavorited
     * 
     * @param tweetId
     * @param isFavorited
     * @return Is Succeed
     */
    public boolean setFavorited(String tweetId, String isFavorited) {
        ContentValues values = new ContentValues();
        values.put(StatusTable.FIELD_FAVORITED, isFavorited);
        int i = updateTweet(tweetId, values);

        return (i > 0) ? true : false;
    }

    // DM & Follower

    /**
     * 写入一条私信
     * 
     * @param dm
     * @param isUnread
     * @return the row ID of the newly inserted row, or -1 if an error occurred,
     *         因为主键的原因,此处返回的不是 _ID 的值, 而是一个自增长的 row_id
     */
    public long createDm(Dm dm, boolean isUnread) {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

        ContentValues initialValues = new ContentValues();
        initialValues.put(MessageTable._ID, dm.id);
        initialValues.put(MessageTable.FIELD_USER_SCREEN_NAME, dm.screenName);
        initialValues.put(MessageTable.FIELD_TEXT, dm.text);
        initialValues.put(MessageTable.FIELD_PROFILE_IMAGE_URL,
                dm.profileImageUrl);
        initialValues.put(MessageTable.FIELD_IS_UNREAD, isUnread);
        initialValues.put(MessageTable.FIELD_IS_SENT, dm.isSent);
        initialValues.put(MessageTable.FIELD_CREATED_AT,
                DB_DATE_FORMATTER.format(dm.createdAt));
        initialValues.put(MessageTable.FIELD_USER_ID, dm.userId);

        return mDb.insert(MessageTable.TABLE_NAME, null, initialValues);
    }

    //

    /**
     * Create a follower
     * 
     * @param userId
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long createFollower(String userId) {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

        ContentValues initialValues = new ContentValues();
        initialValues.put(FollowTable._ID, userId);
        long rowId = mDb.insert(FollowTable.TABLE_NAME, null, initialValues);
        if (-1 == rowId) {
            Log.e(TAG, "Cann't create Follower : " + userId);
        } else {
            Log.i(TAG, "create create follower : " + userId);
        }
        return rowId;
    }

    /**
     * 清空Followers表并添加新内容
     * 
     * @param followers
     */
    public void syncFollowers(List<String> followers) {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

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

    /**
     * @param type
     *            <li>MessageTable.TYPE_SENT</li>
     *            <li>MessageTable.TYPE_GET</li>
     *            <li>其他任何值都认为取出所有类型</li>
     * @return
     */
    public Cursor fetchAllDms(int type) {
        SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

        String selection = null;

        if (MessageTable.TYPE_SENT == type) {
            selection = MessageTable.FIELD_IS_SENT + " = "
                    + MessageTable.TYPE_SENT;
        } else if (MessageTable.TYPE_GET == type) {
            selection = MessageTable.FIELD_IS_SENT + " = "
                    + MessageTable.TYPE_GET;
        }

        return mDb.query(MessageTable.TABLE_NAME, MessageTable.TABLE_COLUMNS,
                selection, null, null, null, MessageTable.FIELD_CREATED_AT
                        + " DESC");
    }

    public Cursor fetchInboxDms() {
        return fetchAllDms(MessageTable.TYPE_GET);
    }

    public Cursor fetchSendboxDms() {
        return fetchAllDms(MessageTable.TYPE_SENT);
    }

    public Cursor fetchAllFollowers() {
        SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

        return mDb.query(FollowTable.TABLE_NAME, FollowTable.TABLE_COLUMNS,
                null, null, null, null, null);
    }

    public Cursor getFollowerUsernames(String filter) {
        SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

        String likeFilter = '%' + filter + '%';

        // FIXME: clean this up. 新数据库中失效, 表名, 列名
        return mDb.rawQuery(
                        "SELECT user_id AS _id, user"
                         + " FROM (SELECT user_id, user FROM tweets"
                         + " INNER JOIN followers on tweets.user_id = followers._id UNION"
                         + " SELECT user_id, user FROM dms INNER JOIN followers"
                         + " on dms.user_id = followers._id)"
                         + " WHERE user LIKE ?"
                         + " ORDER BY user COLLATE NOCASE",
                        new String[] { likeFilter });
    }

    /**
     * @param userId
     *            该用户是否follow Me
     * @deprecated 未使用
     * @return
     */
    public boolean isFollower(String userId) {
        SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

        Cursor cursor = mDb.query(FollowTable.TABLE_NAME,
                FollowTable.TABLE_COLUMNS, FollowTable._ID + "= ?",
                new String[] { userId }, null, null, null);

        boolean result = false;

        if (cursor != null && cursor.moveToFirst()) {
            result = true;
        }

        cursor.close();

        return result;
    }

    public boolean deleteAllFollowers() {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

        return mDb.delete(FollowTable.TABLE_NAME, null, null) > 0;
    }

    public boolean deleteDm(String id) {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

        return mDb.delete(MessageTable.TABLE_NAME,
                String.format("%s = '%s'", MessageTable._ID, id), null) > 0;

    }

    /**
     * @param tableName
     * @return the number of rows affected
     */
    public int markAllTweetsRead(int type) {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(StatusTable.FIELD_IS_UNREAD, 0);

        return mDb.update(StatusTable.TABLE_NAME, values,
                StatusTable.FIELD_STATUS_TYPE + "=" + type, null);
    }

    public boolean deleteAllDms() {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

        return mDb.delete(MessageTable.TABLE_NAME, null, null) > 0;
    }

    public int markAllDmsRead() {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MessageTable.FIELD_IS_UNREAD, 0);

        return mDb.update(MessageTable.TABLE_NAME, values, null, null);
    }

    public String fetchMaxDmId(boolean isSent) {
        SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

        Cursor mCursor = mDb.rawQuery("SELECT " + MessageTable._ID + " FROM "
                + MessageTable.TABLE_NAME + " WHERE "
                + MessageTable.FIELD_IS_SENT + " = ? " + " ORDER BY "
                + MessageTable.FIELD_CREATED_AT + " DESC   LIMIT 1",
                new String[] { isSent ? "1" : "0" });

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

    public int addNewDmsAndCountUnread(List<Dm> dms) {
        addDms(dms, true);

        return fetchUnreadDmCount();
    }

    public int fetchDmCount() {
        SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

        Cursor mCursor = mDb.rawQuery("SELECT COUNT(" + MessageTable._ID
                + ") FROM " + MessageTable.TABLE_NAME, null);

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
        SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

        Cursor mCursor = mDb.rawQuery("SELECT COUNT(" + MessageTable._ID
                + ") FROM " + MessageTable.TABLE_NAME + " WHERE "
                + MessageTable.FIELD_IS_UNREAD + " = 1", null);

        int result = 0;

        if (mCursor == null) {
            return result;
        }

        mCursor.moveToFirst();
        result = mCursor.getInt(0);
        mCursor.close();

        return result;
    }

    public void addDms(List<Dm> dms, boolean isUnread) {
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

        try {
            mDb.beginTransaction();

            for (Dm dm : dms) {
                createDm(dm, isUnread);
            }

            // limitRows(TABLE_DIRECTMESSAGE, TwitterApi.RETRIEVE_LIMIT);
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }
    }

}
