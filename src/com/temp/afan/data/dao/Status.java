package com.temp.afan.data.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ch_linghu.fanfoudroid.fanfou.Photo;
import com.ch_linghu.fanfoudroid.fanfou.RetweetDetails;
import com.ch_linghu.fanfoudroid.fanfou.User;
import com.ch_linghu.fanfoudroid.fanfou.Weibo;
import com.ch_linghu.fanfoudroid.fanfou.WeiboResponse;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.http.Response;

/**
 * 为避免影响旧数据格式, 临时测试用,
 * 
 * 今后结构:
 *  fanfoudroid.db 放数据库基本类
 *  fanfoudroid.data 各种数据逻辑类
 *  fanfoudroid.dao 数据DAO类
 *  fanfroudroi.weibo 无数据类, 只保留API部分
 */
public class Status extends WeiboResponse implements java.io.Serializable {
    private static final long serialVersionUID = 1608000492860584608L;

    // Required
    private String id;
    private String text;
    private String source;
    private String userId;
    private String ownerId; //用于标识数据的所有者。以便于处理其他用户的信息（如其他用户的收藏）
    private String userScreenName;
    private String profileImageUrl;
    private Date createdAt;
    private boolean isUnRead = true;
    private String type = "-1";
    
    private boolean isFavorited = false;
    private boolean isTruncated = false;
    private String inReplyToStatusId = "";
    private String inReplyToUserId = "";
    private String inReplyToScreenName = "";
    private double latitude = -1;
    private double longitude = -1;
    private String thumbnailPic = "";
    private String bmiddlePic = "";
    private String originalPic = "";
    
    private User user = null;
    
    private RetweetDetails retweetDetails = null;
    
    public Status() {
    }

    public Status(Response res, Element elem, Weibo weibo) throws HttpException
    {
        super(res);
        init(res, elem, weibo);
    }
    
    public Status(Response res) throws HttpException, JSONException{
    	super(res);
        init(res.asJSONObject());
    }
    
    /* modify by sycheng add some field*/
    public Status(JSONObject json)throws HttpException, JSONException{
        super();
        init(json);
    }
    
    public Status(String str) throws HttpException, JSONException {
        this(new JSONObject(str));
    }
    
