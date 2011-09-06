package com.ch_linghu.fanfoudroid.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.ch_linghu.fanfoudroid.provider.FanContent.StatusColumns;

/**
 * ContentProvider 
 */
public class FanProvider extends ContentProvider {
    private static final String TAG = "FanProvider";

    protected static final String DATABASE_NAME = "fanfoudroid.db";

    private static final String WHERE_ID = FanContent.RECORD_ID + "=?";

    // Any changes to the database format *must* include update-in-place code.
    // Original version: 1
    // version 2: 抛弃原版旧结构, 重构数据库, 按正常的模式进行设计。
    public static final int DATABASE_VERSION = 2;
    public static final String AUTHORITY = "com.ch_linghu.fanfoudroid.provider";

    private static final int STATUS_BASE = 0;
    private static final int STATUS = STATUS_BASE;
    private static final int STATUS_ID = STATUS_BASE + 1;

    private static final int MESSAGE_BASE = 0x1000;
    private static final int MESSAGE = MESSAGE_BASE;
    private static final int MESSAGE_ID = MESSAGE_BASE + 1;

    private static final int USER_BASE = 0x2000;
    private static final int USER = USER_BASE;
    private static final int USER_ID = USER_BASE + 1;

    // 12 bits to the base type: 0, 0x1000, 0x2000, etc.
    private static final int BASE_SHIFT = 12;

    private static final String[] TABLE_NAMES = { StatusF.TABLE_NAME, };

    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        // URI matching table
        UriMatcher matcher = sURIMatcher;

        // All statuses
        matcher.addURI(AUTHORITY, "status", STATUS);
        // A specific status
        matcher.addURI(AUTHORITY, "status/#", STATUS_ID);

        // All users
        matcher.addURI(AUTHORITY, "user", USER);
        // A specific user
        matcher.addURI(AUTHORITY, "user/#", USER_ID);

        // All messages
        matcher.addURI(AUTHORITY, "message", MESSAGE);
        // A specific message
        matcher.addURI(AUTHORITY, "message/#", MESSAGE_ID);
    }

    /**
     * Internal helper method for index creation. Example:
     * "create index message_" + MessageColumns.FLAG_READ + " on " +
     * Message.TABLE_NAME + " (" + MessageColumns.FLAG_READ + ");"
     */
    /* package */static String createIndex(String tableName, String columnName) {
        return "create index " + tableName.toLowerCase() + '_' + columnName
                + " on " + tableName + " (" + columnName + ");";
    }

    /* package */static void createIndexes(SQLiteDatabase db, String tableName,
            String[] indexColumns) {
        for (String columnName : indexColumns) {
            db.execSQL(createIndex(StatusF.TABLE_NAME, columnName));
        }
    }

    /**
     * Get table name with URI match result
     * 
     * @param uriMatch
     *            , result of sURIMatcher.match(uri)
     * @return
     */
    private static String getTableNameByUriMatch(int uriMatch) {
        int tableIndex = uriMatch >> BASE_SHIFT;
        return TABLE_NAMES[tableIndex];
    }

    // Create Tables

    static void createStatusTable(SQLiteDatabase db) {
        String createString = " (" + FanContent.RECORD_ID
                + " integer primary key autoincrement, "
                + StatusColumns.STATUS_ID + " text, " + ");";

        db.execSQL("CREATE TABLE " + createString);

        String[] indexColumns = { StatusColumns.STATUS_ID };
        createIndexes(db, StatusF.TABLE_NAME, indexColumns);
    }

    static void resetStatusTable(SQLiteDatabase db, int oldVersion,
            int newVersion) {
        try {
            db.execSQL("DROP TABLE " + StatusF.TABLE_NAME);
        } catch (SQLException e) {
            /* do nothing */
        }
        createStatusTable(db);
    }

    private SQLiteDatabase mDatabase;

    public synchronized SQLiteDatabase getDatabase(Context context) {
        // Always return the cached database, if we've got one
        if (mDatabase != null) {
            return mDatabase;
        }

        DatabaseHelper helper = new DatabaseHelper(context, DATABASE_NAME);
        mDatabase = helper.getWritableDatabase();
        if (mDatabase != null) {
            mDatabase.setLockingEnabled(true);
        }

        return mDatabase;
    }

    /* package */static SQLiteDatabase getReadableDatabase(Context context) {
        DatabaseHelper helper = new FanProvider().new DatabaseHelper(context,
                DATABASE_NAME);
        return helper.getReadableDatabase();
    }

    /**
     * DatabaseHelper
     */
    private class DatabaseHelper extends SQLiteOpenHelper {
        Context mContext;

        public DatabaseHelper(Context context, String name) {
            super(context, name, null, DATABASE_VERSION);
            mContext = context;
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            Log.d(TAG, "Creating FanProvider database");
            createStatusTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            resetStatusTable(db, oldVersion, newVersion);
        }

    }

    private String whereWithId(String id, String selection) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("_id=");
        sb.append(id);
        if (selection != null) {
            sb.append(" AND (");
            sb.append(selection);
            sb.append(')');
        }
        return sb.toString();
    }

    /**
     * Combine a locally-generated selection with a user-provided selection
     * 
     * This introduces risk that the local selection might insert incorrect
     * chars into the SQL, so use caution.
     * 
     * @param where
     *            locally-generated selection, must not be null
     * @param selection
     *            user-provided selection, may be null
     * @return a single selection string
     */
    private String whereWith(String where, String selection) {
        if (selection == null) {
            return where;
        }
        StringBuilder sb = new StringBuilder(where);
        sb.append(" AND (");
        sb.append(selection);
        sb.append(')');

        return sb.toString();
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getType(Uri uri) {
        int match = sURIMatcher.match(uri);
        switch (match) {
        case STATUS:
            return "vnd.android.cursor.dir/fan-status";
        case STATUS_ID:
            return "vnd.android.cursor.item/fan-status";
        default:
            throw new IllegalArgumentException("UnKnown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sURIMatcher.match(uri);
        Log.v(TAG, "FanProvider.insert: uri=" + uri + ", match is " + match);
        
        Context context = getContext();
        SQLiteDatabase db = getDatabase(context);
        String table = getTableNameByUriMatch(match);
        long id;
        
        Uri resultUri = null;
        
        try {
            switch (match) {
            case STATUS:
                
                break;
            case STATUS_ID:
                break;
            }
            
        } catch (SQLiteException e) {
            throw e;
        } finally {
            
        }
        
        
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sURIMatcher.match(uri);
        Log.v(TAG, "FanProvider.delete: uri=" + uri + ", match is " + match);

        Context context = getContext();
        SQLiteDatabase db = getDatabase(context);
        String table = getTableNameByUriMatch(match);
        String id = "0";

        int result = -1;

        try {
            switch (match) {
            case STATUS_ID:
                id = uri.getPathSegments().get(1);
                result = db.delete(table, whereWithId(id, selection),
                        selectionArgs);
                break;
            case STATUS:
                result = db.delete(table, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        } catch (SQLException e) {
            throw e;
        } finally {

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

}
