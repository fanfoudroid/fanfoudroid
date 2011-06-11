package com.ch_linghu.fanfoudroid.db2;


public abstract class FanContent {
   
    public interface StatusColumns {
        public static final String ID = "_id";
        public static final String STATUS_ID = "status_id";
        public static final String AUTHOR_ID = "author_id";
        public static final String TEXT = "text";
        public static final String SOURCE = "source";
        public static final String TRUNCATED = "truncated";
        public static final String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
        public static final String IN_REPLY_TO_USER_ID = "in_reply_to_user_id";
        public static final String FAVORITED =  "favorited";
        public static final String CREATED_AT = "created_at";
        public static final String PIC_THUMB = "pic_thumbnail";
        public static final String PIC_MID = "pic_middle";
        public static final String PIC_ORIG = "pic_original";
        
        public static final String STATUS_TYPE = "status_type";
    }
    
    public interface StatusGroupsColumns {
        public static final String ID = "_id";
        public static final String TYPE = "TYPE";
        public static final String OWNER_ID = "owner_id";
        public static final String TAG = "tag";
        public static final String STATUS_ID = "g_status_id";
        public static final String IS_READ = "is_read";
        public static final String IS_LAST = "is_last";
        public static final String TIMELINE = "timeline";
    }
    
    public interface UserColumns {
        public static final String ID = "_id";
        public static final String USER_ID = "user_id";
        public static final String USER_NAME = "user_name";
        public static final String SCREEN_NAME = "screen_name";
        public static final String LOCATION = "location";
        public static final String DESCRIPTION = "description";
        public static final String PROFILE_IMAGE_USER = "profile_image_user";
        public static final String URL = "url";
        public static final String PROTECTED = "protected";
        public static final String FOLLOWERS_COUNT = "followers_count";
        public static final String STATUS_ID = "status_id";
    }
    
    public interface UserGroupsColumns {
        public static final String ID = "_id";
        public static final String TYPE = "type";
        public static final String TAG = "tag";
        public static final String OWNER_ID = "owner_id";
        public static final String USER_ID = "user_id";
        public static final String IS_LAST = "is_last";
    }
   
}
