package com.ch_linghu.fanfoudroid.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Database Helper 
 * 
 * @see SQLiteDatabase
 */
public class SqliteTemplate {
    protected String mPrimaryKey = "_id";
    protected SQLiteDatabase mDb;
    
    public SqliteTemplate() {}
    
    public SqliteTemplate(SQLiteDatabase db) {
        setDb(db);
    }
    
    public SqliteTemplate(SQLiteDatabase db, String primaryKey) {
        this(db);
        setPrimaryKey(primaryKey);
    }
    
    public int deleteByField(String table, String field, String value) {
        return mDb.delete(table, field + "=?", new String[]{value});
    }
    
    public int deleteById(String table, String id) {
        return deleteByField(table, mPrimaryKey, id);
    }
    
    public int updateById(String table, String id, ContentValues values) {
        return mDb.update(table, values, mPrimaryKey + "=?", new String[]{id});
    }
    
    /**
     * Check if exists
     * 
     * @param status
     * @return
     */
    public boolean isExistsByField(String table, String field, String value) {
        StringBuilder sql = new StringBuilder();
        sql.append("COUNT(*) FROM ").append(table)
           .append(" WHERE ").append(field).append(" =?");
        Cursor cursor = mDb.rawQuery(sql.toString(), new String[]{ value });
        boolean result = (cursor != null && cursor.getCount() > 0);
        cursor.close();
        return result;
    }
    
    public boolean isExistsById(String table, String id) {
        return isExistsByField(table, mPrimaryKey, id);
    }
    
    /**
     * Query for cursor
     * 
     * @param <T>
     * @param rowMapper
     * @return a cursor
     * 
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String, String)
     */
    public <T> T queryForObject(RowMapper<T> rowMapper, String table, String[] columns,
            String selection, String[] selectionArgs, String groupBy,
            String having, String orderBy, String limit) {
        
        Cursor cursor = mDb.query(table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
        cursor.moveToFirst();

        T object = rowMapper.mapRow(cursor, cursor.getCount());
        cursor.close();
        return object;
    }
    
    /**
     * Query for list
     * 
     * @param <T>
     * @param rowMapper
     * @return list of object
     * 
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String, String)
     */
    public <T> List<T> queryForList(RowMapper<T> rowMapper, String table, String[] columns,
            String selection, String[] selectionArgs, String groupBy,
            String having, String orderBy, String limit) {
        List<T> list = new ArrayList<T>();

        Cursor cursor = mDb.query(table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
        while (cursor.moveToNext()) {
            list.add(rowMapper.mapRow(cursor, 1));
        }
        cursor.close();
        return list;
    }

    public String getPrimaryKey() {
        return mPrimaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.mPrimaryKey = primaryKey;
    }

    public SQLiteDatabase getDb() {
        return mDb;
    }

    public void setDb(SQLiteDatabase db) {
        this.mDb = db;
    }
    
}
