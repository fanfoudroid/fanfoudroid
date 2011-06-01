package com.ch_linghu.fanfoudroid.data2;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ch_linghu.fanfoudroid.db.StatusTable;
import com.ch_linghu.fanfoudroid.db2.FanContent;
import com.ch_linghu.fanfoudroid.json.JsonMapper;
import com.ch_linghu.fanfoudroid.json.JsonParser;
import com.ch_linghu.fanfoudroid.json.JsonParserException;
import com.ch_linghu.fanfoudroid.json.JsonUtils;
import com.ch_linghu.fanfoudroid.util.DateTimeHelper;
import com.ch_linghu.fanfoudroid.util.TextHelper;
import com.temp.afan.data.dao.RowMapper;
import com.temp.afan.data.dao.StatusDAO;

public class Status extends FanContent implements java.io.Serializable {
    private static final long serialVersionUID = 8307449050213481609L;
    private static final String TAG = "Status";
    public static final String TABLE_NAME = "statuses";
    
    private Date created_at;
    private String id;
    private String text;
    private String source;
    private boolean truncated;
    private String in_reply_to_status_id;
    private String in_reply_to_user_id;
    private boolean favorited;
    private String in_reply_to_screen_name;
    private Photo photo_url;
    private User user;
    
    private boolean isUnRead = false;
    private int type = -1;
    private String owner_id;

    public Status() {}
    
    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public void setTruncated(boolean truncated) {
        this.truncated = truncated;
    }

    public String getInReplyToStatusId() {
        return in_reply_to_status_id;
    }

    public void setInReplyToStatusId(String in_reply_to_status_id) {
        this.in_reply_to_status_id = in_reply_to_status_id;
    }

    public String getInReplyToUserId() {
        return in_reply_to_user_id;
    }

