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
import com.ch_linghu.fanfoudroid.data2.Photo;
import com.ch_linghu.fanfoudroid.data2.Status;
import com.ch_linghu.fanfoudroid.data2.User;
import com.ch_linghu.fanfoudroid.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.db2.FanContent;
import com.ch_linghu.fanfoudroid.db2.FanContent.StatusesPropertyTable;
import com.ch_linghu.fanfoudroid.db2.FanDatabase;
import com.ch_linghu.fanfoudroid.util.DateTimeHelper;
import com.ch_linghu.fanfoudroid.db2.FanContent.*;

//TODO: 目前仅实现旧版兼容接口，不考虑新特性引入
public class StatusDAO {
    private static final String TAG = "StatusDAO";

    private FanDatabase mDb;

    public final static DateFormat DB_DATE_FORMATTER = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
    
    public StatusDAO(Context context) {
        mDb = FanDatabase.getInstance(context);
    }

    /**
     * 判断指定ID的消息是否存在于StatusTable
     * 
     * FIXME: 取消使用Query
     * 
     * @param status
     * @return
     */
    public boolean isExistsInStatus(String statusId) {
    	int count = 0;
    	Cursor c = mDb.getDb(false).query(FanContent.StatusesTable.TABLE_NAME, 
    			new String[] {FanContent.StatusesTable.Columns.ID}, 
    			FanContent.StatusesTable.Columns.STATUS_ID + "=" + statusId, 
    			null, null, null, null);
    	count = c.getCount();
    	c.close();
    	
        return count > 0;
    }

    /**
     * 判断指定ID的消息是否存在于StatusPropertyTable
     * 
     * FIXME: 取消使用Query
     * 
     * @param status
     * @return
     */
    public boolean isExistsInProperty(String statusId) {
    	int count = 0;
    	Cursor c = mDb.getDb(false).query(FanContent.StatusesPropertyTable.TABLE_NAME, 
    			new String[] {FanContent.StatusesPropertyTable.Columns.ID}, 
    			FanContent.StatusesPropertyTable.Columns.STATUS_ID + "=" + statusId, 
    			null, null, null, null);
    	count = c.getCount();
    	c.close();
    	
        return count > 0;
    }

    /**
     * 判断指定条件的消息是否存在于StatusPropertyTable
     * 
     * FIXME: 取消使用Query
     * 
     * @param status
     * @return
     */
    public boolean isExistsInProperty(String statusId, String ownerId, int statusType) {
    	int count = 0;
    	Cursor c = mDb.getDb(false).query(FanContent.StatusesPropertyTable.TABLE_NAME, 
    			new String[] {FanContent.StatusesPropertyTable.Columns.ID}, 
    			FanContent.StatusesPropertyTable.Columns.STATUS_ID + "='" + statusId + "'"
    			+ " AND " + FanContent.StatusesPropertyTable.Columns.OWNER_ID + "='" + ownerId + "'"
    			+ " AND " + FanContent.StatusesPropertyTable.Columns.TYPE + "=" + statusType, 
    			null, null, null, null);
    	count = c.getCount();
    	c.close();
    	
        return count > 0;
    }

    //辅助函数
    private long insertSingleStatusIntoStatusesTable(Status status){
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
        
        if (!isExistsInStatus(status.getStatusId())){
        	return mDb.getDb(true).insert(FanContent.StatusesTable.TABLE_NAME, "", v);
        }else{
        	Log.w(TAG, "[insertSingleStatusIntoStatusesTable]" + status.getStatusId()+" is already exist.");
        	return 0;
        }
    }
    
    private long insertSingleStatusIntoStatuesPropertyTable(Status status, int sequenceFlag){
    	final ContentValues v = new ContentValues();
    	v.put(FanContent.StatusesPropertyTable.Columns.STATUS_ID, status.getStatusId());
    	v.put(FanContent.StatusesPropertyTable.Columns.OWNER_ID, status.getOwner().getId());
    	v.put(FanContent.StatusesPropertyTable.Columns.TYPE, status.getType());
    	v.put(FanContent.StatusesPropertyTable.Columns.SEQUENCE_FLAG, sequenceFlag);

    	//long result1 = 0;
    	//long result2 = 0;
    	if (!isExistsInProperty(status.getStatusId(), status.getOwner().getId(), status.getType())){
    		return mDb.getDb(true).insert(FanContent.StatusesPropertyTable.TABLE_NAME, "", v);
    	}else{
        	Log.w(TAG, "[insertSingleStatusIntoStatuesPropertyTable]" + status.getStatusId()+" is already exist.");
        	return 0;    		
    	}
    	
        //final Photo photo = status.getPhoto();
        //if (photo != null) {
        //	v.put(FanContent.StatusesPropertyTable.Columns.TYPE, Status.TYPE_PHOTO);
        //	//FIXME: insertWithOnConflict 是 Level 8 的函数，为了低版本兼容性，这里需要修改。
        //	//TODO：这里是可能重复的，必须进行判断
        //	result2 = mSqlTemplate.getDb(true).insertWithOnConflict(FanContent.StatusesPropertyTable.TABLE_NAME, "", v, SQLiteDatabase.CONFLICT_IGNORE);
        //}
        //
        //return result1+result2;
    }
    
    /**
     * 插入单一记录
     * 
     * @param statusOwner, statusType, authorId
     * @return
     */
    public boolean insertSingleStatus(Status status){
    	insertSingleStatusIntoStatusesTable(status);
    	insertSingleStatusIntoStatuesPropertyTable(status, 1);
    	//FIXME: 是否需要处理User信息？
    	return true;
    }

