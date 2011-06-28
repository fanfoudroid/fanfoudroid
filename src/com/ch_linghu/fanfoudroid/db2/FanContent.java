package com.ch_linghu.fanfoudroid.db2;

import java.util.zip.CheckedOutputStream;

import android.R.color;

public abstract class FanContent {

    /**
     * 消息表 消息表存放消息本身
     * 
     * @author phoenix
     * 
     */
    public static class StatusesTable {
        public static final String TABLE_NAME = "t_statuses";

        public static class Columns {
            public static final String ID = "_id";
            public static final String STATUS_ID = "status_id";
            public static final String AUTHOR_ID = "author_id";
            public static final String TEXT = "text";
            public static final String SOURCE = "source";
            public static final String CREATED_AT = "created_at";
            public static final String TRUNCATED = "truncated";
            public static final String FAVORITED = "favorited";
            public static final String PHOTO_URL = "photo_url";
            public static final String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
            public static final String IN_REPLY_TO_USER_ID = "in_reply_to_user_id";
            public static final String IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";

        }

        public static String getCreateSQL() {
            String createString = TABLE_NAME + "( " + Columns.ID
                    + " INTEGER PRIMARY KEY, " + Columns.STATUS_ID
                    + " TEXT UNIQUE NOT NULL, " + Columns.AUTHOR_ID + " TEXT, "
                    + Columns.TEXT + " TEXT, " + Columns.SOURCE + " TEXT, "
                    + Columns.CREATED_AT + " INT, " + Columns.TRUNCATED
                    + " INT DEFAULT 0, " + Columns.FAVORITED
                    + " INT DEFAULT 0, " + Columns.PHOTO_URL + " TEXT, "
                    + Columns.IN_REPLY_TO_STATUS_ID + " TEXT, "
                    + Columns.IN_REPLY_TO_USER_ID + " TEXT, "
                    + Columns.IN_REPLY_TO_SCREEN_NAME + " TEXT " + ");";

            return "CREATE TABLE " + createString;
        }

        public static String getDropSQL() {
            return "DROP TABLE " + TABLE_NAME;
        }

        public static String[] getIndexColumns() {
            return new String[] { Columns.ID, Columns.STATUS_ID,
                    Columns.AUTHOR_ID, Columns.TEXT, Columns.SOURCE,
                    Columns.CREATED_AT, Columns.TRUNCATED, Columns.FAVORITED,
                    Columns.PHOTO_URL, Columns.IN_REPLY_TO_STATUS_ID,
                    Columns.IN_REPLY_TO_USER_ID,
                    Columns.IN_REPLY_TO_SCREEN_NAME };
        }

        public static String getCreateIndexSQL() {
            String createIndexSQL = "CREATE INDEX " + TABLE_NAME + "_idx ON "
                    + TABLE_NAME + " ( " + getIndexColumns()[1] + " );";
            return createIndexSQL;
        }
    }

    /**
     * 消息属性表 每一条消息所属类别、所有者等信息 消息ID(外键) 所有者(随便看看的所有者为空)
     * 消息类别(随便看看/首页(自己及自己好友)/个人(仅自己)/收藏/照片)
     * 
     * @author phoenix
     * 
     */
    public static class StatusesPropertyTable {
        public static final String TABLE_NAME = "t_statuses_property";

        public static class Columns {
            public static final String ID = "_id";
            public static final String STATUS_ID = "status_id";
            public static final String OWNER_ID = "owner_id";
            public static final String TYPE = "type";
            public static final String SEQUENCE_FLAG = "sequence_flag";
            public static final String LOAD_TIME = "load_time";
        }

        public static String getCreateSQL() {
            String createString = TABLE_NAME + "( " + Columns.ID
                    + " INTEGER PRIMARY KEY, " + Columns.STATUS_ID
                    + " TEXT NOT NULL, " + Columns.OWNER_ID + " TEXT, "
                    + Columns.TYPE + " INT, " + Columns.SEQUENCE_FLAG
                    + " INT, " + Columns.LOAD_TIME
                    + " TIMESTAMP default (DATETIME('now', 'localtime')) "
                    + ");";

            return "CREATE TABLE " + createString;
        }

        public static String getDropSQL() {
            return "DROP TABLE " + TABLE_NAME;
        }

        public static String[] getIndexColumns() {
            return new String[] { Columns.ID, Columns.STATUS_ID,
                    Columns.OWNER_ID, Columns.TYPE, Columns.SEQUENCE_FLAG,
                    Columns.LOAD_TIME };
        }
    }

    /**
     * User表 包括User的基本信息和扩展信息（每次获得最新User信息都update进User表）
     * 每次更新User表时希望能更新LOAD_TIME,记录最后更新时间
     * 
     * @author phoenix
     * 
     */
    public static class UserTable {
        public static final String TABLE_NAME = "t_user";

