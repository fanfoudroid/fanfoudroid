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
package com.ch_linghu.fanfoudroid.fanfou;

import org.json.JSONException;
import org.json.JSONObject;

import com.ch_linghu.fanfoudroid.http.HttpException;

/**
 * A data class representing Basic user information element
 */
public class Photo extends WeiboResponse implements java.io.Serializable {

    private Weibo weibo;
    private String thumbnail_pic;
    private String bmiddle_pic;
    private String original_pic;
    private boolean verified;
    private static final long serialVersionUID = -6345893237975349030L;


    public Photo(JSONObject json) throws HttpException {
        super();
        init(json);
    }

    private void init(JSONObject json) throws HttpException {
        try {
        	//System.out.println(json);
        	thumbnail_pic = json.getString("thumburl");
			bmiddle_pic = json.getString("imageurl");
			original_pic = json.getString("largeurl");
        } catch (JSONException jsone) {
            throw new HttpException(jsone.getMessage() + ":" + json.toString(), jsone);
        }
    }

	public String getThumbnail_pic() {
		return thumbnail_pic;
	}

	public void setThumbnail_pic(String thumbnail_pic) {
		this.thumbnail_pic = thumbnail_pic;
	}

	public String getBmiddle_pic() {
		return bmiddle_pic;
	}

	public void setBmiddle_pic(String bmiddle_pic) {
		this.bmiddle_pic = bmiddle_pic;
	}

	public String getOriginal_pic() {
		return original_pic;
	}

	public void setOriginal_pic(String original_pic) {
		this.original_pic = original_pic;
	}

	@Override
	public String toString() {
		return "Photo [thumbnail_pic=" + thumbnail_pic + ", bmiddle_pic="
				+ bmiddle_pic + ", original_pic=" + original_pic + "]";
	}

  
}
