package com.ch_linghu.fanfoudroid.dao;

import java.util.ArrayList;
import java.util.List;

import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.data2.Status;
import com.ch_linghu.fanfoudroid.data2.User;
import com.ch_linghu.fanfoudroid.db2.FanContent;
import com.ch_linghu.fanfoudroid.db2.FanDatabase;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database Helper
 * 
 * @see SQLiteDatabase
 */
public class SQLiteTemplate {
    /**
     * Default Primary key
     */
    protected String mPrimaryKey = "_id";

    /**
     * SQLiteDatabase Open Helper
     */
    protected SQLiteOpenHelper mDatabaseOpenHelper;

    /**
     * Construct
     * 
     * @param databaseOpenHelper
     */
    public SQLiteTemplate(SQLiteOpenHelper databaseOpenHelper) {
        mDatabaseOpenHelper = databaseOpenHelper;
    }

    /**
     * Construct
     * 
     * @param databaseOpenHelper
     * @param primaryKey
     */
    public SQLiteTemplate(SQLiteOpenHelper databaseOpenHelper, String primaryKey) {
        this(databaseOpenHelper);
        setPrimaryKey(primaryKey);
    }

    /**
     * 根据某一个字段和值删除一行数据, 如 name="jack"
     * 
     * @param table
     * @param field
     * @param value
     * @return
     */
    public int deleteByField(String table, String field, String value) {
        return getDb(true).delete(table, field + "=?", new String[] { value });
    }

    /**
     * 根据主键删除一行数据
     * 
     * @param table
     * @param id
     * @return
     */
    public int deleteById(String table, String id) {
        return deleteByField(table, mPrimaryKey, id);
    }

    /**
     * 根据主键更新一行数据
     * 
     * @param table
     * @param id
     * @param values
     * @return
     */
    public int updateById(String table, String id, ContentValues values) {
        return getDb(true).update(table, values, mPrimaryKey + "=?",
                new String[] { id });
    }

    /**
     * 根据主键查看某条数据是否存在
     * 
     * @param table
     * @param id
     * @return
     */
    public boolean isExistsById(String table, String id) {
        return isExistsByField(table, mPrimaryKey, id);
    }

    /**
     * 根据某字段/值查看某条数据是否存在
     * 
     * @param status
     * @return
     */
    public boolean isExistsByField(String table, String field, String value) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(table).append(" WHERE ")
                .append(field).append(" =?");

        return isExistsBySQL(sql.toString(), new String[] { value });
    }

    /**
     * 使用SQL语句查看某条数据是否存在
     * 
     * @param sql
     * @param selectionArgs
     * @return
     */
    public boolean isExistsBySQL(String sql, String[] selectionArgs) {
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
    public <T> T queryForObject(RowMapper<T> rowMapper, String table,
            String[] columns, String selection, String[] selectionArgs,
            String groupBy, String having, String orderBy, String limit) {
        T object = null;

        final Cursor c = getDb(false).query(table, columns, selection,
                selectionArgs, groupBy, having, orderBy, limit);
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
    public <T> List<T> queryForList(RowMapper<T> rowMapper, String table,
            String[] columns, String selection, String[] selectionArgs,
            String groupBy, String having, String orderBy, String limit) {
        List<T> list = new ArrayList<T>();

        final Cursor c = getDb(false).query(table, columns, selection,
                selectionArgs, groupBy, having, orderBy, limit);
        try {
            while (c.moveToNext()) {
                list.add(rowMapper.mapRow(c, 1));
            }
        } finally {
            c.close();
        }
        return list;
    }

    /**
     * Get Primary Key
     * 
     * @return
     */
    public String getPrimaryKey() {
        return mPrimaryKey;
    }

    /**
     * Set Primary Key
     * 
     * @param primaryKey
     */
    public void setPrimaryKey(String primaryKey) {
        this.mPrimaryKey = primaryKey;
    }

    /**
     * Get Database Connection
     * 
     * @param writeable
     * @return
     * @see SQLiteOpenHelper#getWritableDatabase();
     * @see SQLiteOpenHelper#getReadableDatabase();
     */
    public SQLiteDatabase getDb(boolean writeable) {
        if (writeable) {
            return mDatabaseOpenHelper.getWritableDatabase();
        } else {
            return mDatabaseOpenHelper.getReadableDatabase();
        }
    }

    /**
     * Some as Spring JDBC RowMapper
     * 
     * @see org.springframework.jdbc.core.RowMapper
     * @see com.ch_linghu.fanfoudroid.db.dao.SqliteTemplate
     * @param <T>
     */
    public interface RowMapper<T> {
        public T mapRow(Cursor cursor, int rowNum);
    }

    /**
     * 测试用
     * @param authorId
     * @return
     */
    public String getMaxStatusIdByAuthorInXXStatuses(String authorId) {
        Cursor c = null;
        String maxStatusId = "";
        try {
            c = getDb(false).query(
                    FanContent.StatusesView.VIEW_NAME,
                    new String[] { FanContent.StatusesView.Columns.STATUS_ID },
                    FanContent.StatusesView.Columns.OWNER_ID + " ='"
                            + TwitterApplication.getMyselfId() + "' AND "
                            + FanContent.StatusesView.Columns.AUTHOR_ID + " ='"
                            + authorId+"' AND "
                            + FanContent.StatusesView.Columns.TYPE + " = '"
                            + Status.TYPE_XXSTATUSES + "'", null, null, null,
                    FanContent.StatusesView.Columns.CREATED_AT);
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
    
    public boolean insertOneStatus(Status status) {
        
        return false;
        
    }
    
    public boolean insertOneStatusProperty(Status status) {
        return false;
    }
    
    public boolean insertOrUpdateUser(User user) {
        return false;
    }
}
