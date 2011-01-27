package com.ch_linghu.fanfoudroid.data.db;

import java.text.ParseException;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import com.ch_linghu.fanfoudroid.data.Dm;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.helper.Utils;

/**
 * All information of status table
 *
 */
public final class StatusTablesInfo {
    /**
     * Table - Statuses
     * <br /> <br />
     * 为节省流量,故此表不保证本地数据库中所有消息具有前后连贯性, 而只确保最新的MAX_ROW_NUM条<br />
     * 数据的连贯性, 超出部分则视为垃圾数据, 不再允许读取, 也不保证其是前后连续的.<br />
     * <br />
     * 因为用户可能中途长时间停止使用本客户端,而换其他客户端(如网页), <br />
     * 如果保证本地所有数据的连贯性, 那么就必须自动去下载所有本地缺失的中间数据,<br />
     * 而这些数据极有可能是用户通过其他客户端阅读过的无用信息, 浪费了用户流量.<br />
     * <br />
     * 即认为相对于旧信息而言, 新信息对于用户更为价值, 所以只会新信息进行维护, <br />
     * 而旧信息一律视为无用的, 如用户需要查看超过MAX_ROW_NUM的旧数据, 可主动点击, <br />
     * 从而请求服务器. 本地只缓存最有价值的MAX条最新信息.<br />
     * <br />
     * 本地数据库中前MAX_ROW_NUM条的数据模拟一个定长列队, 即在尾部插入N条消息, 就会使得头部<br />
     * 的N条消息被标记为垃圾数据(但并不立即收回),只有在认为数据库数据过多时,<br />
     * 可手动调用 <code>StatusDatabase.gc(int type)</code> 方法进行垃圾清理.<br />
     * 
     * 
     */
    public static final class StatusTable implements BaseColumns {
        
        public static final String TAG = "StatusTable";
        
        // Status Types
        public static final int TYPE_HOME = 1;		//首页(我和我的好友)
        public static final int TYPE_MENTION = 2;	//提到我的
        public static final int TYPE_USER = 3;		//指定USER的
        public static final int TYPE_FAVORITE = 4;	//收藏
        public static final int TYPE_BROWSE = 5;	//随便看看
        
        public static final String TABLE_NAME = "status";
        public static final int MAX_ROW_NUM = 20; //单类型数据安全区域
        
        public static final String FIELD_USER_ID = "uid";
        public static final String FIELD_USER_SCREEN_NAME = "screen_name";
        public static final String FIELD_PROFILE_IMAGE_URL = "profile_image_url";
        public static final String FIELD_CREATED_AT = "created_at";
        public static final String FIELD_TEXT = "text";
        public static final String FIELD_SOURCE = "source";
        public static final String FIELD_TRUNCATED = "truncated";
        public static final String FIELD_IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
        public static final String FIELD_IN_REPLY_TO_USER_ID = "in_reply_to_user_id";
        public static final String FIELD_IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";
        public static final String FIELD_FAVORITED =  "favorited";
        public static final String FIELD_IS_UNREAD = "is_unread";
        public static final String FIELD_STATUS_TYPE = "status_type";
    //    private static final String FIELD_PHOTO_URL = "photo_url";
    //    private double latitude = -1;
    //    private double longitude = -1;
    //    private String thumbnail_pic;
    //    private String bmiddle_pic;
    //    private String original_pic;
        
        public static final String[] TABLE_COLUMNS = new String[] {_ID, FIELD_USER_SCREEN_NAME,
            FIELD_TEXT, FIELD_PROFILE_IMAGE_URL, FIELD_IS_UNREAD, FIELD_CREATED_AT,
            FIELD_FAVORITED, FIELD_IN_REPLY_TO_STATUS_ID, FIELD_IN_REPLY_TO_USER_ID,
            FIELD_IN_REPLY_TO_SCREEN_NAME, FIELD_TRUNCATED,
            FIELD_SOURCE, FIELD_USER_ID, FIELD_STATUS_TYPE};
        
        public static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME + " (" 
            + _ID + " text not null,"
            + FIELD_STATUS_TYPE + " text not null, "
            + FIELD_USER_ID + " text not null, "
            + FIELD_USER_SCREEN_NAME + " text not null, "
            + FIELD_TEXT + " text not null, "
            + FIELD_PROFILE_IMAGE_URL + " text not null, "
            + FIELD_IS_UNREAD + " boolean not null, "
            + FIELD_CREATED_AT + " date not null, "
            + FIELD_FAVORITED + " text, "
            + FIELD_IN_REPLY_TO_STATUS_ID + " text, "
            + FIELD_IN_REPLY_TO_USER_ID + " text, "
            + FIELD_IN_REPLY_TO_SCREEN_NAME + " text, "
            + FIELD_SOURCE + " text not null, " 
            + FIELD_TRUNCATED + " boolean ,"
            + "PRIMARY KEY (" + _ID + ","+ FIELD_STATUS_TYPE  + "))";
        
