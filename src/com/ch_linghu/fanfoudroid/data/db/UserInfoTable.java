package com.ch_linghu.fanfoudroid.data.db;

import java.text.ParseException;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import com.ch_linghu.fanfoudroid.data.User;

import com.ch_linghu.fanfoudroid.data.db.StatusTablesInfo.MessageTable;

public final class UserInfoTable implements BaseColumns {

	public static final String TAG = "UserInfoTable";
	
    public static final String TABLE_NAME = "userinfo";

	public static final String FIELD_USER_NAME = "name";
    public static final String FIELD_USER_SCREEN_NAME = "screen_name";
	public static final String FIELD_LOCALTION = "location";
	public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_PROFILE_IMAGE_URL = "profile_image_url";
	public static final String FIELD_URL = "url";
	public static final String FIELD_PROTECTED = "protected";
	public static final String FIELD_FOLLOWERS_COUNT = "followers_count";
	public static final String FIELD_FRIENDS_COUNT = "friends_count";
	public static final String FIELD_FAVORITES_COUNT = "favourites_count";
	public static final String FIELD_STATUSES_COUNT = "statuses_count";
	public static final String FIELD_LAST_STATUS = "last_status";
    public static final String FIELD_CREATED_AT = "created_at";
	public static final String FIELD_FOLLOWING = "following";
	
    public static final String[] TABLE_COLUMNS = new String[] { _ID,
    	FIELD_USER_NAME, FIELD_USER_SCREEN_NAME,
    	FIELD_LOCALTION, FIELD_DESCRIPTION, 
    	FIELD_PROFILE_IMAGE_URL, FIELD_URL, FIELD_PROTECTED,
    	FIELD_FOLLOWERS_COUNT, FIELD_FRIENDS_COUNT, 
    	FIELD_FAVORITES_COUNT, FIELD_STATUSES_COUNT, 
    	FIELD_LAST_STATUS, FIELD_CREATED_AT, FIELD_FOLLOWING};
    
    public static final String CREATE_TABLE = "create table "
        + TABLE_NAME + " (" 
        + _ID + " text primary key on conflict replace, "
    	+ FIELD_USER_NAME + " text not null, "
        + FIELD_USER_SCREEN_NAME + " text, "
    	+ FIELD_LOCALTION + " text, "
    	+ FIELD_DESCRIPTION + " text, "
        + FIELD_PROFILE_IMAGE_URL + " text, "
    	+ FIELD_URL + " text, "
    	+ FIELD_PROTECTED + " boolean, "
    	+ FIELD_FOLLOWERS_COUNT + " integer, "
    	+ FIELD_FRIENDS_COUNT + " integer, "
    	+ FIELD_FAVORITES_COUNT + " integer, "
    	+ FIELD_STATUSES_COUNT + " integer, "
    	+ FIELD_LAST_STATUS + " text, "
        + FIELD_CREATED_AT + " date, "
    	+ FIELD_FOLLOWING + " boolean "
    	+ ")";
    
	 /**
	 * TODO: 将游标解析为一条用户信息
	 * 
	 * @param cursor 该方法不会关闭游标
	 * @return 成功返回User类型的单条数据, 失败返回null
	 */
	public static User parseCursor(Cursor cursor) {
	    
	    if (null == cursor || 0 == cursor.getCount()) {
	        Log.w(TAG, "Cann't parse Cursor, bacause cursor is null or empty.");
	        return null;
	    }
	    
	    User user = new User();
	    
		user.id = cursor.getString(cursor.getColumnIndex(_ID));
		user.name = cursor.getString(cursor.getColumnIndex(FIELD_USER_NAME));
	    user.screenName = cursor.getString(cursor.getColumnIndex(FIELD_USER_SCREEN_NAME));
		user.location = cursor.getString(cursor.getColumnIndex(FIELD_LOCALTION));
		user.description = cursor.getString(cursor.getColumnIndex(FIELD_DESCRIPTION));
	    user.profileImageUrl = cursor.getString(cursor.getColumnIndex(FIELD_PROFILE_IMAGE_URL));
		user.url = cursor.getString(cursor.getColumnIndex(FIELD_URL));
		user.isProtected = (0 == cursor.getInt(cursor.getColumnIndex(FIELD_PROTECTED))) ? false : true;
		user.followersCount = cursor.getInt(cursor.getColumnIndex(FIELD_FOLLOWERS_COUNT));
		user.lastStatus = cursor.getString(cursor.getColumnIndex(FIELD_LAST_STATUS));
	
		user.friendsCount = cursor.getInt(cursor.getColumnIndex(FIELD_FRIENDS_COUNT));
		user.favoritesCount = cursor.getInt(cursor.getColumnIndex(FIELD_FAVORITES_COUNT));
		user.statusesCount = cursor.getInt(cursor.getColumnIndex(FIELD_STATUSES_COUNT));
		user.isFollowing = (0 == cursor.getInt(cursor.getColumnIndex(FIELD_FOLLOWING))) ? false : true;
	
	    try {
	        user.createdAt = StatusDatabase.DB_DATE_FORMATTER.parse(cursor.getString(cursor.getColumnIndex(MessageTable.FIELD_CREATED_AT)));
	    } catch (ParseException e) {
	        Log.w(TAG, "Invalid created at data.");
	    }
	    
	    return user;
	}
	
}