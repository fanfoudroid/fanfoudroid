package com.temp.afan.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.temp.afan.db.FanContent.StatusColumns;

public final class FStatus extends FanContent implements StatusColumns {
    public static final String TABLE_NAME = "statuses";
    public static final int CONTENT_ID_COLUMN = 0;
    public static final int CONTENT_STATUS_ID_COLUMN = 1;
    public static final int CONTENT_AUTHOR_ID_COLUMN = 2;
    public static final int CONTENT_TEXT_COLUMN = 3;
    public static final String[] CONTENT_PROJECTION = new String[] {
        RECORD_ID, STATUS_ID, AUTHOR_ID, TEXT,
    };
    
    public String mStatusId;
    public String mAuthorId;
    public String mText;

    @Override
    public ContentValues toContentValues() {
        // TODO Auto-generated method stub
        ContentValues values = new ContentValues();

        // Assign values for each row.
        values.put(STATUS_ID, mStatusId);
        values.put(AUTHOR_ID, mAuthorId);
        values.put(TEXT, mText);
        return values;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FStatus restore(Cursor c) {
        // TODO Auto-generated method stub
        mStatusId = c.getString(CONTENT_STATUS_ID_COLUMN);
        mAuthorId = c.getString(CONTENT_AUTHOR_ID_COLUMN);
        mText = c.getString(CONTENT_TEXT_COLUMN);
        return this;
    }
    
    private static FStatus restoreBodyWithCursor(Cursor cursor) {
        return restoreWithCursor(cursor, FStatus.class);
    }
    
    public static FStatus restoreStatusWithId(Context context, long id) {
        // TODO
        return restoreBodyWithCursor(null);
    }
    
    /**
     * Restore all the Statuses of the user
     */
    public static FStatus[] restoreStatusesWithUserAndType(Context context,
            String user_id, String timelineType) {
        Cursor c = null; // TODO:
        try {
            int count = c.getCount();
            FStatus[] statuses = new FStatus[count];
            for (int i = 0; i < count; ++i) {
                c.moveToNext();
                statuses[i] = new FStatus().restore(c);
            }
            return statuses;
        } finally {
            c.close();
        }
    }
}
