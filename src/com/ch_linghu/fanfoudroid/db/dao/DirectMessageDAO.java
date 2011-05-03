package com.ch_linghu.fanfoudroid.db.dao;

import com.ch_linghu.fanfoudroid.db.MessageTable;

// TODO 
public class DirectMessageDAO {
    private static final String TAG = "DirectMessageDAO";
    
    private SqliteTemplate sqliteTemplate;
    
    public DirectMessageDAO() {
        sqliteTemplate = new SqliteTemplate(MessageTable.TABLE_NAME);
    }

}