package com.ch_linghu.fanfoudroid.db2;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ch_linghu.fanfoudroid.data2.Status;
import com.ch_linghu.fanfoudroid.db2.FanContent.StatusColumns;

public class FanDatabase {
    private static final String TAG = "FanDatabase";
    
    private static final String DATABASE_NAME = "fanfoudroid.db";
    public static final int DATABASE_VERSION = 2;
    
    private static final String WHERE_ID = FanContent.RECORD_ID + "=?";
    
    private static FanDatabase sInstance = null;
    private DatabaseHelper mOpenHelper = null;
    
    /**
     * SQLiteOpenHelper
     * 
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
    
    private FanDatabase(Context context) {
        mOpenHelper = new DatabaseHelper(context);
    }
    
    public static synchronized FanDatabase getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new FanDatabase(context);
        }
        return sInstance;
    }
    
    public SQLiteOpenHelper getSQLiteOpenHelper() {
        return mOpenHelper;
    }
    
    public SQLiteDatabase getDb(boolean writeable) {
        if (writeable) {
            return mOpenHelper.getWritableDatabase();
        } else {
            return mOpenHelper.getReadableDatabase();
        }
    }

    public void close() {
        if (null != sInstance) {
            mOpenHelper.close();
            sInstance = null;
        }
    }
    
    public static String createIndex(String tableName, String columnName) {
        return "CREATE INDEX " + tableName.toLowerCase() + '_' + columnName
            + " on " + tableName + " (" + columnName + ");";
    }
    
    public static void createIndex(SQLiteDatabase db, String tableName, String[] indexColumns) {
        for (String columnName : indexColumns) {
            db.execSQL(createIndex(tableName, columnName));
        }
    }
    
    // Table Statuses
    
    static void createStatusTable(SQLiteDatabase db) {
        String createString = Status.TABLE_NAME + "( "
                + StatusColumns.ID + " INTEGER PRIMARY KEY, "
                + StatusColumns.STATUS_ID + " INT UNIQUE NOT NULL, "
                + StatusColumns.AUTHOR_ID + " INT, "
                + StatusColumns.TEXT + " TEXT, "
                + StatusColumns.SOURCE + " TEXT, "
                + StatusColumns.TRUNCATED + " INT, "
                + StatusColumns.IN_REPLY_TO_STATUS_ID + " INT, "
                + StatusColumns.IN_REPLY_TO_USER_ID + " INT, "
                + StatusColumns.FAVORITED + " INT, "
                + StatusColumns.CREATED_AT + " INT "
                + StatusColumns.PIC_THUMB + " TEXT, "
                + StatusColumns.PIC_MID + " TEXT, "
                + StatusColumns.PIC_ORIG + " TEXT "
                + ");";
        
        String[] indexColumns = new String[] {
        };
        
        db.execSQL("CREATE TABLE " + createString);
        createIndex(db, Status.TABLE_NAME, indexColumns);
    }
    
    static void resetStatusTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE " + Status.TABLE_NAME);
        } catch (SQLException e) {
        }
        createStatusTable(db);
    }
    
    // Table User 

}