        public static class Columns {
            public static final String ID = "_id";
            public static final String USER_ID = "user_id";
            public static final String USER_NAME = "user_name";
            public static final String SCREEN_NAME = "screen_name";
            public static final String LOCATION = "location";
            public static final String DESCRIPTION = "description";
            public static final String URL = "url";
            public static final String PROTECTED = "protected";
            public static final String PROFILE_IMAGE_URL = "profile_image_url";
            public static final String FOLLOWERS_COUNT = "followers_count";
            public static final String FRIENDS_COUNT = "friends_count";
            public static final String FAVOURITES_COUNT = "favourites_count";
            public static final String STATUSES_COUNT = "statuses_count";
            public static final String CREATED_AT = "created_at";
            public static final String FOLLOWING = "following";
            public static final String NOTIFICATIONS = "notifications";
            public static final String UTC_OFFSET = "utc_offset";
            public static final String LOAD_TIME = "load_time";
        }

        public static String getCreateSQL() {
            String createString = TABLE_NAME + "( " + Columns.ID
                    + " INTEGER PRIMARY KEY, " + Columns.USER_ID
                    + " TEXT UNIQUE NOT NULL, " + Columns.USER_NAME
                    + " TEXT UNIQUE NOT NULL, " + Columns.SCREEN_NAME
                    + " TEXT, " + Columns.LOCATION + " TEXT, "
                    + Columns.DESCRIPTION + " TEXT, " + Columns.URL + " TEXT, "
                    + Columns.PROTECTED + " INT DEFAULT 0, "
                    + Columns.PROFILE_IMAGE_URL + " TEXT "
                    + Columns.FOLLOWERS_COUNT + " INT, "
                    + Columns.FRIENDS_COUNT + " INT, "
                    + Columns.FAVOURITES_COUNT + " INT, "
                    + Columns.STATUSES_COUNT + " INT, " + Columns.CREATED_AT
                    + " INT, " + Columns.FOLLOWING + " INT DEFAULT 0, "
                    + Columns.NOTIFICATIONS + " INT DEFAULT 0, "
                    + Columns.UTC_OFFSET + " TEXT, " + Columns.LOAD_TIME
                    + " TIMESTAMP default (DATETIME('now', 'localtime')) "
                    + ");";

            return "CREATE TABLE " + createString;
        }

        public static String getDropSQL() {
            return "DROP TABLE " + TABLE_NAME;
        }

        public static String[] getIndexColumns() {
            return new String[] { Columns.ID, Columns.USER_ID,
                    Columns.USER_NAME, Columns.SCREEN_NAME, Columns.LOCATION,
                    Columns.DESCRIPTION, Columns.URL, Columns.PROTECTED,
                    Columns.PROFILE_IMAGE_URL, Columns.FOLLOWERS_COUNT,
                    Columns.FRIENDS_COUNT, Columns.FAVOURITES_COUNT,
                    Columns.STATUSES_COUNT, Columns.CREATED_AT,
                    Columns.FOLLOWING, Columns.NOTIFICATIONS,
                    Columns.UTC_OFFSET, Columns.LOAD_TIME };
        }
    }

    /**
     * 私信表 私信的基本信息
     * 
     * @author phoenix
     * 
     */
    public static class DirectMessageTable {
        public static final String TABLE_NAME = "t_direct_message";

        public static class Columns {
            public static final String ID = "_id";
            public static final String MSG_ID = "msg_id";
            public static final String TEXT = "text";
            public static final String SENDER_ID = "sender_id";
            public static final String RECIPINET_ID = "recipinet_id";
            public static final String CREATED_AT = "created_at";
            public static final String LOAD_TIME = "load_time";
            public static final String SEQUENCE_FLAG = "sequence_flag";
        }

        public static String getCreateSQL() {
            String createString = TABLE_NAME + "( " + Columns.ID
                    + " INTEGER PRIMARY KEY, " + Columns.MSG_ID
                    + " TEXT UNIQUE NOT NULL, " + Columns.TEXT + " TEXT, "
                    + Columns.SENDER_ID + " TEXT, " + Columns.RECIPINET_ID
                    + " TEXT, " + Columns.CREATED_AT + " INT, "
                    + Columns.SEQUENCE_FLAG + " INT, " + Columns.LOAD_TIME
                    + " TIMESTAMP default (DATETIME('now', 'localtime')) "
                    + ");";

            return "CREATE TABLE " + createString;
        }

        public static String getDropSQL() {
            return "DROP TABLE " + TABLE_NAME;
        }

        public static String[] getIndexColumns() {
            return new String[] { Columns.ID, Columns.MSG_ID, Columns.TEXT,
                    Columns.SENDER_ID, Columns.RECIPINET_ID,
                    Columns.CREATED_AT, Columns.SEQUENCE_FLAG,
                    Columns.LOAD_TIME };
        }
    }

    /**
     * Follow关系表 某个特定用户的Follow关系(User1 following User2,
     * 查找关联某人好友只需限定User1或者User2)
     * 
     * @author phoenix
     * 
     */
    public static class FollowRelationshipTable {
        public static final String TABLE_NAME = "t_follow_relationship";