        /**
         * 将游标解析为一条Tweet
         *
         * 
         * @param cursor 该方法不会移动或关闭游标
         * @return 成功返回 Tweet 类型的单条数据, 失败返回null
         */
        public static Tweet parseCursor(Cursor cursor) {
            
            if (null == cursor || 0 == cursor.getCount()) {
                Log.w(TAG, "Cann't parse Cursor, bacause cursor is null or empty.");
                return null;
            } else if ( -1 == cursor.getPosition() ) {
                cursor.moveToFirst();
            }
            
            Tweet tweet = new Tweet();
            tweet.id = cursor.getString(cursor.getColumnIndex(_ID));
            tweet.createdAt = Utils.parseDateTimeFromSqlite(cursor.getString(cursor.getColumnIndex(FIELD_CREATED_AT)));
            tweet.favorited = cursor.getString(cursor.getColumnIndex(FIELD_FAVORITED));
            tweet.screenName = cursor.getString(cursor.getColumnIndex(FIELD_USER_SCREEN_NAME));
            tweet.userId = cursor.getString(cursor.getColumnIndex(FIELD_USER_ID));
            tweet.text = cursor.getString(cursor.getColumnIndex(FIELD_TEXT));
            tweet.source = cursor.getString(cursor.getColumnIndex(FIELD_SOURCE));
            tweet.profileImageUrl = cursor.getString(cursor.getColumnIndex(FIELD_PROFILE_IMAGE_URL));
            tweet.inReplyToScreenName = cursor.getString(cursor.getColumnIndex(FIELD_IN_REPLY_TO_SCREEN_NAME));
            tweet.inReplyToStatusId = cursor.getString(cursor.getColumnIndex(FIELD_IN_REPLY_TO_STATUS_ID));
            tweet.inReplyToUserId = cursor.getString(cursor.getColumnIndex(FIELD_IN_REPLY_TO_USER_ID));
            tweet.truncated = cursor.getString(cursor.getColumnIndex(FIELD_TRUNCATED));
            tweet.setStatusType(cursor.getInt(cursor.getColumnIndex(FIELD_STATUS_TYPE)) );
            
            return tweet;
        }
    }
    
    /**
     *  Table - Direct Messages
     *
     */
    public static final class MessageTable implements BaseColumns {
        
        public static final String TAG = "MessageTable";
        
        public static final int TYPE_GET  = 0;
        public static final int TYPE_SENT = 1;
        
        public static final String TABLE_NAME = "message";
        public static final int MAX_ROW_NUM = 20;
        
        public static final String FIELD_USER_ID = "uid";
        public static final String FIELD_USER_SCREEN_NAME = "screen_name";
        public static final String FIELD_PROFILE_IMAGE_URL = "profile_image_url";
        public static final String FIELD_CREATED_AT = "created_at";
        public static final String FIELD_TEXT = "text";
        public static final String FIELD_IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
        public static final String FIELD_IN_REPLY_TO_USER_ID = "in_reply_to_user_id";
        public static final String FIELD_IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";
        public static final String FIELD_IS_UNREAD = "is_unread";
        public static final String FIELD_IS_SENT = "is_send";
        
        public static final String[] TABLE_COLUMNS = new String[] { _ID,
            FIELD_USER_SCREEN_NAME, FIELD_TEXT, FIELD_PROFILE_IMAGE_URL,
            FIELD_IS_UNREAD, FIELD_IS_SENT, FIELD_CREATED_AT, FIELD_USER_ID };
        
        public static final String CREATE_TABLE =  "CREATE TABLE "
            + TABLE_NAME + " (" 
            + _ID + " text primary key on conflict replace, " 
            + FIELD_USER_SCREEN_NAME + " text not null, "
            + FIELD_TEXT + " text not null, "
            + FIELD_PROFILE_IMAGE_URL + " text not null, "
            + FIELD_IS_UNREAD + " boolean not null, "
            + FIELD_IS_SENT + " boolean not null, "
            + FIELD_CREATED_AT + " date not null, " 
            + FIELD_USER_ID + " text)";
        
        /**
         * TODO: 将游标解析为一条私信
         * 
         * @param cursor 该方法不会关闭游标
         * @return 成功返回Dm类型的单条数据, 失败返回null
         */
        public static Dm parseCursor(Cursor cursor) {
            
            if (null == cursor || 0 == cursor.getCount()) {
                Log.w(TAG, "Cann't parse Cursor, bacause cursor is null or empty.");
                return null;
            }
            
            Dm dm = new Dm();
            
            dm.id = cursor.getString(cursor.getColumnIndex(MessageTable._ID));
            dm.screenName = cursor.getString(cursor.getColumnIndex(MessageTable.FIELD_USER_SCREEN_NAME));
            dm.text = cursor.getString(cursor.getColumnIndex(MessageTable.FIELD_TEXT));
            dm.profileImageUrl = cursor.getString(cursor.getColumnIndex(MessageTable.FIELD_PROFILE_IMAGE_URL));
            dm.isSent = (0 == cursor.getInt(cursor.getColumnIndex(MessageTable.FIELD_IS_SENT))) ? false : true ;
            try {
                dm.createdAt = StatusDatabase.DB_DATE_FORMATTER.parse(cursor.getString(cursor.getColumnIndex(MessageTable.FIELD_CREATED_AT)));
            } catch (ParseException e) {
                Log.w(TAG, "Invalid created at data.");
            }
            dm.userId = cursor.getString(cursor.getColumnIndex(MessageTable.FIELD_USER_ID));
            
            return dm;
        }
    }
    
    /**
     * Table - Followers
     *
     */
    public static final class FollowTable implements BaseColumns {
        
        public static final String TABLE_NAME = "followers";
        
        public static final String[] TABLE_COLUMNS = new String[] { _ID };
        
        public static final String CREATE_TABLE = "create table "
            + TABLE_NAME + " (" + _ID
            + " text primary key on conflict replace)";
    }
        
}