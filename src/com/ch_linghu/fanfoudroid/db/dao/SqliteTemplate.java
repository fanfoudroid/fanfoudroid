package com.ch_linghu.fanfoudroid.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ch_linghu.fanfoudroid.db.TwitterDatabase;

/**
 * Some as Spring JdbcTemplate
 * 
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see com.ch_linghu.fanfoudroid.db.dao.RowMapper
 */
public class SqliteTemplate {
    private static final String TAG = "SqliteTemplate";
    private String mTable;
    
    /**
     * Construct
     * 
     * @param tableName table name
     */
    public SqliteTemplate(String tableName) {
        mTable = tableName;
    }
    
    /**
     * Insert a row
     * 
     * @param values
     * @return the row ID of the newly inserted row, or -1 if an error occurred 
     */
    public long insert(ContentValues values) {
        SQLiteDatabase Db = TwitterDatabase.getDb(true);
        long id = Db.insert(mTable, null, values);
        Log.i(TAG, ((id != -1) ? "[Success] " : "[Fail] ") + "Insert " + values.toString());
        return id;
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
    public <T> T queryForObject(Query query, RowMapper<T> rowMapper) 
    
    {
        SQLiteDatabase db = TwitterDatabase.getDb(false);
        Cursor cursor = query.from(mTable, null).query(db);
        
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
        SQLiteDatabase db = TwitterDatabase.getDb(false);
        return query.from(mTable, null).query(db);
    }

}