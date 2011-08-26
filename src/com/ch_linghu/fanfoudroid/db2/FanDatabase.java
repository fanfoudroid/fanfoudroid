package com.ch_linghu.fanfoudroid.db2;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ch_linghu.fanfoudroid.db2.FanContent.*;

public class FanDatabase {
    private static final String TAG = FanDatabase.class.getSimpleName();

    /**
     * SQLite Database file name
     */
    private static final String DATABASE_NAME = "fanfoudroid.db";

    /**
     * Database Version
     */
    public static final int DATABASE_VERSION = 2;

    /**
     * self instance
     */
    private static FanDatabase sInstance = null;

    /**
     * SQLiteDatabase Open Helper
     */
    private DatabaseHelper mOpenHelper = null;

    /**
     * SQLiteOpenHelper
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        // Construct
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "Create Database.");
            // TODO: create tables
            createAllTables(db);
            createAllIndexes(db);
            createAllViews(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "Upgrade Database.");
            resetAllTables(db, oldVersion, newVersion);
            resetAllIndexes(db, oldVersion, newVersion);
            resetAllViews(db, oldVersion, newVersion);
        }

    }

    /**
     * Construct
     * 
     * @param context
     */
    private FanDatabase(Context context) {
        mOpenHelper = new DatabaseHelper(context);
    }

    /**
     * Get Database
     * 
     * @param context
     * @return
     */
    public static synchronized FanDatabase getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new FanDatabase(context);
        }
        return sInstance;
    }

    /**
     * Get SQLiteDatabase Open Helper
     * 
     * @return
     */
    public SQLiteOpenHelper getSQLiteOpenHelper() {
        return mOpenHelper;
    }

    /**
     * Get Database Connection
     * 
     * @param writeable
     * @return
     */
    public SQLiteDatabase getDb(boolean writeable) {
        if (writeable) {
            return mOpenHelper.getWritableDatabase();
        } else {
            return mOpenHelper.getReadableDatabase();
        }
    }

    /**
     * Close Database
     */
    public void close() {
        if (null != sInstance) {
            mOpenHelper.close();
            sInstance = null;
        }
    }

    // Create All tables
    private static void createAllTables(SQLiteDatabase db) {
        db.execSQL(StatusesTable.getCreateSQL());
        db.execSQL(StatusesPropertyTable.getCreateSQL());
        db.execSQL(UserTable.getCreateSQL());
        db.execSQL(DirectMessageTable.getCreateSQL());
        db.execSQL(FollowRelationshipTable.getCreateSQL());
        db.execSQL(TrendTable.getCreateSQL());
        db.execSQL(SavedSearchTable.getCreateSQL());
    }
    
    private static void dropAllTables(SQLiteDatabase db) {
        db.execSQL(StatusesTable.getDropSQL());
        db.execSQL(StatusesPropertyTable.getDropSQL());
        db.execSQL(UserTable.getDropSQL());
        db.execSQL(DirectMessageTable.getDropSQL());
        db.execSQL(FollowRelationshipTable.getDropSQL());
        db.execSQL(TrendTable.getDropSQL());
        db.execSQL(SavedSearchTable.getDropSQL());
    }
    
    private static void resetAllTables(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            dropAllTables(db);
        } catch (SQLException e) {
            Log.e(TAG, "resetAllTables ERROR!");
        }
        createAllTables(db);
    }
    
 // Create All views
    private static void createAllViews(SQLiteDatabase db) {
        db.execSQL(StatusesView.getCreateSQL());
    }
    
    private static void dropAllViews(SQLiteDatabase db) {
        db.execSQL(StatusesView.getDropSQL());
    }
    
    private static void resetAllViews(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            dropAllViews(db);
        } catch (SQLException e) {
            Log.e(TAG, "resetAllViews ERROR!");
        }
        createAllViews(db);
    }
    
    //indexes    
    private static void createAllIndexes(SQLiteDatabase db) {
        db.execSQL(StatusesTable.getCreateIndexSQL());
        db.execSQL(StatusesPropertyTable.getCreateIndexSQL());
        db.execSQL(UserTable.getCreateIndexSQL());
        db.execSQL(DirectMessageTable.getCreateIndexSQL());
        db.execSQL(FollowRelationshipTable.getCreateIndexSQL());
        db.execSQL(TrendTable.getCreateIndexSQL());
        db.execSQL(SavedSearchTable.getCreateIndexSQL());
    }

    private static void dropAllIndexes(SQLiteDatabase db) {
        db.execSQL(StatusesTable.getDropIndexSQL());
        db.execSQL(StatusesPropertyTable.getDropIndexSQL());
        db.execSQL(UserTable.getDropIndexSQL());
        db.execSQL(DirectMessageTable.getDropIndexSQL());
        db.execSQL(FollowRelationshipTable.getDropIndexSQL());
        db.execSQL(TrendTable.getDropIndexSQL());
        db.execSQL(SavedSearchTable.getDropIndexSQL());
    }
    
    private static void resetAllIndexes(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
        	dropAllIndexes(db);
        } catch (SQLException e) {
            Log.e(TAG, "resetAllIndexes ERROR!");
        }
        createAllIndexes(db);
    }
    

}
