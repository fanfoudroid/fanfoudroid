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
    private SqliteTemplate sqliteTemplate;
    
    public StatusDAO() {
        sqliteTemplate = new SqliteTemplate();
    }

    /**
     * Insert a Status
     * 
     * 若报 SQLiteconstraintexception 异常, 检查是否某not null字段为空
     * @param status
     * @param isUnread
     * @return
     */
    public long insertStatus(Status status) {
        if (!isExists(status)) {
            Query query = new Query();
            query.into(StatusTable.TABLE_NAME)
                 .values(statusToValues(status));
            
            return sqliteTemplate.insert(query);
        }
        Log.e(TAG, status.getId() + "is exists.");
        return -1;
    }
    
    // TODO: 
    public int insertStatuses(List<Status> statuses) {
        SQLiteDatabase db = TwitterDatabase.getDb(true);
        
        int result = 0;
        try {
            db.beginTransaction();

            for (int i = statuses.size() - 1; i >= 0; i--) {
                Status status = statuses.get(i);
 
                long id = db.insert(StatusTable.TABLE_NAME, null, statusToValues(status));

                if (-1 == id) {
                    Log.e(TAG, "cann't insert the tweet : " + status.toString());
                } else {
                    ++result;
                    Log.v(TAG, String.format("Insert a status into database : %s",
                             status.toString()));
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return result;
    }

    /**
     * Delete a status
     * 
     * @param statusId
     * @param owner owner id
     * @param type status type
     * @return succeed or fail
     * @see StatusDAO#deleteStatus(Status)
     */
    public boolean deleteStatus(String statusId, String owner, int type) {
        Query query = new Query();
        query.from(StatusTable.TABLE_NAME)
             .where(StatusTable._ID + "=?", statusId);

        if ( !TextHelper.isEmpty(owner) ) {
            query.where(StatusTable.OWNER_ID + "=?", owner);
        }
        if ( -1 != type ) {
            query.where(StatusTable.STATUS_TYPE + "=" + type);
        }
        return sqliteTemplate.delete(query);
    }
    
    /**
     * Delete a Status
     * 
     * @param status
     * @return succeed or fail
     * @see StatusDAO#deleteStatus(String, String, int)
     */
    public boolean deleteStatus(Status status) {
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
        Query query = new Query();
        query.from(StatusTable.TABLE_NAME, null)
             .where("_id =?", statusId)
             .orderBy("created_at DESC");
        
        return sqliteTemplate.queryForObject(query, new StatusMapper());
    }
    
    /**
     * Find user's statuses
     * 
     * @param userId
     * @param statusType
     * @return list of statuses
     */
    public List<Status> findStatuses(String userId, int statusType) {
        Query query = new Query();
        query.from(StatusTable.TABLE_NAME, null)
             .where("owner = ?", userId)
             .where("status_type = " + statusType)
             .orderBy("created_at DESC");
        
        return sqliteTemplate.queryForList(query, new StatusMapper());
    }
    
    /**
     * Find user's statuses
     * 
     * @param userId
     * @param statusType
     * @return 
     * @see StatusDAO#findStatuses(String, int)
     */
    public List<Status> findStatuses(String userId, String statusType) {
        return findStatuses(userId, Integer.parseInt(statusType));
    }

    /**
     * Update status with newValues
     * 
     * @param statusId
     * @param newValues
     * @return
     */
    public boolean updateStatus(String statusId, ContentValues newValues) {
        Query query = new Query();
        query.setTable(StatusTable.TABLE_NAME)
             .where(StatusTable._ID + "=?", statusId)
             .values(newValues);
        return ( sqliteTemplate.upload(query) > 0 );
    }
    
    /**
     * Update status with fields
     * 
     * @param status
     * @return
     */
    public boolean updateStatus(Status status) {
        return updateStatus(status.getId(), statusToValues(status));
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
            result = ( cursor != null && cursor.getCount() > 0);
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
    private ContentValues statusToValues(Status status) {
        ContentValues v = new ContentValues();
        v.put(StatusTable._ID, status.getId());
        v.put(StatusTable.STATUS_TYPE, status.getType());
        v.put(StatusTable.TEXT, status.getText());
        v.put(StatusTable.OWNER_ID, status.getOwnerId());
        v.put(StatusTable.USER_ID, status.getUser().getId());
        v.put(StatusTable.USER_SCREEN_NAME, status.getUser().getScreenName());
        v.put(StatusTable.PROFILE_IMAGE_URL, status.getUser().getProfileImageUrl());
        Photo photo = status.getPhotoUrl();
        v.put(StatusTable.PIC_THUMB, photo.getThumburl());
        v.put(StatusTable.PIC_MID, photo.getImageurl());
        v.put(StatusTable.PIC_ORIG, photo.getLargeurl());
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
     * Cursor -> Status
     * for SqliteTemplate
     */
    private static final class StatusMapper implements RowMapper<Status> {

        @Override
        public Status mapRow(Cursor cursor, int rowNum) {
            Photo photo = new Photo();
            photo.setImageurl(cursor.getString(
                    cursor.getColumnIndex(StatusTable.PIC_MID)));
            photo.setLargeurl(cursor.getString(
                    cursor.getColumnIndex(StatusTable.PIC_ORIG)));
            photo.setThumburl(cursor.getString(
                    cursor.getColumnIndex(StatusTable.PIC_THUMB)));
            
            User user = new User();
            user.setScreenName(cursor.getString(
                    cursor.getColumnIndex(StatusTable.USER_SCREEN_NAME)));
            user.setId(cursor.getString(
                    cursor.getColumnIndex(StatusTable.USER_ID)));
            user.setProfileImageUrl(cursor.getString(
                    cursor.getColumnIndex(StatusTable.PROFILE_IMAGE_URL)));
            
            Status status = new Status();
            status.setPhotoUrl(photo);
            status.setUser(user);
            status.setOwnerId(cursor.getString(
                    cursor.getColumnIndex(StatusTable.OWNER_ID)));
            //TODO: 将数据库中的statusType改成Int类型
            status.setType(cursor.getInt(
                    cursor.getColumnIndex(StatusTable.STATUS_TYPE)));
            status.setId(cursor.getString(
                    cursor.getColumnIndex(StatusTable._ID)));
            status.setCreatedAt(DateTimeHelper.parseDateTimeFromSqlite(cursor.getString(
                    cursor.getColumnIndex(StatusTable.CREATED_AT))));
            //TODO: 更改favorite 在数据库类型为boolean后改为 " != 0 "
            status.setFavorited(cursor.getString(
                    cursor.getColumnIndex(StatusTable.FAVORITED)).equals("true"));
            status.setText(cursor.getString(
                    cursor.getColumnIndex(StatusTable.TEXT)));
            status.setSource(cursor.getString(
                    cursor.getColumnIndex(StatusTable.SOURCE)));
            status.setInReplyToScreenName(cursor.getString(
                    cursor.getColumnIndex(StatusTable.IN_REPLY_TO_SCREEN_NAME)));
            status.setInReplyToStatusId(cursor.getString(
                    cursor.getColumnIndex(StatusTable.IN_REPLY_TO_STATUS_ID)));
            status.setInReplyToUserId(cursor.getString(
                    cursor.getColumnIndex(StatusTable.IN_REPLY_TO_USER_ID)));
            status.setTruncated(cursor.getInt(
                   cursor.getColumnIndex(StatusTable.TRUNCATED)) != 0);
            return status;
        }
    }

}