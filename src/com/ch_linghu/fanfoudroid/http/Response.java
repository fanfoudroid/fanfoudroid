package com.ch_linghu.fanfoudroid.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.util.DebugTimer;

public class Response {
    private HttpResponse mResponse = null;
    private boolean mStreamConsumed = false;
    
    public Response(HttpResponse res) {
        mResponse = res;
    }

    public InputStream asStream() throws ResponseException {
        HttpEntity entity = mResponse.getEntity();
        //FIXME: entity 可能为空

        InputStream is = null;
        try {
            is = entity.getContent();
            Header ceheader = entity.getContentEncoding();
            if (ceheader != null
                    && ceheader.getValue().equalsIgnoreCase("gzip")) {
                is = new GZIPInputStream(is);
            }
        } catch (IllegalStateException e) {
            throw new ResponseException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ResponseException(e.getMessage(), e);
        }

        mResponse = null;
        return is;
    }

    public String asString() throws ResponseException {
    	if (TwitterApplication.DEBUG){
    		DebugTimer.betweenStart("AS STRING");
    	}

        String str = null;
        InputStream is = asStream();
        if (null == is) {
            return str;
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer buf = new StringBuffer();
            char[] buffer = new char[1024];
            while ((br.read(buffer)) != -1) {
                buf.append(buffer);
            }
            str = buf.toString();
        } catch (NullPointerException npe) {
            throw new ResponseException(npe.getMessage(), npe);
        } catch (IOException ioe) {
            throw new ResponseException(ioe.getMessage(), ioe);
        } finally {
            if (br != null) {
                try {
                    setStreamConsumed(true);
                    is.close();
                    br.close();
                } catch (IOException e) {
                    throw new ResponseException(e.getMessage(), e);
                }
            }
        }
        if(TwitterApplication.DEBUG){
        	DebugTimer.betweenEnd("AS STRING");
        }
        return str;
    }

    public JSONObject asJSONObject() throws ResponseException {
        try {
            return new JSONObject(asString());
        } catch (JSONException jsone) {
            throw new ResponseException(jsone.getMessage() + ":"
                    + asString(), jsone);
        }
    }

    public JSONArray asJSONArray() throws ResponseException {
        try {
            return new JSONArray(asString());
        } catch (Exception jsone) {
            throw new ResponseException(jsone.getMessage() + ":"
                    + asString(), jsone);
        }
    }

    private void setStreamConsumed(boolean mStreamConsumed) {
        this.mStreamConsumed = mStreamConsumed;
    }

    public boolean isStreamConsumed() {
        return mStreamConsumed;
    }

    /**
     * @deprecated
     * @return
     */
    public Document asDocument() {
        // TODO Auto-generated method stub
        return null;
    }

}