    public boolean insertSingleStatus(Status status, int sequenceFlag){
    	insertSingleStatusIntoStatusesTable(status);
    	insertSingleStatusIntoStatuesPropertyTable(status, sequenceFlag);
    	//FIXME: 是否需要处理User信息？
    	return true;
    }
    
    /**
     * 删除单一记录
     * 
     * @param statusOwner, statusType, authorId
     * @return
     */
    public boolean deleteSingleStatus(String statusId, String ownerId, int statusType){
    	mDb.getDb(true).delete(FanContent.StatusesPropertyTable.TABLE_NAME, 
    			FanContent.StatusesPropertyTable.Columns.STATUS_ID + "='" + statusId + "'"
    			+ " AND " + FanContent.StatusesPropertyTable.Columns.OWNER_ID + "='" + ownerId + "'"
    			+ " AND " + FanContent.StatusesPropertyTable.Columns.TYPE + "=" + statusType, 
    			null);
    	
    	//如果属性表中的记录完全被删除，则同时删除主表中的记录
    	if (!isExistsInProperty(statusId)){
    		mDb.getDb(true).delete(FanContent.StatusesTable.TABLE_NAME, 
    				FanContent.StatusesTable.Columns.STATUS_ID + "='" + statusId + "'", 
    				null);
    	}
    	
    	return true;
    }
    
    /**
     * 设置某条消息的收藏状态
     * 
     * @param statusOwner, statusType, authorId
     * @return
     */
    public boolean setFavorited(String statusId, boolean isFavorited){
    	final ContentValues v = new ContentValues();
    	v.put(FanContent.StatusesTable.Columns.FAVORITED, isFavorited);
    	mDb.getDb(true).update(FanContent.StatusesTable.TABLE_NAME, 
    			v, FanContent.StatusesTable.Columns.STATUS_ID + "='" + statusId + "'", null);
    	return true;
    }

    /**
     * 获得指定用户指定类型的信息
     * 
     * @param statusOwner, statusType, authorId
     * @return
     */
    public Cursor fetchStatus(String ownerId, int statusType){
    	return mDb.getDb(false).query(FanContent.StatusesView.VIEW_NAME, 
    			FanContent.StatusesView.getColumns(), 
    			FanContent.StatusesView.Columns.OWNER_ID + "='" + ownerId + "'"
    			+ " AND " + FanContent.StatusesView.Columns.TYPE + "=" + statusType, 
    			null, null, null, 
    			FanContent.StatusesView.Columns.CREATED_AT + " DESC");
    }

    /**
     * 获得表中的最大ID
     * 
     * @param statusOwner, statusType, authorId
     * @return
     */
    public String getMaxStatusId(String ownerId, int statusType, String authorId) {
        Cursor c = null;
        String maxStatusId = "";
        try {
            c = mDb.getDb(false).query(
                    FanContent.StatusesView.VIEW_NAME,
                    new String[] { FanContent.StatusesView.Columns.STATUS_ID },
                    FanContent.StatusesView.Columns.OWNER_ID + " ='"
                            + ownerId + "' AND "
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
    
    
//    public int getNewSequenceFlag(String ownerId, int statusType){
//    	//FIXME: 非常低效的方法，需要优化
//    	String SQL = "SELECT max( " + FanContent.StatusesPropertyTable.Columns.SEQUENCE_FLAG + " ) "
//    	            + " FROM " + FanContent.StatusesPropertyTable.TABLE_NAME;
//    	Cursor c = mSqlTemplate.getDb(false).rawQuery(SQL, new String[]{});
//    	
//    	int result = 0;
//    	if (c != null && c.moveToFirst()){
//    		result = c.getInt(0);
//    	}
//    	return result + 1;
//    }
//    
//    public int getCurrentSequenceFlag(String ownerId, int statusType){
//    	//FIXME: 非常低效的方法，需要优化
//    	String SQL = "SELECT max( " + FanContent.StatusesPropertyTable.Columns.SEQUENCE_FLAG + " ) "
//    	            + " FROM " + FanContent.StatusesPropertyTable.TABLE_NAME;
//    	Cursor c = mSqlTemplate.getDb(false).rawQuery(SQL, new String[]{});
//    	
//    	int result = 0;
//    	if (c != null && c.moveToFirst()){
//    		result = c.getInt(0);
//    	}
//    	return result;
//    }
//    
//    public int getPrevSequenceFlag(String ownerId, int statusType){
//    	//FIXME: 非常低效的方法，需要优化
//    	//取第二大的seq
//    	String SQL = "SELECT max( " + FanContent.StatusesPropertyTable.Columns.SEQUENCE_FLAG + " ) "
//    	            + " FROM " + FanContent.StatusesPropertyTable.TABLE_NAME + " "
//    	            + " WHERE " + FanContent.StatusesPropertyTable.Columns.SEQUENCE_FLAG + " < "
//    	            + " (SELECT max( "+ FanContent.StatusesPropertyTable.Columns.SEQUENCE_FLAG + " )"
//    	            + "    FROM " + FanContent.StatusesPropertyTable.TABLE_NAME + ") ";
//    	Cursor c = mSqlTemplate.getDb(false).rawQuery(SQL, new String[]{});
//    	
//    	int result = 0;
//    	if (c != null && c.moveToFirst()){
//    		result = c.getInt(0);
//    	}
//    	return result;
//    }
}
