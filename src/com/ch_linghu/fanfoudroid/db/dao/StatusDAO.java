package com.ch_linghu.fanfoudroid.db.dao;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ch_linghu.fanfoudroid.data2.Photo;
import com.ch_linghu.fanfoudroid.data2.Status;
import com.ch_linghu.fanfoudroid.data2.User;
import com.ch_linghu.fanfoudroid.db.StatusTable;
import com.ch_linghu.fanfoudroid.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.helper.utils.DateTimeHelper;
import com.ch_linghu.fanfoudroid.helper.utils.TextHelper;

public class StatusDAO {
    private static final String TAG = "StatusDAO";
    
    private static RowMapper<Status> mRowMapper = new StatusMapper();
    private static String mTable = StatusTable.TABLE_NAME;
    private static String mPrimaryKey = "_id";

    private SqliteTemplate mSqlTemplate;
    private SQLiteDatabase mDb;

    public StatusDAO() {
        mDb = TwitterDatabase.getDb(true);
        mSqlTemplate = new SqliteTemplate(mDb, mPrimaryKey);
    }

    /**
     * Insert a Status
     * 
     * 若报 SQLiteconstraintexception 异常, 检查是否某not null字段为空
     * 
     * @param status
     * @param isUnread
     * @return
     */
    public long insertStatus(Status status) {
        if (!isExists(status)) {
            return mDb.insert(mTable, null, statusToContentValues(status));
        } else {
            Log.e(TAG, status.getId() + " is exists.");
            return -1;
        }
    }

    // TODO:
    public int insertStatuses(List<Status> statuses) {
        int result = 0;
        try {
            mDb.beginTransaction();
            for (int i = statuses.size() - 1; i >= 0; i--) {
                Status status = statuses.get(i);

                long id = mDb.insert(StatusTable.TABLE_NAME, null,
                        statusToContentValues(status));

                if (-1 == id) {
                    Log.e(TAG, "cann't insert the tweet : " + status.toString());
                } else {
                    ++result;
                    Log.v(TAG, String.format(
                            "Insert a status into database : %s",
                            status.toString()));
                }
            }
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }
        return result;
    }

    /**
     * Delete a status
     * 
     * @param statusId
     * @param owner
     *            owner id
     * @param type
     *            status type
     * @return
     * @see StatusDAO#deleteStatus(Status)
     */
    public int deleteStatus(String statusId, String owner, int type) {
        String where = StatusTable._ID + " =? ";
        String[] binds;

        if (!TextHelper.isEmpty(owner)) {
            where += " AND " + StatusTable.OWNER_ID + " = ? ";
            binds = new String[] { statusId, owner };
        } else {
            binds = new String[] { statusId };
        }

        if (-1 != type) {
            where += " AND " + StatusTable.STATUS_TYPE + " = " + type;
        }

        return mDb.delete(mTable, where.toString(), binds);
    }

    /**
     * Delete a Status
     * 
     * @param status
     * @return
     * @see StatusDAO#deleteStatus(String, String, int)
     */
    public int deleteStatus(Status status) {
        return deleteStatus(status.getId(), status.getOwnerId(),
                status.getType());
    }

    /**
     * Find a status by status ID
     * 
     * @param statusId
     * @return
     */
    public Status findStatus(String statusId) {
        return mSqlTemplate.queryForObject(mRowMapper, mTable, null,
                mPrimaryKey + " = ?", new String[] { statusId }, null, null,
                "created_at DESC", "1");
    }

    /**
     * Find user's statuses
     * 
     * @param userId user id
     * @param statusType status type, see {@link StatusTable#TYPE_USER}...
     * @return list of statuses
     */
    public List<Status> findStatuses(String userId, int statusType) {
        return mSqlTemplate.queryForList(mRowMapper, mTable, null,
                StatusTable.OWNER_ID + " = ? AND " + StatusTable.STATUS_TYPE
                        + " = " + statusType, new String[] { userId }, null,
                null, "created_at DESC", null);
    }

    /**
     * @see StatusDAO#findStatuses(String, int)
     */
    public List<Status> findStatuses(String userId, String statusType) {
        return findStatuses(userId, Integer.parseInt(statusType));
    }

    /**
     * Update by using {@link ContentValues}
     * 
     * @param statusId
     * @param newValues
     * @return
     */
    public int updateStatus(String statusId, ContentValues values) {
        return mSqlTemplate.updateById(mTable, statusId, values);
    }

    /**
     * Update by using {@link Status}
     * 
     * @param status
     * @return
     */
    public int updateStatus(Status status) {
        return updateStatus(status.getId(), statusToContentValues(status));
    }

    /**
     * Check if status exists
     * 
     * @param status
     * @return
     */
    public boolean isExists(Status status) {
        boolean result = false;
        if (null != status.getId()) {
            Query query = new Query(TwitterDatabase.getDb(false));
            query.from(StatusTable.TABLE_NAME, new String[] { "_id" })
                    .where("_id = ?", status.getId())
                    .where("owner = ?", status.getUser().getId())
                    .where("status_type = " + status.getType());

            Cursor cursor = query.select();
            result = (cursor != null && cursor.getCount() > 0);
            cursor.close();
        }
        return result;
    }

