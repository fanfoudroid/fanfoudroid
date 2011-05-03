package com.ch_linghu.fanfoudroid.db.dao;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ch_linghu.fanfoudroid.data2.Status;
import com.ch_linghu.fanfoudroid.db.StatusTable;
import com.ch_linghu.fanfoudroid.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.helper.Utils;

public class StatusDAO {
    private static final String TAG = "StatusDAO";
    private SqliteTemplate sqliteTemplate;
    
    public StatusDAO() {
        sqliteTemplate = new SqliteTemplate(StatusTable.TABLE_NAME);
    }

    public long insertStatus(Status status, boolean isUnread) {
        if (! isExists(status)) {
            return sqliteTemplate.insert(statusToValues(status, isUnread));
        }
        Log.i(TAG, status.getId() + "is exists.");
        return -1;
    }

    public boolean deleteStatus() {
        return false;
    }
    
    public Status findStatus(String statusId) {
        Query query = new Query();
        query.where("_id =?", statusId)
             .orderBy("created_at DESC");
        
        return sqliteTemplate.queryForObject(query, new StatusMapper());
    }
    
    public List<Status> findStatuses(String userId, int statusType) {
        Query query = new Query();
        query.where("owner = ?", userId)
             .where("status_type = " + statusType)
             .orderBy("created_at DESC");
        
        return sqliteTemplate.queryForList(query, new StatusMapper());
    }

    public boolean updateStatus(Status status) {
        return false;
    }

    /**
     * Check if a Status exists
     * 
     * @param status
     * @return
     */
    public boolean isExists(Status status) {
        boolean result = false;
        if (null != status.getId()) {
            Query select = new Query();
            select.from(StatusTable.TABLE_NAME, new String[] { "_id" })
                  .where("_id = ?", status.getId())
                  .where("owner = ?", status.getUserId())
                  .where("status_type = " + status.getType());
            
            SQLiteDatabase db = TwitterDatabase.getDb(false);
            Cursor cursor = select.query(db);
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
    private ContentValues statusToValues(Status status, boolean isUnread) {
        ContentValues v = new ContentValues();
        v.put(StatusTable._ID, status.getId());
        v.put(StatusTable.STATUS_TYPE, status.getType());
        v.put(StatusTable.TEXT, status.getText());
        v.put(StatusTable.OWNER_ID, status.getUserId());
        v.put(StatusTable.USER_SCREEN_NAME, status.getUserScreenName());
        v.put(StatusTable.PROFILE_IMAGE_URL, status.getProfileImageUrl());
        v.put(StatusTable.PIC_THUMB, status.getThumbnailPic());
        v.put(StatusTable.PIC_MID, status.getBmiddlePic());
        v.put(StatusTable.PIC_ORIG, status.getOriginalPic());
        v.put(StatusTable.FAVORITED, status.isFavorited() + "");
        v.put(StatusTable.TRUNCATED, status.isTruncated() + ""); // TODO:
        v.put(StatusTable.IN_REPLY_TO_STATUS_ID, status.getInReplyToStatusId());
        v.put(StatusTable.IN_REPLY_TO_USER_ID, status.getInReplyToUserId());
        v.put(StatusTable.IN_REPLY_TO_SCREEN_NAME,
                status.getInReplyToScreenName());
        // v.put(IS_REPLY, status.isReply());
        v.put(StatusTable.CREATED_AT,
                TwitterDatabase.DB_DATE_FORMATTER.format(status.getCreatedAt()));
        v.put(StatusTable.SOURCE, status.getSource());
        v.put(StatusTable.IS_UNREAD, isUnread);

        return v;
    }
    
   
    /** For SqliteTemplate */
    private static final class StatusMapper implements RowMapper<Status> {

        @Override
        public Status mapRow(Cursor cursor, int rowNum) {
            Status status = new Status();
            
            status.setId(cursor.getString(
                    cursor.getColumnIndex(StatusTable._ID)));
            status.setCreatedAt(Utils.parseDateTimeFromSqlite(cursor.getString(
                    cursor.getColumnIndex(StatusTable.CREATED_AT))));
            status.setFavorited(cursor.getInt(
                    cursor.getColumnIndex(StatusTable.FAVORITED)) != 0);
            status.setUserScreenName(cursor.getString(
                    cursor.getColumnIndex(StatusTable.USER_SCREEN_NAME)));
            status.setUserId(cursor.getString(
                    cursor.getColumnIndex(StatusTable.USER_ID)));
            status.setText(cursor.getString(
                    cursor.getColumnIndex(StatusTable.TEXT)));
            status.setSource(cursor.getString(
                    cursor.getColumnIndex(StatusTable.SOURCE)));
            status.setProfileImageUrl(cursor.getString(
                    cursor.getColumnIndex(StatusTable.PROFILE_IMAGE_URL)));
            status.setInReplyToScreenName(cursor.getString(
                    cursor.getColumnIndex(StatusTable.IN_REPLY_TO_SCREEN_NAME)));
            status.setInReplyToStatusId(cursor.getString(
                    cursor.getColumnIndex(StatusTable.IN_REPLY_TO_STATUS_ID)));
            status.setInReplyToUserId(cursor.getString(
                    cursor.getColumnIndex(StatusTable.IN_REPLY_TO_USER_ID)));
            //TODO: 将数据库中的 isFav, isUnRead, isTruncated 列该改成 boolean 
            //status.setTruncated(cursor.getString(
            //        cursor.getColumnIndex(StatusTable.TRUNCATED)));
            status.setThumbnailPic(cursor.getString(
                    cursor.getColumnIndex(StatusTable.PIC_THUMB)));
            status.setBmiddlePic(cursor.getString(
                    cursor.getColumnIndex(StatusTable.PIC_MID)));
            status.setOriginalPic(cursor.getString(
                    cursor.getColumnIndex(StatusTable.PIC_ORIG)));
            //TODO: 将数据库中的statusType改成int类型
            status.setType(cursor.getString(
                    cursor.getColumnIndex(StatusTable.STATUS_TYPE)));

            return status;
        }
    }

}