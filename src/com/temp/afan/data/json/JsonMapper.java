package com.temp.afan.data.json;

import org.json.JSONObject;


public interface JsonMapper<T> {
    public T mapRow(JSONObject json) throws JsonParserException;
}