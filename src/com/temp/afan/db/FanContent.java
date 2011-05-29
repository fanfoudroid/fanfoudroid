package com.temp.afan.db;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class FanContent {
    // All classes share this
    public static final String RECORD_ID = "_id";
    // Newly created objects get this id
    private static final int NOT_SAVED = -1;
    // The id of the Content
    public long mId = NOT_SAVED;
    
    // Write the Content into a ContentValues container
    public abstract ContentValues toContentValues();
    // Read the Content from a ContentCursor
    public abstract <T extends FanContent> T restore (Cursor cursor);
    
    public boolean isSaved() {
        return mId != NOT_SAVED;
    }
    
    @SuppressWarnings("unchecked")
    // The Content sub class must have a no-arg constructor
    public static <T extends FanContent> T getContent(Cursor cursor, Class<T> klass) {
        try {
            T content = klass.newInstance();
            content.mId = cursor.getLong(0);
            return (T)content.restore(cursor);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static <T extends FanContent> T restoreWithCursor(Cursor cursor, Class<T> klass) {
        try {
            if (cursor.moveToFirst()) {
                return getContent(cursor, klass);
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
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
        + FStatus.TABLE_NAME + "( "
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
