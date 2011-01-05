/*
Copyright (c) 2007-2009, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package tk.sandin.android.fanfoudroid.weibo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tk.sandin.android.fanfoudroid.http.Response;

/**
 * A data class representing one single status of a user.
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class Status extends WeiboResponse implements java.io.Serializable {

    private Date createdAt;
    private String id;
    private String text;
    private String source;
    private boolean isTruncated;
    private String inReplyToStatusId;
    private String inReplyToUserId;
    private boolean isFavorited;
    private String inReplyToScreenName;
    private double latitude = -1;
    private double longitude = -1;
    private String thumbnail_pic;
    private String bmiddle_pic;
    private String original_pic;
    private String photo_url;
    private RetweetDetails retweetDetails;
    private static final long serialVersionUID = 1608000492860584608L;

    /*package*/Status(Response res, Weibo weibo) throws WeiboException {
        super(res);
        Element elem = res.asDocument().getDocumentElement();
        init(res, elem, weibo);
    }

    /*package*/Status(Response res, Element elem, Weibo weibo) throws
            WeiboException {
        super(res);
        init(res, elem, weibo);
    }
    
    Status(Response res)throws WeiboException{
    	super(res);
    	JSONObject json=res.asJSONObject();
    	try {
			id = json.getString("id");
			text = json.getString("text");
			source = json.getString("source");
			createdAt = parseDate(json.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");

			inReplyToStatusId = getString("in_reply_to_status_id", json);
			inReplyToUserId = getString("in_reply_to_user_id", json);
			isFavorited = getBoolean("favorited", json);
//			System.out.println("json photo" + json.getJSONObject("photo"));
			if(!json.isNull("photo")) {
//				System.out.println("not null" + json.getJSONObject("photo"));
				Photo photo = new Photo(json.getJSONObject("photo"));
				thumbnail_pic = photo.getThumbnail_pic();
				bmiddle_pic = photo.getBmiddle_pic();
				original_pic = photo.getOriginal_pic();
			} else {
//				System.out.println("Null");
				thumbnail_pic = "";
				bmiddle_pic = "";
				original_pic = "";
			}
			if(!json.isNull("user"))
				user = new User(json.getJSONObject("user"));
				inReplyToScreenName=json.getString("in_reply_to_screen_name");
			if(!json.isNull("retweetDetails")){
				retweetDetails = new RetweetDetails(json.getJSONObject("retweetDetails"));
			}
		} catch (JSONException je) {
			throw new WeiboException(je.getMessage() + ":" + json.toString(), je);
		}
        
    }
    
    /* modify by sycheng add some field*/
    public Status(JSONObject json)throws WeiboException, JSONException{
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
			thumbnail_pic = photo.getThumbnail_pic();
			bmiddle_pic = photo.getBmiddle_pic();
			original_pic = photo.getOriginal_pic();
		} else {
			thumbnail_pic = "";
			bmiddle_pic = "";
			original_pic = "";
		}
        user = new User(json.getJSONObject("user"));
    }
    public Status(String str) throws WeiboException, JSONException {
        // StatusStream uses this constructor
        super();
        JSONObject json = new JSONObject(str);
        id = json.getString("id");
        text = json.getString("text");
        source = json.getString("source");
        createdAt = parseDate(json.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");

        inReplyToStatusId = getString("in_reply_to_status_id", json);
        inReplyToUserId = getString("in_reply_to_user_id", json);
        isFavorited = getBoolean("favorited", json);
        if(!json.isNull("photo")) {
			Photo photo = new Photo(json.getJSONObject("photo"));
			thumbnail_pic = photo.getThumbnail_pic();
			bmiddle_pic = photo.getBmiddle_pic();
			original_pic = photo.getOriginal_pic();
		} else {
			thumbnail_pic = "";
			bmiddle_pic = "";
			original_pic = "";
		}
        user = new User(json.getJSONObject("user"));
    }

    private void init(Response res, Element elem, Weibo weibo) throws
            WeiboException {
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

    /**
     * Return the created_at
     *
     * @return created_at
     * @since Weibo4J 1.1.0
     */

    public Date getCreatedAt() {
        return this.createdAt;
    }

    /**
     * Returns the id of the status
     *
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the text of the status
     *
     * @return the text
     */
    public String getText() {
        return this.text;
    }

    /**
     * Returns the source
     *
     * @return the source
     * @since Weibo4J 1.0.4
     */
    public String getSource() {
        return this.source;
    }


    /**
     * Test if the status is truncated
     *
     * @return true if truncated
     * @since Weibo4J 1.0.4
     */
    public boolean isTruncated() {
        return isTruncated;
    }

    /**
     * Returns the in_reply_tostatus_id
     *
     * @return the in_reply_tostatus_id
     * @since Weibo4J 1.0.4
     */
    public String getInReplyToStatusId() {
        return inReplyToStatusId;
    }

    /**
     * Returns the in_reply_user_id
     *
     * @return the in_reply_tostatus_id
     * @since Weibo4J 1.0.4
     */
    public String getInReplyToUserId() {
        return inReplyToUserId;
    }

    /**
     * Returns the in_reply_to_screen_name
     *
     * @return the in_in_reply_to_screen_name
     * @since Weibo4J 2.0.4
     */
    public String getInReplyToScreenName() {
        return inReplyToScreenName;
    }

    /**
     * returns The location's latitude that this tweet refers to.
     *
     * @since Weibo4J 2.0.10
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * returns The location's longitude that this tweet refers to.
     *
     * @since Weibo4J 2.0.10
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Test if the status is favorited
     *
     * @return true if favorited
     * @since Weibo4J 1.0.4
     */
    public boolean isFavorited() {
        return isFavorited;
    }

    public String getThumbnail_pic() {
		return thumbnail_pic;
	}

	public String getBmiddle_pic() {
		return bmiddle_pic;
	}

	public String getOriginal_pic() {
		return original_pic;
	}

	private User user = null;

    /**
     * Return the user
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     *
     * @since Weibo4J 2.0.10
     */
    public boolean isRetweet(){
        return null != retweetDetails;
    }

    /**
     *
     * @since Weibo4J 2.0.10
     */
    public RetweetDetails getRetweetDetails() {
        return retweetDetails;
    }


    /*package*/
    static List<Status> constructStatuses(Response res,
                                          Weibo weibo) throws WeiboException {
    	
    	 Document doc = res.asDocument();
        if (isRootNodeNilClasses(doc)) {
            return new ArrayList<Status>(0);
        } else {
            try {
                ensureRootNodeNameIs("statuses", doc);
                NodeList list = doc.getDocumentElement().getElementsByTagName(
                        "status");
                int size = list.getLength();
                List<Status> statuses = new ArrayList<Status>(size);
                for (int i = 0; i < size; i++) {
                    Element status = (Element) list.item(i);
                    statuses.add(new Status(res, status, weibo));
                }
                return statuses;
            } catch (WeiboException te) {
                ensureRootNodeNameIs("nil-classes", doc);
                return new ArrayList<Status>(0);
            }
        }
       
    }

    /*modify by sycheng add json call method*/
    /*package*/
    static List<Status> constructStatuses(Response res) throws WeiboException {
    	 try {
             JSONArray list = res.asJSONArray();
             int size = list.length();
             List<Status> statuses = new ArrayList<Status>(size);
             for (int i = 0; i < size; i++) {
                 statuses.add(new Status(list.getJSONObject(i)));
             }
             return statuses;
         } catch (JSONException jsone) {
             throw new WeiboException(jsone);
         } catch (WeiboException te) {
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
                ", thumbnail_pic=" + thumbnail_pic +
                ", bmiddle_pic=" + bmiddle_pic +
                ", original_pic=" + original_pic +
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
