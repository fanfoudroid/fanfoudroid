package com.ch_linghu.fanfoudroid.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * FanContent
 * 所有Content类型的抽象类, 包含所有与Content相关的信息。 和 {@link FanProvider} 协作
 * 
 * @author lds
 */
public abstract class FanContent {
    public static final String AUTHORITY = FanProvider.AUTHORITY;

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    // All classes share this
    public static final String RECORD_ID = "_id";

    private static final String[] COUNT_COLUMNS = new String[] { "count(*)" };

    /**
     * This projection can be used with any of the FanContent2 classes, when all
     * you need is a list of id's. Use ID_PROJECTION_COLUMN to access the row
     * data.
     */
    public static final String[] ID_PROJECTION = new String[] { RECORD_ID };
    public static final int ID_PROJECTION_COLUMN = 0;

    private static final String ID_SELECTION = RECORD_ID + " =?";

    public static final String FIELD_COLUMN_NAME = "field";
    public static final String ADD_COLUMN_NAME = "add";

    // Newly created objects get this id
    private static final int NOT_SAVED = -1;
    // The base Uri that this piece of content came from
    public Uri mBaseUri;
    // Lazily initialized uri for this Content
    private Uri mUri = null;
    // The id of the Content
    public long mId = NOT_SAVED;

    // Write the Content into a ContentValues container
    public abstract ContentValues toContentValues();

    // Read the Content from a ContentCursor
    public abstract <T extends FanContent> T restore(Cursor cursor);

    // The Uri is lazily initialized
    public Uri getUri() {
        if (mUri == null) {
            mUri = ContentUris.withAppendedId(mBaseUri, mId);
        }
        return mUri;
    }

    public boolean isSaved() {
        return mId != NOT_SAVED;
    }

    @SuppressWarnings("unchecked")
    // The Content sub class must have a no-arg constructor
    static public <T extends FanContent> T getContent(Cursor cursor,
            Class<T> klass) {
        try {
            T content = klass.newInstance();
            content.mId = cursor.getLong(0);
            return (T) content.restore(cursor);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Uri save(Context context) {
        if (isSaved()) {
            throw new UnsupportedOperationException();
        }
        Uri res = context.getContentResolver().insert(mBaseUri,
                toContentValues());
        mId = Long.parseLong(res.getPathSegments().get(1));
        return res;
    }

    public int update(Context context, ContentValues contentValues) {
        if (!isSaved()) {
            throw new UnsupportedOperationException();
        }
        return context.getContentResolver().update(getUri(), contentValues,
                null, null);
    }

    static public int update(Context context, Uri baseUri, long id,
            ContentValues contentValues) {
        return context.getContentResolver().update(
                ContentUris.withAppendedId(baseUri, id), contentValues, null,
                null);
    }

    /**
     * Generic count method that can be used for any ContentProvider
     * 
     * @param context
     *            the calling Context
     * @param uri
     *            the Uri for the provider query
     * @param selection
     *            as with a query call
     * @param selectionArgs
     *            as with a query call
     * @return the number of items matching the query (or zero)
     */
    static public int count(Context context, Uri uri, String selection,
            String[] selectionArgs) {
        Cursor cursor = context.getContentResolver().query(uri, COUNT_COLUMNS,
                selection, selectionArgs, null);
        try {
            if (!cursor.moveToFirst()) {
                return 0;
            }
            return cursor.getInt(0);
        } finally {
            cursor.close();
        }
    }

    /**
     * no public constructor since this is a utility class
     */
    public FanContent() {
    }

    // Columns

    /**
     * 12
     */
    public interface StatusColumns {
        public static final String ID = "_id";
        public static final String STATUS_ID = "status_id";
        public static final String AUTHOR_ID = "author_id";
        public static final String TEXT = "text";
        public static final String SOURCE = "source";
        public static final String CREATED_AT = "created_at";
        public static final String TRUNCATED = "truncated";
        public static final String FAVORITED = "favorited";
        public static final String PHOTO_URL = "photo_url";
        public static final String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
        public static final String IN_REPLY_TO_USER_ID = "in_reply_to_user_id";
        public static final String IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";
    }
    
    

}