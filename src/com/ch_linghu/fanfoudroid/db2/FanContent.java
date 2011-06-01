package com.ch_linghu.fanfoudroid.db2;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ch_linghu.fanfoudroid.data2.Status;
import com.ch_linghu.fanfoudroid.db.TwitterDatabase;
import com.temp.afan.data.dao.RowMapper;

public abstract class FanContent {
    // All classes share this
    public static final String RECORD_ID = "_id";
    // Newly created objects get this id
    private static final int NOT_SAVED = -1;
    // The id of the Content
    public long mId = NOT_SAVED;
    
    public boolean isSaved() {
        return mId != NOT_SAVED;
    }
    
    public static int deleteByField(String table, String field, String value) {
        return getDb(true).delete(table, field + "=?", new String[] { value });
    }

    public static int deleteById(String table, String id) {
        return deleteByField(table, RECORD_ID, id);
    }

    public static int updateById(String table, String id, ContentValues values) {
        return getDb(true).update(table, values, RECORD_ID + "=?",
                new String[] { id });
    }
    
    public static boolean isExistsById(String table, String id) {
        return isExistsByField(table, RECORD_ID, id);
    }

    /**
     * Check if exists
     * 
     * @param status
     * @return
     */
    public static boolean isExistsByField(String table, String field, String value) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(table).append(" WHERE ")
                .append(field).append(" =?");

        return isExistsBySQL(sql.toString(), new String[] { value });
    }
    
    public static boolean isExistsBySQL(String sql, String[] selectionArgs) {
        boolean result = false;

        final Cursor c = getDb(false).rawQuery(sql, selectionArgs);
        try {
            if (c.moveToFirst()) {
                result = (c.getInt(0) > 0);
            }
        } finally {
            c.close();
        }
        return result;
    }
    
    public static <T> T valueOf(RowMapper<T> rowMapper, Cursor cursor) {
        return rowMapper.mapRow(cursor, cursor.getCount());
    }

    /**
     * Query for cursor
     * 
     * @param <T>
     * @param rowMapper
     * @return a cursor
     * 
     * @see SQLiteDatabase#query(String, String[], String, String[], String,
     *      String, String, String)
     */
    public static <T> T queryForObject(RowMapper<T> rowMapper, String table,
            String[] columns, String selection, String[] selectionArgs,
            String groupBy, String having, String orderBy, String limit) {
        T object = null;

        final Cursor c = getDb(false).query(table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
        try {
            if (c.moveToFirst()) {
                object = rowMapper.mapRow(c, c.getCount());
            }
        } finally {
            c.close();
        }
        return object;
    }

    /**
     * Query for list
     * 
     * @param <T>
     * @param rowMapper
     * @return list of object
     * 
     * @see SQLiteDatabase#query(String, String[], String, String[], String,
     *      String, String, String)
     */
    public static <T> List<T> queryForList(RowMapper<T> rowMapper, String table,
            String[] columns, String selection, String[] selectionArgs,
            String groupBy, String having, String orderBy, String limit) {
        List<T> list = new ArrayList<T>();

        final Cursor c = getDb(false).query(table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
        try {
            while (c.moveToNext()) {
                list.add(rowMapper.mapRow(c, 1));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public static SQLiteDatabase getDb(boolean writeable) {
        return TwitterDatabase.getDb(writeable);
    }
    
    // Columns
   
    public interface StatusColumns {
        public static final String ID = "_id";
        public static final String STATUS_ID = "status_id";
        public static final String AUTHOR_ID = "author_id";
        public static final String TEXT = "text";
        public static final String SOURCE = "source";
        public static final String TRUNCATED = "truncated";
        public static final String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
        public static final String IN_REPLY_TO_USER_ID = "in_reply_to_user_id";
        public static final String FAVORITED =  "favorited";
        public static final String CREATED_AT = "created_at";
        public static final String PIC_THUMB = "pic_thumbnail";
        public static final String PIC_MID = "pic_middle";
        public static final String PIC_ORIG = "pic_original";
        
        public static final String STATUS_TYPE = "status_type";
    }
    
    public interface StatusGroups {
        public static final String ID = "_id";
        public static final String TYPE = "TYPE";
        public static final String OWNER_ID = "owner_id";
        public static final String TAG = "tag";
        public static final String STATUS_ID = "g_status_id";
        public static final String IS_READ = "is_read";
        public static final String IS_LAST = "is_last";
        public static final String TIMELINE = "timeline";
    }
    
    public interface UserColumns {
        public static final String ID = "_id";
        public static final String USER_ID = "user_id";
        public static final String USER_NAME = "user_name";
        public static final String SCREEN_NAME = "screen_name";
        public static final String LOCATION = "location";
        public static final String DESCRIPTION = "description";
        public static final String PROFILE_IMAGE_USER = "profile_image_user";
        public static final String URL = "url";
        public static final String PROTECTED = "protected";
        public static final String FOLLOWERS_COUNT = "followers_count";
        public static final String STATUS_ID = "status_id";
    }
    
    public interface UserGroupsColumns {
        public static final String ID = "_id";
        public static final String TYPE = "type";
        public static final String TAG = "tag";
        public static final String OWNER_ID = "owner_id";
        public static final String USER_ID = "user_id";
        public static final String IS_LAST = "is_last";
    }
    
    // TODO:
    public static final String CREATE_TABLE_STATUSES = "CREATE TABLE " 
        + Status.TABLE_NAME + "( "
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
        + StatusColumns.PIC_THUMB + " TEXT DEFAULT '', "
        + StatusColumns.PIC_MID + " TEXT DEFAULT '', "
        + StatusColumns.PIC_ORIG + " TEXT DEFAULT '' "
        + " ) ";
}
