package com.ch_linghu.fanfoudroid.dao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.dao.SQLiteTemplate.RowMapper;
import com.ch_linghu.fanfoudroid.data2.Photo;
import com.ch_linghu.fanfoudroid.data2.Status;
import com.ch_linghu.fanfoudroid.data2.User;
import com.ch_linghu.fanfoudroid.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.db2.FanContent;
import com.ch_linghu.fanfoudroid.db2.FanContent.StatusesPropertyTable;
import com.ch_linghu.fanfoudroid.db2.FanDatabase;
import com.ch_linghu.fanfoudroid.util.DateTimeHelper;
import com.ch_linghu.fanfoudroid.db2.FanContent.*;

public class StatusDAO {
    private static final String TAG = "StatusDAO";

    private SQLiteTemplate mSqlTemplate;

    public final static DateFormat DB_DATE_FORMATTER = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
    
    public StatusDAO(Context context) {
        mSqlTemplate = new SQLiteTemplate(FanDatabase.getInstance(context)
                .getSQLiteOpenHelper());
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
            return mSqlTemplate.getDb(true).insert(StatusesTable.TABLE_NAME,
                    null, statusToContentValues(status));
        } else {
            Log.e(TAG, status.getStatusId() + " is exists.");
            return -1;
        }
    }

    // TODO:
    public int insertStatuses(List<Status> statuses) {
        int result = 0;
        SQLiteDatabase db = mSqlTemplate.getDb(true);

        try {
            db.beginTransaction();
            for (int i = statuses.size() - 1; i >= 0; i--) {
                Status status = statuses.get(i);

                long id = db.insertWithOnConflict(StatusesTable.TABLE_NAME,
                        null, statusToContentValues(status),
                        SQLiteDatabase.CONFLICT_IGNORE);

                if (-1 == id) {
                    Log.e(TAG, "cann't insert the tweet : " + status.toString());
                } else {
                    ++result;
                    Log.v(TAG, String.format(
                            "Insert a status into database : %s",
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
     * @param owner_id
     *            owner id
     * @param type
     *            status type
     * @return
     * @see StatusDAO#deleteStatus(Status)
     */
    public int deleteStatus(String statusId, String owner_id, int type) {
        // FIXME: 数据模型改变后这里的逻辑需要完全重写，目前仅保证编译可通过
        String where = StatusesTable.Columns.ID + " =? ";
        String[] binds;

        if (!TextUtils.isEmpty(owner_id)) {
            where += " AND " + StatusesPropertyTable.Columns.OWNER_ID + " = ? ";
            binds = new String[] { statusId, owner_id };
        } else {
            binds = new String[] { statusId };
        }

        if (-1 != type) {
            where += " AND " + StatusesPropertyTable.Columns.TYPE + " = "
                    + type;
        }

        return mSqlTemplate.getDb(true).delete(StatusesTable.TABLE_NAME,
                where.toString(), binds);
    }

    /**
     * Delete a Status
     * 
     * @param status
     * @return
     * @see StatusDAO#deleteStatus(String, String, int)
     */
    public int deleteStatus(Status status) {
        return deleteStatus(status.getStatusId(), status.getStatusId(),
                status.getType());
    }

    /**
     * Find a status by status ID
     * 
     * @param statusId
     * @return
     */
    public Status fetchStatus(String statusId) {
        return mSqlTemplate.queryForObject(mRowMapper,
                StatusesTable.TABLE_NAME, null, StatusesTable.Columns.ID
                        + " = ?", new String[] { statusId }, null, null,
                "created_at DESC", "1");
    }

    /**
     * Find user's statuses
     * 
     * @param userId
     *            user id
     * @param statusType
     *            status type, see {@link StatusTable#TYPE_USER}...
     * @return list of statuses
     */
    public List<Status> fetchStatuses(String userId, int statusType) {
        return mSqlTemplate.queryForList(mRowMapper,
                FanContent.StatusesTable.TABLE_NAME, null,
                StatusesPropertyTable.Columns.OWNER_ID + " = ? AND "
                        + StatusesPropertyTable.Columns.TYPE + " = "
                        + statusType, new String[] { userId }, null, null,
                "created_at DESC", null);
    }

    /**
     * @see StatusDAO#fetchStatuses(String, int)
     */
    public List<Status> fetchStatuses(String userId, String statusType) {
        return fetchStatuses(userId, Integer.parseInt(statusType));
    }

    /**
     * Update by using {@link ContentValues}
     * 
     * @param statusId
     * @param newValues
     * @return
     */
    public int updateStatus(String statusId, ContentValues values) {
        return mSqlTemplate.updateById(FanContent.StatusesTable.TABLE_NAME,
                statusId, values);
    }

    /**
     * Update by using {@link Status}
     * 
     * @param status
     * @return
     */
    public int updateStatus(Status status) {
        return updateStatus(status.getStatusId(), statusToContentValues(status));
    }

    /**
     * Check if status exists
     * 
     * FIXME: 取消使用Query
     * 
     * @param status
     * @return
     */
    public boolean isExists(Status status) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ")
                .append(FanContent.StatusesTable.TABLE_NAME).append(" WHERE ")
                .append(StatusesTable.Columns.ID).append(" =? AND ")
                .append(StatusesPropertyTable.Columns.OWNER_ID)
                .append(" =? AND ").append(StatusesPropertyTable.Columns.TYPE)
                .append(" = ").append(status.getType());
        return false;
        // return mSqlTemplate.isExistsBySQL(sql.toString(),
        // new String[] { status.getStatusId(), status.getUser().getStatusId()
        // });
    }

    /**
     * Status -> ContentValues
     * 
     * @param status
     * @param isUnread
     * @return
     */
    private ContentValues statusToContentValues(Status status) {
        final ContentValues v = new ContentValues();
        v.put(StatusesTable.Columns.ID, status.getStatusId());
        v.put(StatusesPropertyTable.Columns.TYPE, status.getType());
        v.put(StatusesTable.Columns.TEXT, status.getText());
        // v.put(StatusesPropertyTable.Columns.OWNER_ID, status.getOwnerId());
        v.put(StatusesTable.Columns.FAVORITED, status.isFavorited() + "");
        v.put(StatusesTable.Columns.TRUNCATED, status.isTruncated()); // TODO:
        v.put(StatusesTable.Columns.IN_REPLY_TO_STATUS_ID,
                status.getInReplyToStatusId());
        v.put(StatusesTable.Columns.IN_REPLY_TO_USER_ID,
                status.getInReplyToUserId());
        // v.put(StatusTable.Columns.IN_REPLY_TO_SCREEN_NAME,
        // status.getInReplyToScreenName());
        // v.put(IS_REPLY, status.isReply());
        v.put(StatusesTable.Columns.CREATED_AT,
                DB_DATE_FORMATTER.format(status.getCreatedAt()));
        v.put(StatusesTable.Columns.SOURCE, status.getSource());
        // v.put(StatusTable.Columns.IS_UNREAD, status.isUnRead());

        // final User user = status.getUser();
        // if (user != null) {
        // v.put(UserTable.Columns.USER_ID, user.getId());
        // v.put(UserTable.Columns.SCREEN_NAME, user.getScreenName());
        // v.put(UserTable.Columns.PROFILE_IMAGE_URL,
        // user.getProfileImageUrl());
        // }
        final Photo photo = status.getPhoto();
        /*
         * if (photo != null) { v.put(StatusTable.Columns.PIC_THUMB,
         * photo.getThumburl()); v.put(StatusTable.Columns.PIC_MID,
         * photo.getImageurl()); v.put(StatusTable.Columns.PIC_ORIG,
         * photo.getLargeurl()); }
         */

        return v;
    }

    public String getMaxStatusId(String statusOwner, int statusType, String authorId) {
        Cursor c = null;
        String maxStatusId = "";
        try {
            c = mSqlTemplate.getDb(false).query(
                    FanContent.StatusesView.VIEW_NAME,
                    new String[] { FanContent.StatusesView.Columns.STATUS_ID },
                    FanContent.StatusesView.Columns.OWNER_ID + " ='"
                            + statusOwner + "' AND "
                            + FanContent.StatusesView.Columns.AUTHOR_ID + " ='"
                            + authorId+"' AND "
                            + FanContent.StatusesView.Columns.TYPE + " = '"
                            + statusType + "'", null, null, null,
                    FanContent.StatusesView.Columns.CREATED_AT + " DESC");
            if (c.getCount() > 0) {
                c.moveToLast();
                maxStatusId = c.getString(0);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        return maxStatusId;
    }
    
    private long insertOneStatusIntoStatusesTable(Status status){
    	final ContentValues v = new ContentValues();
        v.put(FanContent.StatusesTable.Columns.STATUS_ID, status.getStatusId());
        v.put(FanContent.StatusesTable.Columns.TEXT, status.getText());
        v.put(FanContent.StatusesTable.Columns.FAVORITED, status.isFavorited() + "");
        v.put(FanContent.StatusesTable.Columns.TRUNCATED, status.isTruncated());
        v.put(FanContent.StatusesTable.Columns.IN_REPLY_TO_STATUS_ID,
                status.getInReplyToStatusId());
        v.put(FanContent.StatusesTable.Columns.IN_REPLY_TO_USER_ID,
                status.getInReplyToUserId());
        v.put(FanContent.StatusesTable.Columns.CREATED_AT,
                DB_DATE_FORMATTER.format(status.getCreatedAt()));
        v.put(FanContent.StatusesTable.Columns.SOURCE, status.getSource());

        final User author = status.getAuthor();
        if (author != null) {
        	v.put(FanContent.StatusesTable.Columns.AUTHOR_ID, author.getId());
        }
        
        final Photo photo = status.getPhoto();
        if (photo != null) { 
        	v.put(FanContent.StatusesTable.Columns.PIC_THUMB, photo.getThumburl()); 
        	v.put(FanContent.StatusesTable.Columns.PIC_MID, photo.getImageurl()); 
        	v.put(FanContent.StatusesTable.Columns.PIC_ORIG, photo.getLargeurl()); }
        
        //FIXME: insertWithOnConflict 是 Level 8 的函数，为了低版本兼容性，这里需要修改。
    	//TODO：这里是可能重复的，必须进行判断
    	return mSqlTemplate.getDb(true).insertWithOnConflict(FanContent.StatusesTable.TABLE_NAME, "", v, SQLiteDatabase.CONFLICT_IGNORE);
    }
    
    private long insertOneStatusIntoStatuesPropertyTable(Status status, int sequenceFlag){
    	final ContentValues v = new ContentValues();
    	v.put(FanContent.StatusesPropertyTable.Columns.STATUS_ID, status.getStatusId());
    	v.put(FanContent.StatusesPropertyTable.Columns.OWNER_ID, status.getOwner().getId());
    	v.put(FanContent.StatusesPropertyTable.Columns.TYPE, status.getType());
    	v.put(FanContent.StatusesPropertyTable.Columns.SEQUENCE_FLAG, sequenceFlag);

    	long result1 = 0;
    	long result2 = 0;
    	//FIXME: insertWithOnConflict 是 Level 8 的函数，为了低版本兼容性，这里需要修改。
    	result1 = mSqlTemplate.getDb(true).insertWithOnConflict(FanContent.StatusesPropertyTable.TABLE_NAME, "", v, SQLiteDatabase.CONFLICT_IGNORE);
    	
        final Photo photo = status.getPhoto();
        if (photo != null) {
        	v.put(FanContent.StatusesPropertyTable.Columns.TYPE, Status.TYPE_PHOTO);
        	//FIXME: insertWithOnConflict 是 Level 8 的函数，为了低版本兼容性，这里需要修改。
        	//TODO：这里是可能重复的，必须进行判断
        	result2 = mSqlTemplate.getDb(true).insertWithOnConflict(FanContent.StatusesPropertyTable.TABLE_NAME, "", v, SQLiteDatabase.CONFLICT_IGNORE);
        }
        
        return result1+result2;
    }
    
    public boolean insertOneStatus(Status status, int sequenceFlag){
    	insertOneStatusIntoStatusesTable(status);
    	insertOneStatusIntoStatuesPropertyTable(status, sequenceFlag);
    	//TODO: 是否需要处理User信息？
    	return true;
    }
    
    public int getNewSequenceFlag(String ownerId, int statusType){
    	//FIXME: 非常低效的方法，需要优化
    	String SQL = "SELECT max( " + FanContent.StatusesPropertyTable.Columns.SEQUENCE_FLAG + " ) "
    	            + " FROM " + FanContent.StatusesPropertyTable.TABLE_NAME;
    	Cursor c = mSqlTemplate.getDb(false).rawQuery(SQL, new String[]{});
    	
    	int result = 0;
    	if (c != null && c.moveToFirst()){
    		result = c.getInt(0);
    	}
    	return result + 1;
    }
    
    public int getCurrentSequenceFlag(String ownerId, int statusType){
    	//FIXME: 非常低效的方法，需要优化
    	String SQL = "SELECT max( " + FanContent.StatusesPropertyTable.Columns.SEQUENCE_FLAG + " ) "
    	            + " FROM " + FanContent.StatusesPropertyTable.TABLE_NAME;
    	Cursor c = mSqlTemplate.getDb(false).rawQuery(SQL, new String[]{});
    	
    	int result = 0;
    	if (c != null && c.moveToFirst()){
    		result = c.getInt(0);
    	}
    	return result;
    }
    
    public int getPrevSequenceFlag(String ownerId, int statusType){
    	//FIXME: 非常低效的方法，需要优化
    	//取第二大的seq
    	String SQL = "SELECT max( " + FanContent.StatusesPropertyTable.Columns.SEQUENCE_FLAG + " ) "
    	            + " FROM " + FanContent.StatusesPropertyTable.TABLE_NAME + " "
    	            + " WHERE " + FanContent.StatusesPropertyTable.Columns.SEQUENCE_FLAG + " < "
    	            + " (SELECT max( "+ FanContent.StatusesPropertyTable.Columns.SEQUENCE_FLAG + " )"
    	            + "    FROM " + FanContent.StatusesPropertyTable.TABLE_NAME + ") ";
    	Cursor c = mSqlTemplate.getDb(false).rawQuery(SQL, new String[]{});
    	
    	int result = 0;
    	if (c != null && c.moveToFirst()){
    		result = c.getInt(0);
    	}
    	return result;
    }
    
    public List<Status> getOneGroupStatus(String ownerId, String authorId, int type) {
        List<Status> statuses = new ArrayList<Status>();
        //在mSqlTemplate查
        return statuses;
    }

    private static final RowMapper<Status> mRowMapper = new RowMapper<Status>() {

        @Override
        public Status mapRow(Cursor cursor, int rowNum) {
            Photo photo = new Photo();
            /*
             * photo.setImageurl(cursor.getString(cursor
             * .getColumnIndex(StatusTable.Columns.PIC_MID)));
             * photo.setLargeurl(cursor.getString(cursor
             * .getColumnIndex(StatusTable.Columns.PIC_ORIG)));
             * photo.setThumburl(cursor.getString(cursor
             * .getColumnIndex(StatusTable.Columns.PIC_THUMB)));
             */
            User user = new User();
            user.setScreenName(cursor.getString(cursor
                    .getColumnIndex(UserTable.Columns.SCREEN_NAME)));
            user.setId(cursor.getString(cursor
                    .getColumnIndex(UserTable.Columns.USER_ID)));
            user.setProfileImageUrl(cursor.getString(cursor
                    .getColumnIndex(UserTable.Columns.PROFILE_IMAGE_URL)));

            Status status = new Status();
            status.setPhoto(photo);
            // status.setUser(user);
            // status.setOwnerId(cursor.getString(cursor
            // .getColumnIndex(StatusesPropertyTable.Columns.OWNER_ID)));
            // TODO: 将数据库中的statusType改成Int类型
            status.setType(cursor.getInt(cursor
                    .getColumnIndex(StatusesPropertyTable.Columns.TYPE)));
            // status.setId(cursor.getString(cursor
            // .getColumnIndex(StatusesTable.Columns.ID)));
            status.setCreatedAt(DateTimeHelper.parseDateTimeFromSqlite(cursor
                    .getString(cursor
                            .getColumnIndex(StatusesTable.Columns.CREATED_AT))));
            // TODO: 更改favorite 在数据库类型为boolean后改为 " != 0 "
            status.setFavorited(cursor.getString(
                    cursor.getColumnIndex(StatusesTable.Columns.FAVORITED))
                    .equals("true"));
            status.setText(cursor.getString(cursor
                    .getColumnIndex(StatusesTable.Columns.TEXT)));
            status.setSource(cursor.getString(cursor
                    .getColumnIndex(StatusesTable.Columns.SOURCE)));
            // status.setInReplyToScreenName(cursor.getString(cursor
            // .getColumnIndex(StatusTable.IN_REPLY_TO_SCREEN_NAME)));
            status.setInReplyToStatusId(cursor.getString(cursor
                    .getColumnIndex(StatusesTable.Columns.IN_REPLY_TO_STATUS_ID)));
            status.setInReplyToUserId(cursor.getString(cursor
                    .getColumnIndex(StatusesTable.Columns.IN_REPLY_TO_USER_ID)));
            status.setTruncated(cursor.getInt(cursor
                    .getColumnIndex(StatusesTable.Columns.TRUNCATED)) != 0);
            // status.setUnRead(cursor.getInt(cursor
            // .getColumnIndex(StatusTable.Columns.IS_UNREAD)) != 0);
            return status;
        }

    };

}
