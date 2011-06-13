package com.ch_linghu.fanfoudroid.db2;

public abstract class FanContent {
   
    public static class StatusTable{
        public static final String TABLE_NAME = "statuses";
    	
    	public static class Columns {
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
    	
    	public static String getCreateSQL(){
            String createString = TABLE_NAME + "( " 
            + Columns.ID + " INTEGER PRIMARY KEY, " 
            + Columns.STATUS_ID + " INT UNIQUE NOT NULL, " 
            + Columns.AUTHOR_ID + " INT, "
            + Columns.TEXT + " TEXT, " 
            + Columns.SOURCE + " TEXT, " 
            + Columns.TRUNCATED + " INT, "
            + Columns.IN_REPLY_TO_STATUS_ID + " INT, "
            + Columns.IN_REPLY_TO_USER_ID + " INT, "
            + Columns.FAVORITED + " INT, " 
            + Columns.CREATED_AT + " INT " 
            + Columns.PIC_THUMB + " TEXT, "
            + Columns.PIC_MID + " TEXT, " 
            + Columns.PIC_ORIG + " TEXT " 
            + ");";   	
            
            return "CREATE TABLE " + createString;
    	}
    	
    	public static String getDropSQL(){
    		return "DROP TABLE " + TABLE_NAME;
    	}
    	
    	public static String[] getIndexColumns(){
    		return new String[] {};
    	}
    }
    
    public static class StatusGroupTable {
        public static final String TABLE_NAME = "status_groups";
    	
    	public static class Columns {
	        public static final String ID = "_id";
	        public static final String TYPE = "TYPE";
	        public static final String OWNER_ID = "owner_id";
	        public static final String TAG = "tag";
	        public static final String STATUS_ID = "g_status_id";
	        public static final String IS_READ = "is_read";
	        public static final String IS_LAST = "is_last";
	        public static final String TIMELINE = "timeline";
    	}
    }
    
    public static class UserTable {
        public static final String TABLE_NAME = "users";
    	
    	public static class Columns {
	        public static final String ID = "_id";
	        public static final String USER_ID = "user_id";
	        public static final String USER_NAME = "user_name";
	        public static final String SCREEN_NAME = "screen_name";
	        public static final String LOCATION = "location";
	        public static final String DESCRIPTION = "description";
	        public static final String PROFILE_IMAGE_URL = "profile_image_url";
	        public static final String URL = "url";
	        public static final String PROTECTED = "protected";
	        public static final String FOLLOWERS_COUNT = "followers_count";
	        public static final String STATUS_ID = "status_id";
    	}
    }
    
    public static class UserGroupTable {
        public static final String TABLE_NAME = "user_groups";
    	
    	public static class Columns {
    		public static final String ID = "_id";
	        public static final String TYPE = "type";
	        public static final String TAG = "tag";
	        public static final String OWNER_ID = "owner_id";
	        public static final String USER_ID = "user_id";
	        public static final String IS_LAST = "is_last";
    	}
    }
   
}
