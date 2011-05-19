package com.ch_linghu.fanfoudroid.data.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {

    public static <T> T parseToObject(String jsonString, JsonMapper<T> jsonMap)
            throws JsonParserException {
        try {
            return jsonMap.mapRow(new JSONObject(jsonString));
        } catch (JSONException e) {
            throw new JsonParserException(e.getMessage(), e);
        }
    }

    public static <T> List<T> parseToList(String jsonString, JsonMapper<T> jsonMap)
            throws JsonParserException {
        try {
            JSONArray json = new JSONArray(jsonString);
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
