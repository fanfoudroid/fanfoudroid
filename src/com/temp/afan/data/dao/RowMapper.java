package com.temp.afan.data.dao;

import android.database.Cursor;

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
