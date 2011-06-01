package com.temp.afan.data.dao;

import com.ch_linghu.fanfoudroid.db.StatusTable;
import com.ch_linghu.fanfoudroid.db.TwitterDatabase;

public class StatusDAO {
    private static final String TAG = "StatusDAO";

    private static String mTable = StatusTable.TABLE_NAME;
    private SQLiteTemplate mSqlTemplate;

    public StatusDAO() {
        mSqlTemplate = new SQLiteTemplate(TwitterDatabase.getInstance()
                .getSQLiteOpenHelper(), StatusTable._ID);
    }

   

}