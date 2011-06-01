package com.ch_linghu.fanfoudroid.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JsonParser {

    public static <T> T parseToObject(JSONObject json, JsonMapper<T> jsonMap)
            throws JsonParserException {
        return jsonMap.mapRow(json);
    }

    public static <T> List<T> parseToList(JSONArray json, JsonMapper<T> jsonMap)
            throws JsonParserException {
        try {
            int size = json.length();
            List<T> list = new ArrayList<T>(size);
            for (int i = 0; i < size; i++) {
                list.add(jsonMap.mapRow(json.getJSONObject(i)));
            }
            return list;
        } catch (JSONException e) {
            throw new JsonParserException(e.getMessage(), e);
        }
    }
}