    /** for JSON */
    private void init(JSONObject json) throws JSONException, HttpException {
        id = json.getString("id");
        text = json.getString("text");
        source = json.getString("source");
        createdAt = parseDate(json.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");

        isFavorited = getBoolean("favorited", json);
        isTruncated=getBoolean("truncated", json);
        
        inReplyToStatusId = getString("in_reply_to_status_id", json);
        inReplyToUserId = getString("in_reply_to_user_id", json);
        inReplyToScreenName=json.getString("in_reply_to_screen_name");
        if(!json.isNull("photo")) {
            Photo photo = new Photo(json.getJSONObject("photo"));
            thumbnailPic = photo.getThumbnail_pic();
            bmiddlePic = photo.getBmiddle_pic();
            originalPic = photo.getOriginal_pic();
        }
        if(!json.isNull("user"))
            user = new User(json.getJSONObject("user"));
            inReplyToScreenName=json.getString("in_reply_to_screen_name");
        user = new User(json.getJSONObject("user"));
    }

    /** for XML */
    private void init(Response res, Element elem, Weibo weibo) throws
            HttpException {
        ensureRootNodeNameIs("status", elem);
        user = new User(res, (Element) elem.getElementsByTagName("user").item(0)
                , weibo);
        id = getChildString("id", elem);
        text = getChildText("text", elem);
        source = getChildText("source", elem);
        createdAt = getChildDate("created_at", elem);
        isTruncated = getChildBoolean("truncated", elem);
        inReplyToStatusId = getChildString("in_reply_to_status_id", elem);
        inReplyToUserId = getChildString("in_reply_to_user_id", elem);
        isFavorited = getChildBoolean("favorited", elem);
        inReplyToScreenName = getChildText("in_reply_to_screen_name", elem);
        NodeList georssPoint = elem.getElementsByTagName("georss:point");
        
        if(1 == georssPoint.getLength()){
            String[] point = georssPoint.item(0).getFirstChild().getNodeValue().split(" ");
            if(!"null".equals(point[0]))
            	latitude = Double.parseDouble(point[0]);
            if(!"null".equals(point[1]))
            	longitude = Double.parseDouble(point[1]);
        }
        NodeList retweetDetailsNode = elem.getElementsByTagName("retweet_details");
        if(1 == retweetDetailsNode.getLength()){
            retweetDetails = new RetweetDetails(res,(Element)retweetDetailsNode.item(0),weibo);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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

    public String getUserScreenName() {
        return userScreenName;
    }

    public void setUserScreenName(String userScreenName) {
        this.userScreenName = userScreenName;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isTruncated() {
        return isTruncated;
    }

    public void setTruncated(boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    public String getInReplyToStatusId() {
        return inReplyToStatusId;
    }

    public void setInReplyToStatusId(String inReplyToStatusId) {
        this.inReplyToStatusId = inReplyToStatusId;
    }

    public String getInReplyToUserId() {
        return inReplyToUserId;
    }

    public void setInReplyToUserId(String inReplyToUserId) {
        this.inReplyToUserId = inReplyToUserId;
    }

    public boolean isFavorited() {
        return isFavorited;
    }

    public void setFavorited(boolean isFavorited) {
        this.isFavorited = isFavorited;
    }

    public String getInReplyToScreenName() {
        return inReplyToScreenName;
    }

    public void setInReplyToScreenName(String inReplyToScreenName) {
        this.inReplyToScreenName = inReplyToScreenName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getThumbnailPic() {
        return thumbnailPic;
    }

    public void setThumbnailPic(String thumbnail_pic) {
        this.thumbnailPic = thumbnail_pic;
    }

    public String getBmiddlePic() {
        return bmiddlePic;
    }

    public void setBmiddlePic(String bmiddle_pic) {
        this.bmiddlePic = bmiddle_pic;
    }

    public String getOriginalPic() {
        return originalPic;
    }

    public void setOriginalPic(String original_pic) {
        this.originalPic = original_pic;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public RetweetDetails getRetweetDetails() {
        return retweetDetails;
    }

    public void setRetweetDetails(RetweetDetails retweetDetails) {
        this.retweetDetails = retweetDetails;
    }

    public boolean isUnRead() {
        return isUnRead;
    }

    public void setIsUnRead(boolean isUnRead) {
        this.isUnRead = isUnRead;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    
    /*modify by sycheng add json call method*/
    /*package*/
    static List<Status> constructStatuses(Response res) throws HttpException {
    	 try {
             JSONArray list = res.asJSONArray();
             int size = list.length();
             List<Status> statuses = new ArrayList<Status>(size);
             for (int i = 0; i < size; i++) {
                 statuses.add(new Status(list.getJSONObject(i)));
             }
             return statuses;
         } catch (JSONException jsone) {
             throw new HttpException(jsone);
         } catch (HttpException te) {
             throw te;
         }  
       
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
//      return obj instanceof Status && ((Status) obj).id == this.id;
        return obj instanceof Status && this.id.equals(((Status) obj).id);
    }

    @Override
    public String toString() {
        return "Status{" +
                "createdAt=" + createdAt +
                ", id=" + id +
                ", text='" + text + '\'' +
                ", source='" + source + '\'' +
                ", isTruncated=" + isTruncated +
                ", inReplyToStatusId=" + inReplyToStatusId +
                ", inReplyToUserId=" + inReplyToUserId +
                ", isFavorited=" + isFavorited +
                ", thumbnail_pic=" + thumbnailPic +
                ", bmiddle_pic=" + bmiddlePic +
                ", original_pic=" + originalPic +
                ", inReplyToScreenName='" + inReplyToScreenName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", retweetDetails=" + retweetDetails +
                ", user=" + user +
                '}';
    }
    
    public boolean isEmpty() {
    	return (null == id);
    }
}