    public void setInReplyToUserId(String in_reply_to_user_id) {
        this.in_reply_to_user_id = in_reply_to_user_id;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public String getInReplyToScreenName() {
        return in_reply_to_screen_name;
    }

    public void setInReplyToScreenName(String in_reply_to_screen_name) {
        this.in_reply_to_screen_name = in_reply_to_screen_name;
    }

    public Photo getPhotoUrl() {
        return photo_url;
    }

    public void setPhotoUrl(Photo photo_url) {
        this.photo_url = photo_url;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public boolean isUnRead() {
        return isUnRead;
    }

    public void setUnRead(boolean isUnRead) {
        this.isUnRead = isUnRead;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getOwnerId() {
        return owner_id;
    }

    public void setOwnerId(String owner_id) {
        this.owner_id = owner_id;
    }
    
    
    // Database Access
    
    /**
     * Insert a Status
     * 
     * 若报 SQLiteconstraintexception 异常, 检查是否某not null字段为空
     * 
     * @param status
     * @param isUnread
     * @return
     */
    public long insertStatus(Status status) {
        if (!isExists(status)) {
            return getDb(true).insert(TABLE_NAME, null, toContentValues());
        } else {
            Log.e(TAG, status.getId() + " is exists.");
            return -1;
        }
    }

    public int insertStatuses(List<Status> statuses) {
        int result = 0;
        SQLiteDatabase db = getDb(true);

        try {
            db.beginTransaction();
            for (int i = statuses.size() - 1; i >= 0; i--) {
                Status status = statuses.get(i);

                long id = db.insertWithOnConflict(StatusTable.TABLE_NAME, null,
                        toContentValues(),
                        SQLiteDatabase.CONFLICT_IGNORE);

                if (-1 == id) {
                    Log.e(TAG, "cann't insert the tweet : " + status.toString());
                } else {
                    ++result;
                    Log.v(TAG, String.format(
                            "Insert a status into database : %s",
                            status.toString()));
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return result;
    }

    /**
     * Delete a status
     * 
     * @param statusId
     * @param owner
     *            owner id
     * @param type
     *            status type
     * @return
     * @see StatusDAO#deleteStatus(Status)
     */
    public int deleteStatus(String statusId, String owner, int type) {
        String where = StatusTable._ID + " =? ";
        String[] binds;

        if (!TextHelper.isEmpty(owner)) {
            where += " AND " + StatusTable.OWNER_ID + " = ? ";
            binds = new String[] { statusId, owner };
        } else {
            binds = new String[] { statusId };
        }

        if (-1 != type) {
            where += " AND " + StatusTable.STATUS_TYPE + " = " + type;
        }

        return getDb(true).delete(TABLE_NAME, where.toString(), binds);
    }

    /**
     * Delete a Status
     * 
     * @param status
     * @return
     * @see StatusDAO#deleteStatus(String, String, int)
     */
    public int deleteStatus(Status status) {
        return deleteStatus(status.getId(), status.getOwnerId(),
                status.getType());
    }

    /**
     * Find a status by status ID
     * 
     * @param statusId
     * @return
     */
    public Status fetchStatus(String statusId) {
        return queryForObject(mRowMapper, TABLE_NAME, null,
                StatusTable._ID + " = ?", new String[] { statusId }, null,
                null, "created_at DESC", "1");
    }

    /**
     * Find user's statuses
     * 
     * @param userId
     *            user id
     * @param statusType
     *            status type, see {@link StatusTable#TYPE_USER}...
     * @return list of statuses
     */
    public List<Status> fetchStatuses(String userId, int statusType) {
        return queryForList(mRowMapper, TABLE_NAME, null,
                StatusTable.OWNER_ID + " = ? AND " + StatusTable.STATUS_TYPE
                        + " = " + statusType, new String[] { userId }, null,
                null, "created_at DESC", null);
    }

    /**
     * @see StatusDAO#fetchStatuses(String, int)
     */
    public List<Status> fetchStatuses(String userId, String statusType) {
        return fetchStatuses(userId, Integer.parseInt(statusType));
    }

    /**
     * Update by using {@link ContentValues}
     * 
     * @param statusId
     * @param newValues
     * @return
     */
    public int updateStatus(String statusId, ContentValues values) {
        return updateById(TABLE_NAME, statusId, values);
    }

    /**
     * Update by using {@link Status}
     * 
     * @param status
     * @return
     */
    public int updateStatus(Status status) {
        return updateStatus(status.getId(), toContentValues());
    }

    /**
     * Check if status exists
     * 
     * FIXME: 取消使用Query
     * 
     * @param status
     * @return
     */
    public boolean isExists(Status status) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(TABLE_NAME).append(" WHERE ")
                .append(StatusTable._ID).append(" =? AND ")
                .append(StatusTable.OWNER_ID).append(" =? AND ")
                .append(StatusTable.STATUS_TYPE).append(" = ")
                .append(status.getType());

        return isExistsBySQL(sql.toString(),
                new String[] { status.getId(), status.getUser().getId() });
    }

    /**
     * Status -> ContentValues
     * 
     * @param status
     * @param isUnread
     * @return
     */
    private ContentValues toContentValues() {
        final ContentValues v = new ContentValues();
        v.put(StatusTable._ID, mId);
        /*
        v.put(StatusTable.STATUS_TYPE, status.getType());
        v.put(StatusTable.TEXT, status.getText());
        v.put(StatusTable.OWNER_ID, status.getOwnerId());
        v.put(StatusTable.FAVORITED, status.isFavorited() + "");
        v.put(StatusTable.TRUNCATED, status.isTruncated()); // TODO:
        v.put(StatusTable.IN_REPLY_TO_STATUS_ID, status.getInReplyToStatusId());
        v.put(StatusTable.IN_REPLY_TO_USER_ID, status.getInReplyToUserId());
        v.put(StatusTable.IN_REPLY_TO_SCREEN_NAME,
                status.getInReplyToScreenName());
        // v.put(IS_REPLY, status.isReply());
        v.put(StatusTable.CREATED_AT,
                TwitterDatabase.DB_DATE_FORMATTER.format(status.getCreatedAt()));
        v.put(StatusTable.SOURCE, status.getSource());
        v.put(StatusTable.IS_UNREAD, status.isUnRead());

        final User user = status.getUser();
        if (user != null) {
            v.put(StatusTable.USER_ID, user.getId());
            v.put(StatusTable.USER_SCREEN_NAME, user.getScreenName());
            v.put(StatusTable.PROFILE_IMAGE_URL, user.getProfileImageUrl());
        }
        final Photo photo = status.getPhotoUrl();
        if (photo != null) {
            v.put(StatusTable.PIC_THUMB, photo.getThumburl());
            v.put(StatusTable.PIC_MID, photo.getImageurl());
            v.put(StatusTable.PIC_ORIG, photo.getLargeurl());
        }
        */

        return v;
    }
    
    public static Status valueOf(Cursor cursor) {
        return valueOf(mRowMapper, cursor);
    }

    private static final RowMapper<Status> mRowMapper = new RowMapper<Status>() {

        @Override
        public Status mapRow(Cursor cursor, int rowNum) {
            // TODO: 减少实例对象
            Photo photo = new Photo();
            photo.setImageurl(cursor.getString(cursor
                    .getColumnIndex(StatusTable.PIC_MID)));
            photo.setLargeurl(cursor.getString(cursor
                    .getColumnIndex(StatusTable.PIC_ORIG)));
            photo.setThumburl(cursor.getString(cursor
                    .getColumnIndex(StatusTable.PIC_THUMB)));
            User user = new User();
            user.setScreenName(cursor.getString(cursor
                    .getColumnIndex(StatusTable.USER_SCREEN_NAME)));
            user.setId(cursor.getString(cursor
                    .getColumnIndex(StatusTable.USER_ID)));
            user.setProfileImageUrl(cursor.getString(cursor
                    .getColumnIndex(StatusTable.PROFILE_IMAGE_URL)));

            Status status = new Status();
            status.setPhotoUrl(photo);
            status.setUser(user);
            status.setOwnerId(cursor.getString(cursor
                    .getColumnIndex(StatusTable.OWNER_ID)));
            // TODO: 将数据库中的statusType改成Int类型
            status.setType(cursor.getInt(cursor
                    .getColumnIndex(StatusTable.STATUS_TYPE)));
            status.setId(cursor.getString(cursor
                    .getColumnIndex(StatusTable._ID)));
            status.setCreatedAt(DateTimeHelper.parseDateTimeFromSqlite(cursor
                    .getString(cursor.getColumnIndex(StatusTable.CREATED_AT))));
            // TODO: 更改favorite 在数据库类型为boolean后改为 " != 0 "
            status.setFavorited(cursor.getString(
                    cursor.getColumnIndex(StatusTable.FAVORITED))
                    .equals("true"));
            status.setText(cursor.getString(cursor
                    .getColumnIndex(StatusTable.TEXT)));
            status.setSource(cursor.getString(cursor
                    .getColumnIndex(StatusTable.SOURCE)));
            status.setInReplyToScreenName(cursor.getString(cursor
                    .getColumnIndex(StatusTable.IN_REPLY_TO_SCREEN_NAME)));
            status.setInReplyToStatusId(cursor.getString(cursor
                    .getColumnIndex(StatusTable.IN_REPLY_TO_STATUS_ID)));
            status.setInReplyToUserId(cursor.getString(cursor
                    .getColumnIndex(StatusTable.IN_REPLY_TO_USER_ID)));
            status.setTruncated(cursor.getInt(cursor
                    .getColumnIndex(StatusTable.TRUNCATED)) != 0);
            status.setUnRead(cursor.getInt(cursor
                    .getColumnIndex(StatusTable.IS_UNREAD)) != 0);
            return status;
        }
    };
    
    // JSON SUPPORT
    
    public static Status valueOf(JSONObject json)
            throws JsonParserException {
        return JsonParser.parseToObject(json, mStatusJsonMap);
    }

    public static List<Status> valueOf(JSONArray json)
            throws JsonParserException {
        return JsonParser.parseToList(json, mStatusJsonMap);
    }

    private static final JsonMapper<Status> mStatusJsonMap = new JsonMapper<Status>() {

        @Override
        public Status mapRow(JSONObject json) throws JsonParserException {
            try {
                Status status = new Status();
                status.setId(json.getString("id"));
                status.setText(json.getString("text"));
                status.setSource(json.getString("source"));
                status.setCreatedAt(JsonUtils.parseDate(
                        json.getString("created_at"),
                        "EEE MMM dd HH:mm:ss z yyyy"));
                status.setFavorited(JsonUtils.getBoolean("favorited", json));
                status.setTruncated(JsonUtils.getBoolean("truncated", json));
                status.setInReplyToStatusId(JsonUtils.getString(
                        "in_reply_to_status_id", json));
                status.setInReplyToUserId(JsonUtils.getString(
                        "in_reply_to_user_id", json));
                status.setInReplyToScreenName(json
                        .getString("in_reply_to_screen_name"));
                if (!json.isNull("photo")) {
                    final JSONObject photoJson = json.getJSONObject("photo");
                    Photo photo = new Photo();
                    photo.setThumburl(photoJson.getString("thumburl"));
                    photo.setImageurl(photoJson.getString("imageurl"));
                    photo.setLargeurl(photoJson.getString("largeurl"));
                    status.setPhotoUrl(photo);
                }
                if (!json.isNull("user")) {
                    final JSONObject userJson = json.getJSONObject("user");
                    User user = new User();
                    user.setScreenName(userJson
                            .getString("in_reply_to_screen_name"));
                    status.setUser(user);
                }
                return status;
            } catch (JSONException e) {
                throw new JsonParserException("Cann't convert to JSONObject", e);
            }
        }

    };

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Status other = (Status) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (owner_id == null) {
            if (other.owner_id != null)
                return false;
        } else if (!owner_id.equals(other.owner_id))
            return false;
        if (type != other.type)
            return false;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Status [created_at=" + created_at + ", id=" + id + ", text="
                + text + ", source=" + source + ", truncated=" + truncated
                + ", in_reply_to_status_id=" + in_reply_to_status_id
                + ", in_reply_to_user_id=" + in_reply_to_user_id
                + ", favorited=" + favorited + ", in_reply_to_screen_name="
                + in_reply_to_screen_name + ", photo_url=" + photo_url
                + ", user=" + user + "]";
    }

   
}
