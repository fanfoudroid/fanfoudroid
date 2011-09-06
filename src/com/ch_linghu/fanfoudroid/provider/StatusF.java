package com.ch_linghu.fanfoudroid.provider;

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;

import com.ch_linghu.fanfoudroid.provider.FanContent.StatusColumns;

public class StatusF extends FanContent implements StatusColumns {
    public static final String TABLE_NAME = "Status";
    // public static final Uri CONTENT_URI = Uri.parse(EmailContent.CONTENT_URI
    // + "/body");

    // TODO: type, isUnRead...
    public static final int CONTENT_ID_COLUMN = 0;
    public static final int CONTENT_STATUS_ID_COLUMN = 1;
    public static final int CONTENT_AUTHOR_ID_COLUMN = 2;
    public static final int CONTENT_TEXT_COLUMN = 3;
    public static final int CONTENT_SOURCE_COLUMN = 4;
    public static final int CONTENT_CREATED_AT_COLUMN = 5;
    public static final int CONTENT_TRUNCATED_COLUMN = 6;
    public static final int CONTENT_FAVORIFED_COLUMN = 7;
    public static final int CONTENT_PHOTO_COLUMN = 8;
    public static final int CONTENT_IN_REPLY_TO_STATUS_ID_COLUMN = 9;
    public static final int CONTENT_IN_REPLY_TO_USER_ID_COLUMN = 10;
    public static final int CONTENT_IN_REPLY_TO_SCREEN_COLUMN = 11;
    public static final String[] CONTENT_PROJECTION = new String[] { RECORD_ID,
            STATUS_ID, AUTHOR_ID, TEXT, SOURCE, CREATED_AT, TRUNCATED,
            FAVORITED, PHOTO_URL, IN_REPLY_TO_STATUS_ID, IN_REPLY_TO_USER_ID,
            IN_REPLY_TO_SCREEN_NAME };
    
    private String status_id;
    private String author_id;
    private String text;
    private String source;
    private Date created_at;
    private boolean truncated;
    private boolean favorited;
    private String photo_url;
    private String in_reply_to_status_id;
    private String in_reply_to_user_id;
    private String in_reply_to_screen_name;
    private boolean isUnRead = false;
    private int type = -1;
    
    public StatusF() {
        
    }

    @Override
    public ContentValues toContentValues() {
        final ContentValues values = new ContentValues();
        
        // Assign values for each row.
        values.put(STATUS_ID, status_id);
        values.put(AUTHOR_ID, author_id);
        values.put(TEXT, text);
        values.put(SOURCE, source);
        values.put(CREATED_AT, created_at.toGMTString()); // TODO: format date
        values.put(TRUNCATED, truncated);
        values.put(FAVORITED, favorited);
        values.put(PHOTO_URL, photo_url);
        values.put(IN_REPLY_TO_STATUS_ID, in_reply_to_status_id);
        values.put(IN_REPLY_TO_USER_ID, in_reply_to_user_id);
        values.put(IN_REPLY_TO_SCREEN_NAME, in_reply_to_screen_name);
        // TODO: isUnRead...
        return values;
    }

    @Override
    @SuppressWarnings("unchecked")
    public StatusF restore(Cursor c) {
        this.status_id = c.getString(CONTENT_STATUS_ID_COLUMN);
        // TODO: and so on
        return this;
    }
    
    @Override
    public String toString() {
        // TODO
        return "Status [created_at=" + created_at + ", id=" + status_id 
                + ", text=" + text 
                + ", source=" + source 
                + ", truncated=" + truncated
                + ", in_reply_to_status_id=" + in_reply_to_status_id
                + ", in_reply_to_user_id=" + in_reply_to_user_id
                + ", favorited=" + favorited 
                + ", in_reply_to_screen_name=" + in_reply_to_screen_name 
                + ", photo_url=" + photo_url
                  +  "]";
    }

}
