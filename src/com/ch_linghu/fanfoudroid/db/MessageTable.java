package com.ch_linghu.fanfoudroid.db;

import java.text.ParseException;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import com.ch_linghu.fanfoudroid.data.Dm;

/**
 * Table - Direct Messages
 * 
 */
public final class MessageTable implements BaseColumns {

	public static final String TAG = "MessageTable";

	public static final int TYPE_GET = 0;
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

	public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
			+ " (" + _ID + " text primary key on conflict replace, "
			+ FIELD_USER_SCREEN_NAME + " text not null, " + FIELD_TEXT
			+ " text not null, " + FIELD_PROFILE_IMAGE_URL + " text not null, "
			+ FIELD_IS_UNREAD + " boolean not null, " + FIELD_IS_SENT
			+ " boolean not null, " + FIELD_CREATED_AT + " date not null, "
			+ FIELD_USER_ID + " text)";

	/**
	 * TODO: 将游标解析为一条私信
	 * 
	 * @param cursor
	 *            该方法不会关闭游标
	 * @return 成功返回Dm类型的单条数据, 失败返回null
	 */
	public static Dm parseCursor(Cursor cursor) {

		if (null == cursor || 0 == cursor.getCount()) {
			Log.w(TAG, "Cann't parse Cursor, bacause cursor is null or empty.");
			return null;
		}

		Dm dm = new Dm();

		dm.id = cursor.getString(cursor.getColumnIndex(MessageTable._ID));
		dm.screenName = cursor.getString(cursor
				.getColumnIndex(MessageTable.FIELD_USER_SCREEN_NAME));
		dm.text = cursor.getString(cursor
				.getColumnIndex(MessageTable.FIELD_TEXT));
		dm.profileImageUrl = cursor.getString(cursor
				.getColumnIndex(MessageTable.FIELD_PROFILE_IMAGE_URL));
		dm.isSent = (0 == cursor.getInt(cursor
				.getColumnIndex(MessageTable.FIELD_IS_SENT))) ? false : true;
		try {
			dm.createdAt = TwitterDatabase.DB_DATE_FORMATTER.parse(cursor
					.getString(cursor
							.getColumnIndex(MessageTable.FIELD_CREATED_AT)));
		} catch (ParseException e) {
			Log.w(TAG, "Invalid created at data.");
		}
		dm.userId = cursor.getString(cursor
				.getColumnIndex(MessageTable.FIELD_USER_ID));

		return dm;
	}
}