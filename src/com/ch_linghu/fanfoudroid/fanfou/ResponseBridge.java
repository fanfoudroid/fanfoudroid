package com.ch_linghu.fanfoudroid.fanfou;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ch_linghu.fanfoudroid.data2.Photo;
import com.ch_linghu.fanfoudroid.data2.Status;
import com.ch_linghu.fanfoudroid.data2.User;
import com.ch_linghu.fanfoudroid.json.JsonMapper;
import com.ch_linghu.fanfoudroid.json.JsonParser;
import com.ch_linghu.fanfoudroid.json.JsonParserException;
import com.ch_linghu.fanfoudroid.json.JsonUtils;

// TODO: 这个部分还没想好
public class ResponseBridge {

    /**
     *
     */
    public static class JSON2Status {

        public static Status parserToStatus(JSONObject json)
                throws JsonParserException {
            return JsonParser.parseToObject(json, mStatusJsonMap);
        }

        public static List<Status> parserToStatuses(JSONArray json)
                throws JsonParserException {
            return JsonParser.parseToList(json, mStatusJsonMap);
        }

        private static final JsonMapper<Status> mStatusJsonMap = new JsonMapper<Status>() {

            @Override
            public Status mapRow(JSONObject json) throws JsonParserException {
                try {
                    Status status = new Status();
                    status.setId(json.getString("id"));
                    status.setText(json.getString("text"));
                    status.setSource(json.getString("source"));
                    status.setCreatedAt(JsonUtils.parseDate(
                            json.getString("created_at"),
                            "EEE MMM dd HH:mm:ss z yyyy"));
                    status.setFavorited(JsonUtils.getBoolean("favorited", json));
                    status.setTruncated(JsonUtils.getBoolean("truncated", json));
                    status.setInReplyToStatusId(JsonUtils.getString(
                            "in_reply_to_status_id", json));
                    status.setInReplyToUserId(JsonUtils.getString(
                            "in_reply_to_user_id", json));
                    status.setInReplyToScreenName(json
                            .getString("in_reply_to_screen_name"));
                    if (!json.isNull("photo")) {
                        final JSONObject photoJson = json
                                .getJSONObject("photo");
                        // TODO: 考虑是使用Photo实例还是使用三个字段
                        Photo photo = new Photo();
                        photo.setThumburl(photoJson.getString("thumburl"));
                        photo.setImageurl(photoJson.getString("imageurl"));
                        photo.setLargeurl(photoJson.getString("largeurl"));
                        status.setPhotoUrl(photo);
                    }
                    if (!json.isNull("user")) {
                        final JSONObject userJson = json.getJSONObject("user");
                        // TODO:
                        User user = new User();
                        user.setScreenName(userJson
                                .getString("in_reply_to_screen_name"));
                        status.setUser(user);
                    }
                    return status;
                } catch (JSONException e) {
                    throw new JsonParserException(
                            "Cann't convert to JSONObject", e);
                }
            }
        };

    }

}