        public static class Columns {
            public static final String USER1_ID = "user1_id";
            public static final String USER2_ID = "user2_id";
            public static final String LOAD_TIME = "load_time";
        }

        public static String getCreateSQL() {
            String createString = TABLE_NAME + "( " + Columns.USER1_ID
                    + " TEXT, " + Columns.USER2_ID + " TEXT, "
                    + Columns.LOAD_TIME
                    + " TIMESTAMP default (DATETIME('now', 'localtime')) "
                    + ");";

            return "CREATE TABLE " + createString;
        }

        public static String getDropSQL() {
            return "DROP TABLE " + TABLE_NAME;
        }

        public static String[] getIndexColumns() {
            return new String[] { Columns.USER1_ID, Columns.USER2_ID,
                    Columns.LOAD_TIME };
        }
    }

    /**
     * 热门话题表 记录每次查询得到的热词
     * 
     * @author phoenix
     * 
     */
    public static class TrendTable {
        public static final String TABLE_NAME = "t_trend";

        public static class Columns {
            public static final String NAME = "name";
            public static final String QUERY = "query";
            public static final String URL = "url";
            public static final String LOAD_TIME = "load_time";
        }

        public static String getCreateSQL() {
            String createString = TABLE_NAME + "( " + Columns.NAME + " TEXT, "
                    + Columns.QUERY + " TEXT, " + Columns.URL + " TEXT, "
                    + Columns.LOAD_TIME
                    + " TIMESTAMP default (DATETIME('now', 'localtime')) "
                    + ");";

            return "CREATE TABLE " + createString;
        }

        public static String getDropSQL() {
            return "DROP TABLE " + TABLE_NAME;
        }

        public static String[] getIndexColumns() {
            return new String[] { Columns.NAME, Columns.QUERY, Columns.URL,
                    Columns.LOAD_TIME };
        }
    }

    /**
     * 保存搜索表 QUERY_ID(这个ID在API删除保存搜索词时使用)
     * 
     * @author phoenix
     * 
     */
    public static class SavedSearchTable {
        public static final String TABLE_NAME = "t_saved_search";

        public static class Columns {
            public static final String QUERY_ID = "query_id";
            public static final String QUERY = "query";
            public static final String NAME = "name";
            public static final String CREATED_AT = "created_at";
            public static final String LOAD_TIME = "load_time";
        }

        public static String getCreateSQL() {
            String createString = TABLE_NAME + "( " + Columns.QUERY_ID
                    + " INT, " + Columns.QUERY + " TEXT, " + Columns.NAME
                    + " TEXT, " + Columns.CREATED_AT + " INT, "
                    + Columns.LOAD_TIME
                    + " TIMESTAMP default (DATETIME('now', 'localtime')) "
                    + ");";

            return "CREATE TABLE " + createString;
        }

        public static String getDropSQL() {
            return "DROP TABLE " + TABLE_NAME;
        }

        public static String[] getIndexColumns() {
            return new String[] { Columns.QUERY_ID, Columns.QUERY,
                    Columns.NAME, Columns.CREATED_AT, Columns.LOAD_TIME };
        }
    }

    /**
     * 关联Statuses表和其属性表
     * 
     * @author phoenix
     * 
     */
    public static class StatusesView {
        public static final String VIEW_NAME = "vw_statuses";

        public static class Columns {
            public static final String STATUS_ID = "status_id";
            public static final String OWNER_ID = "owner_id";
            public static final String AUTHOR_ID = "author_id";
            public static final String TYPE = "type";
            public static final String CREATED_AT = "created_at";
        }

        public static String getCreateSQL() {
            String createString = VIEW_NAME + " AS SELECT "
                    + StatusesPropertyTable.TABLE_NAME + "."
                    + StatusesPropertyTable.Columns.STATUS_ID + ", "
                    + StatusesPropertyTable.TABLE_NAME + "."
                    + StatusesPropertyTable.Columns.OWNER_ID + ", "
                    + StatusesTable.TABLE_NAME + "."
                    + StatusesTable.Columns.AUTHOR_ID + ", "
                    + StatusesPropertyTable.TABLE_NAME + "."
                    + StatusesPropertyTable.Columns.TYPE + ", "
                    + StatusesTable.TABLE_NAME + "."
                    + StatusesTable.Columns.CREATED_AT + " FROM "
                    + StatusesPropertyTable.TABLE_NAME + " LEFT JOIN "
                    + StatusesTable.TABLE_NAME + " ON "
                    + StatusesPropertyTable.TABLE_NAME + "."
                    + StatusesPropertyTable.Columns.STATUS_ID + " = "
                    + StatusesTable.TABLE_NAME + "."
                    + StatusesTable.Columns.STATUS_ID + ";";

            return "CREATE VIEW " + createString;
        }

        public static String getDropSQL() {
            return "DROP VIEW " + VIEW_NAME;
        }

        public static String[] getIndexColumns() {
            return new String[] { Columns.STATUS_ID, Columns.OWNER_ID,
                    Columns.AUTHOR_ID, Columns.TYPE, Columns.CREATED_AT };
        }
    }
}
