package com.ch_linghu.fanfoudroid.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import android.util.Log;

import com.ch_linghu.fanfoudroid.data.json.JsonParser;
import com.ch_linghu.fanfoudroid.data.json.JsonParserException;
import com.ch_linghu.fanfoudroid.data2.Status;
import com.ch_linghu.fanfoudroid.debug.DebugTimer;

public class Response {
    private static boolean DEBUG = true;
    private HttpResponse mResponse = null;
    private boolean mStreamConsumed = false;
    
    public Response(HttpResponse res) {
        mResponse = res;
    }

    public InputStream asStream() throws ResponseException {
        HttpEntity entity = mResponse.getEntity();
        
        /* copy response content for debug
        if (DEBUG && entity != null) {
            try {
                entity = new BufferedHttpEntity(entity);
                debugEntity(entity); // copy 
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        */

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
    
    // ignore me, it's only for debug
    private void debugEntity(HttpEntity entity) throws IOException,
            JsonParserException {
        InputStream is = entity.getContent();
        Header ceheader = entity.getContentEncoding();
        if (ceheader != null && ceheader.getValue().equalsIgnoreCase("gzip")) {
            is = new GZIPInputStream(is);
        }

        JsonParser jsonParser = new JsonParser();
        List<Status> statuses = jsonParser.parseToStatuses(is);
        DebugTimer.betweenEnd("GSON");
        Log.v("DEBUG", "Parser statuses :" + statuses.size());
        /*
        for (Status s : statuses) {
            Log.v("DEBUG", s.toString());
        }
        */
    }

}
