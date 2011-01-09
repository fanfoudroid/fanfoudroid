package com.ch_linghu.android.fanfoudroid.task;

import java.util.HashMap;

import org.json.JSONException;

import com.ch_linghu.android.fanfoudroid.weibo.WeiboException;


/**
 * 暂未使用，待观察是否今后需要此类
 * @author lds
 *
 */
public class TaskParams {
	
	private HashMap<String, Object> params = null;
	
	public TaskParams() {
		params = new HashMap<String, Object>();
	}
	
	public TaskParams(String key, Object value) {
		this();
		put(key, value);
	}
	
	public void put(String key, Object value) {
		params.put(key, value);
	}
	
	public Object get(String key) {
		return params.get(key);
	}
	
    /**
     * Get the boolean value associated with a key.
     *
     * @param key   A key string.
     * @return      The truth.
     * @throws      WeiboException
     *  if the value is not a Boolean or the String "true" or "false".
     */
    public boolean getBoolean(String key) throws WeiboException {
        Object object = get(key);
        if (object.equals(Boolean.FALSE) ||
                (object instanceof String &&
                ((String)object).equalsIgnoreCase("false"))) {
            return false;
        } else if (object.equals(Boolean.TRUE) ||
                (object instanceof String &&
                ((String)object).equalsIgnoreCase("true"))) {
            return true;
        }
        throw new WeiboException(key + " is not a Boolean.");
    }


    /**
     * Get the double value associated with a key.
     * @param key   A key string.
     * @return      The numeric value.
     * @throws WeiboException if the key is not found or
     *  if the value is not a Number object and cannot be converted to a number.
     */
    public double getDouble(String key) throws WeiboException {
        Object object = get(key);
        try {
            return object instanceof Number ?
                ((Number)object).doubleValue() :
                Double.parseDouble((String)object);
        } catch (Exception e) {
            throw new WeiboException(key + " is not a number.");
        }
    }


    /**
     * Get the int value associated with a key. 
     *
     * @param key   A key string.
     * @return      The integer value.
     * @throws   WeiboException if the key is not found or if the value cannot
     *  be converted to an integer.
     */
    public int getInt(String key) throws WeiboException {
        Object object = get(key);
        try {
            return object instanceof Number ?
                ((Number)object).intValue() :
                Integer.parseInt((String)object);
        } catch (Exception e) {
            throw new WeiboException(key + " is not an int.");
        }
    }
    
    /**
     * Get the string associated with a key.
     *
     * @param key   A key string.
     * @return      A string which is the value.
     * @throws   JSONException if the key is not found.
     */
    public String getString(String key) throws WeiboException {
        Object object = get(key);
        return object == null ? null : object.toString();
    }


    /**
     * Determine if the JSONObject contains a specific key.
     * @param key   A key string.
     * @return      true if the key exists in the JSONObject.
     */
    public boolean has(String key) {
        return this.params.containsKey(key);
    }

}
