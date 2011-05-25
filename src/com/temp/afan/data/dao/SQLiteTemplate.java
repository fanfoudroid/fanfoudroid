package com.temp.afan.data.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database Helper
 * 
 * @see SQLiteDatabase
 */
public class SQLiteTemplate {
    protected String mPrimaryKey = "_id";
    protected final SQLiteOpenHelper mDatabaseOpenHelper;

    public SQLiteTemplate(SQLiteOpenHelper databaseOpenHelper) {
        mDatabaseOpenHelper = databaseOpenHelper;
    }

    public SQLiteTemplate(SQLiteOpenHelper databaseOpenHelper, String primaryKey) {
        this(databaseOpenHelper);
        setPrimaryKey(primaryKey);
    }

    public int deleteByField(String table, String field, String value) {
        return getDb(true).delete(table, field + "=?", new String[] { value });
    }

    public int deleteById(String table, String id) {
        return deleteByField(table, mPrimaryKey, id);
    }

    public int updateById(String table, String id, ContentValues values) {
        return getDb(true).update(table, values, mPrimaryKey + "=?",
                new String[] { id });
    }
    
    public boolean isExistsById(String table, String id) {
        return isExistsByField(table, mPrimaryKey, id);
    }

    /**
     * Check if exists
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

        final Cursor c = getDb(false).query(table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
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

        final Cursor c = getDb(false).query(table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
        try {
            while (c.moveToNext()) {
                list.add(rowMapper.mapRow(c, 1));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public String getPrimaryKey() {
        return mPrimaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.mPrimaryKey = primaryKey;
    }
    
    public  SQLiteDatabase getDb(boolean writeable) {
        if (writeable) {
            return mDatabaseOpenHelper.getWritableDatabase();
        } else {
            return mDatabaseOpenHelper.getReadableDatabase();
        }
    }

}
