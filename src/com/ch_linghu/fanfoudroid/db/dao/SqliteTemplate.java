package com.ch_linghu.fanfoudroid.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ch_linghu.fanfoudroid.db.TwitterDatabase;

/**
 * Wrapper of SQLiteDatabase
 * Some as Spring JdbcTemplate
 * 
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see com.ch_linghu.fanfoudroid.db.dao.RowMapper
 */
public class SqliteTemplate {
    private static final String TAG = "SqliteTemplate";
    
    /**
     * Construct
     * 
     * @param tableName table name
     */
    public SqliteTemplate() {
    }
    
    /**
     * Insert a Row
     * 
     * @param table
     * @param nullColumnHack
     * @param values
     * @return the row ID of the newly inserted row, or -1 if an error occurred 
     * 
     * @see SQLiteDatabase#insert(String table, String nullColumnHack, ContentValues values)
     */
    public long insert(Query query)
    {
        query.setDb(TwitterDatabase.getDb(true));
        long id = query.insert();
        Log.v(TAG, ((id != -1) ? "[Success] " : "[Fail] ") + "Insert " 
                + query.getContentValues().toString());
        return id;
    }
    
    /**
     * Delete a row
     * 
     * @param query
     * @return succeed or fail
     */
    public boolean delete(Query query)
    {
        query.setDb(TwitterDatabase.getDb(true));
        int rows = query.delete();
        Log.v(TAG, ((rows > 0) ? "[Success] " : "[Fail] ") + "Delete " 
                + query.toString());
        return ( rows > 0 );
    }
    
    /**
     * Query For Object
     * 
     * @param <T>
     * @param query builder
     * @param rowMapper
     * @return object 
     * @throws DAOException 
     */
    public <T> T queryForObject(Query query, RowMapper<T> rowMapper) {
        query.setDb(TwitterDatabase.getDb(false));
        Cursor cursor = query.select();
        cursor.moveToFirst();
        
        T object = rowMapper.mapRow(cursor, cursor.getCount());
        cursor.close();
        return object;
    }
    
    /**
     * Query for list
     * 
     * @param <T>
     * @param query builder
     * @param rowMapper
     * @return list of object
     */
    public <T> List<T> queryForList(Query query, RowMapper<T> rowMapper) {
        List<T> list = new ArrayList<T>();

        Cursor cursor = queryForCursor(query);
        while (cursor.moveToNext()) {
            list.add(rowMapper.mapRow(cursor, 1));
        }
        cursor.close();
        return list;
    }
    
    /**
     * Query for cursor 
     * 
     * @param query builder
     * @return a cursor
     */
    public Cursor queryForCursor(Query query) {
        query.setDb(TwitterDatabase.getDb(false));
        return query.select();
    }
    
    /**
     * Upload a row
     * 
     * @param query
     * @return the number of rows affected, or -1 if an error occurred
     */
    public int upload(Query query) {
        query.setDb(TwitterDatabase.getDb(true));
        return query.update();
    }

}