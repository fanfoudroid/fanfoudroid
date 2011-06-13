package com.ch_linghu.fanfoudroid.db2;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ch_linghu.fanfoudroid.db2.FanContent.*;

public class FanDatabase {
    private static final String TAG = "FanDatabase";

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
            createStatusTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "Upgrade Database.");
            // TODO: DROP TABLE
            onCreate(db);
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

    /**
     * Create Table Index
     * 
     * @param tableName
     * @param columnName
     * @return
     */
    public static String createIndex(String tableName, String columnName) {
        return "CREATE INDEX " + tableName.toLowerCase() + '_' + columnName
                + " on " + tableName + " (" + columnName + ");";
    }

    public static void createIndexes(SQLiteDatabase db, String tableName,
            String[] indexColumns) {
        for (String columnName : indexColumns) {
            db.execSQL(createIndex(tableName, columnName));
        }
    }

    // Table - Statuses

    static void createStatusTable(SQLiteDatabase db) {
        db.execSQL(StatusTable.getCreateSQL());
        createIndexes(db, StatusTable.TABLE_NAME, StatusTable.getIndexColumns());
    }

    static void resetStatusTable(SQLiteDatabase db, int oldVersion,
            int newVersion) {
        try {
            db.execSQL(StatusTable.getDropSQL());
        } catch (SQLException e) {
        }
        createStatusTable(db);
    }

    // Table - User

}