    /**
     * Status -> ContentValues
     * 
     * @param status
     * @param isUnread
     * @return
     */
    private ContentValues statusToContentValues(Status status) {
        ContentValues v = new ContentValues();
        v.put(StatusTable._ID, status.getId());
        v.put(StatusTable.STATUS_TYPE, status.getType());
        v.put(StatusTable.TEXT, status.getText());
        v.put(StatusTable.OWNER_ID, status.getOwnerId());
        v.put(StatusTable.USER_ID, status.getUser().getId());
        v.put(StatusTable.USER_SCREEN_NAME, status.getUser().getScreenName());
        v.put(StatusTable.PROFILE_IMAGE_URL, status.getUser()
                .getProfileImageUrl());
        Photo photo = status.getPhotoUrl();
        if (photo != null) {
            v.put(StatusTable.PIC_THUMB, photo.getThumburl());
            v.put(StatusTable.PIC_MID, photo.getImageurl());
            v.put(StatusTable.PIC_ORIG, photo.getLargeurl());
        }
        v.put(StatusTable.FAVORITED, status.isFavorited() + "");
        v.put(StatusTable.TRUNCATED, status.isTruncated()); // TODO:
        v.put(StatusTable.IN_REPLY_TO_STATUS_ID, status.getInReplyToStatusId());
        v.put(StatusTable.IN_REPLY_TO_USER_ID, status.getInReplyToUserId());
        v.put(StatusTable.IN_REPLY_TO_SCREEN_NAME,
                status.getInReplyToScreenName());
        // v.put(IS_REPLY, status.isReply());
        v.put(StatusTable.CREATED_AT,
                TwitterDatabase.DB_DATE_FORMATTER.format(status.getCreatedAt()));
        v.put(StatusTable.SOURCE, status.getSource());
        v.put(StatusTable.IS_UNREAD, status.isUnRead());

        return v;
    }

    /**
     * Cursor -> Status for SqliteTemplate
     */
    private static final class StatusMapper implements RowMapper<Status> {

        @Override
        public Status mapRow(Cursor cursor, int rowNum) {
            Photo photo = new Photo();
            photo.setImageurl(cursor.getString(cursor
                    .getColumnIndex(StatusTable.PIC_MID)));
            photo.setLargeurl(cursor.getString(cursor
                    .getColumnIndex(StatusTable.PIC_ORIG)));
            photo.setThumburl(cursor.getString(cursor
                    .getColumnIndex(StatusTable.PIC_THUMB)));

            User user = new User();
            user.setScreenName(cursor.getString(cursor
                    .getColumnIndex(StatusTable.USER_SCREEN_NAME)));
            user.setId(cursor.getString(cursor
                    .getColumnIndex(StatusTable.USER_ID)));
            user.setProfileImageUrl(cursor.getString(cursor
                    .getColumnIndex(StatusTable.PROFILE_IMAGE_URL)));

            Status status = new Status();
            status.setPhotoUrl(photo);
            status.setUser(user);
            status.setOwnerId(cursor.getString(cursor
                    .getColumnIndex(StatusTable.OWNER_ID)));
            // TODO: 将数据库中的statusType改成Int类型
            status.setType(cursor.getInt(cursor
                    .getColumnIndex(StatusTable.STATUS_TYPE)));
            status.setId(cursor.getString(cursor
                    .getColumnIndex(StatusTable._ID)));
            status.setCreatedAt(DateTimeHelper.parseDateTimeFromSqlite(cursor
                    .getString(cursor.getColumnIndex(StatusTable.CREATED_AT))));
            // TODO: 更改favorite 在数据库类型为boolean后改为 " != 0 "
            status.setFavorited(cursor.getString(
                    cursor.getColumnIndex(StatusTable.FAVORITED))
                    .equals("true"));
            status.setText(cursor.getString(cursor
                    .getColumnIndex(StatusTable.TEXT)));
            status.setSource(cursor.getString(cursor
                    .getColumnIndex(StatusTable.SOURCE)));
            status.setInReplyToScreenName(cursor.getString(cursor
                    .getColumnIndex(StatusTable.IN_REPLY_TO_SCREEN_NAME)));
            status.setInReplyToStatusId(cursor.getString(cursor
                    .getColumnIndex(StatusTable.IN_REPLY_TO_STATUS_ID)));
            status.setInReplyToUserId(cursor.getString(cursor
                    .getColumnIndex(StatusTable.IN_REPLY_TO_USER_ID)));
            status.setTruncated(cursor.getInt(cursor
                    .getColumnIndex(StatusTable.TRUNCATED)) != 0);
            status.setUnRead(cursor.getInt(cursor
                    .getColumnIndex(StatusTable.IS_UNREAD)) != 0);
            return status;
        }
    }

}