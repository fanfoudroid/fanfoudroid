package com.ch_linghu.fanfoudroid.data.json;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.ch_linghu.fanfoudroid.data2.Photo;
import com.ch_linghu.fanfoudroid.data2.Status;
import com.ch_linghu.fanfoudroid.data2.User;

public class StatusJSON {
    
    public static Status parseToStatus(String jsonString)
            throws JsonParserException {
        return JsonParser.parseToObject(jsonString, mStatusJsonMap);
    }

    public static List<Status> parseToStatuses(String jsonString)
            throws JsonParserException {
        return JsonParser.parseToList(jsonString, mStatusJsonMap);
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
                    final JSONObject photoJson = json.getJSONObject("photo");
                    Photo photo = new Photo();
                    photo.setThumburl(photoJson.getString("thumburl"));
                    photo.setImageurl(photoJson.getString("imageurl"));
                    photo.setLargeurl(photoJson.getString("largeurl"));
                    status.setPhotoUrl(photo);
                }
                if (!json.isNull("user")) {
                    final JSONObject userJson = json.getJSONObject("user");
                    User user = new User();
                    user.setScreenName(userJson
                            .getString("in_reply_to_screen_name"));
                    status.setUser(user);
                }
                return status;
            } catch (JSONException e) {
                throw new JsonParserException("Cann't convert to JSONObject", e);
            }
        }

    };
}
