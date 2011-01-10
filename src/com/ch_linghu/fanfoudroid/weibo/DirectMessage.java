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
package com.ch_linghu.fanfoudroid.weibo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ch_linghu.fanfoudroid.http.Response;


/**
 * A data class representing sent/received direct message.
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class DirectMessage extends WeiboResponse implements java.io.Serializable {
    private String id;
    private String text;
    private String sender_id;
    private String recipient_id;
    private Date created_at;
    private String sender_screen_name;
    private String recipient_screen_name;
    private static final long serialVersionUID = -3253021825891789737L;

    /*package*/DirectMessage(Response res, Weibo weibo) throws WeiboException {
        super(res);
        init(res, res.asDocument().getDocumentElement(), weibo);
    }
    /*package*/DirectMessage(Response res, Element elem, Weibo weibo) throws WeiboException {
        super(res);
        init(res, elem, weibo);
    }
    /*modify by sycheng add json call*/
    /*package*/DirectMessage(JSONObject json) throws WeiboException {
        try {
        	
			id = json.getString("id");
			text = json.getString("text");
			sender_id = json.getString("sender_id");
			recipient_id = json.getString("recipient_id");
			created_at = parseDate(json.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
			sender_screen_name = json.getString("sender_screen_name");
			recipient_screen_name = json.getString("recipient_screen_name");
			
			if(!json.isNull("sender"))
				sender = new User(json.getJSONObject("sender"));
			if(!json.isNull("recipient"))
				recipient = new User(json.getJSONObject("recipient"));
		} catch (JSONException jsone) {
			throw new WeiboException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
        
    }
    
    private void init(Response res, Element elem, Weibo weibo) throws WeiboException{
        

    	ensureRootNodeNameIs("direct_message", elem);
        sender = new User(res, (Element) elem.getElementsByTagName("sender").item(0),
                weibo);
        recipient = new User(res, (Element) elem.getElementsByTagName("recipient").item(0),
                weibo);
        id = getChildString("id", elem);
        text = getChildText("text", elem);
        sender_id = getChildString("sender_id", elem);
        recipient_id = getChildString("recipient_id", elem);
        created_at = getChildDate("created_at", elem);
        sender_screen_name = getChildText("sender_screen_name", elem);
        recipient_screen_name = getChildText("recipient_screen_name", elem);

    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getSenderId() {
        return sender_id;
    }

    public String getRecipientId() {
        return recipient_id;
    }

    /**
     * @return created_at
     * @since Weibo4J 1.1.0
     */
    public Date getCreatedAt() {
        return created_at;
    }

    public String getSenderScreenName() {
        return sender_screen_name;
    }

    public String getRecipientScreenName() {
        return recipient_screen_name;
    }

    private User sender;

    public User getSender() {
        return sender;
    }

    private User recipient;

    public User getRecipient() {
        return recipient;
    }

    /*package*/
    static List<DirectMessage> constructDirectMessages(Response res,
                                                       Weibo weibo) throws WeiboException {
        Document doc = res.asDocument();
        if (isRootNodeNilClasses(doc)) {
            return new ArrayList<DirectMessage>(0);
        } else {
            try {
                ensureRootNodeNameIs("direct-messages", doc);
                NodeList list = doc.getDocumentElement().getElementsByTagName(
                        "direct_message");
                int size = list.getLength();
                List<DirectMessage> messages = new ArrayList<DirectMessage>(size);
                for (int i = 0; i < size; i++) {
                    Element status = (Element) list.item(i);
                    messages.add(new DirectMessage(res, status, weibo));
                }
                return messages;
            } catch (WeiboException te) {
                if (isRootNodeNilClasses(doc)) {
                    return new ArrayList<DirectMessage>(0);
                } else {
                    throw te;
                }
            }
        }
    }
    
    /*package*/
    static List<DirectMessage> constructDirectMessages(Response res
                                                       ) throws WeiboException {
    	JSONArray list=	 res.asJSONArray();
    	
            try {
                int size = list.length();
                List<DirectMessage> messages = new ArrayList<DirectMessage>(size);
                for (int i = 0; i < size; i++) {
                    
                    messages.add(new DirectMessage(list.getJSONObject(i)));
                }
                return messages;
            } catch (JSONException jsone) {
            	throw new WeiboException(jsone);
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
        return obj instanceof DirectMessage && ((DirectMessage) obj).id.equals(this.id);
    }

    @Override
    public String toString() {
        return "DirectMessage{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", sender_id=" + sender_id +
                ", recipient_id=" + recipient_id +
                ", created_at=" + created_at +
                ", sender_screen_name='" + sender_screen_name + '\'' +
                ", recipient_screen_name='" + recipient_screen_name + '\'' +
                ", sender=" + sender +
                ", recipient=" + recipient +
                '}';
    }
}
