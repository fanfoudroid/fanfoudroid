package com.ch_linghu.fanfoudroid.data.json;

import org.json.JSONObject;

public interface JsonMapper<T> {
    public T mapRow(JSONObject json) throws JsonParserException;
}