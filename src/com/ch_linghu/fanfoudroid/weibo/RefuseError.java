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
 * 服务器响应的错误信息
 */
public class RefuseError extends WeiboResponse implements java.io.Serializable {
    
    // TODO: get error type
    public static final int ERROR_A = 1;
    public static final int ERROR_B = 1;
    public static final int ERROR_C = 1;
    
    private int mErrorCode = -1;
    private String mRequestUrl = "";
    private String mResponseError = "";
    
    private static final long serialVersionUID = -2105422180879273058L;
    
    public RefuseError(Response res) throws WeiboException {
        this(res.asJSONObject());
    }

    public RefuseError(JSONObject json) throws WeiboException {
        try {
           mRequestUrl = json.getString("request");
           mResponseError = json.getString("error");
           parseError(mResponseError);
       } catch (JSONException je) {
           throw new WeiboException(je.getMessage() + ":" + json.toString(), je);
       }
    }
    
    private void parseError(String error) {
        if (error.equals("")) {
            mErrorCode = ERROR_A;
        }
    }
    
    public int getErrorCode() {
        return mErrorCode;
    }

    public String getRequestUrl() {
        return mRequestUrl;
    }

    public String getMessage() {
        return mResponseError;
    }
}