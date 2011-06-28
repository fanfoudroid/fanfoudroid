package com.ch_linghu.fanfoudroid.data2;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.dao.StatusDAO;
import com.ch_linghu.fanfoudroid.fanfou.Paging;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.http.Response;

import android.content.Context;

public class StatusUtils {
    private static final String TAG = StatusUtils.class.getSimpleName();

    private Context context;
    private StatusDAO statusDAO;

    public StatusUtils(Context con) {
        context = con;
        statusDAO = new StatusDAO(con);
    }

    public boolean getNewUserTimeline(String authorId) {
        String maxStatusId = statusDAO
                .getMaxStatusIdByAuthorInXXStatuses(authorId);
        // 数据库没有该用户消息时
        if (maxStatusId.equals("")) {
            try {
                Response res = TwitterApplication.mApi.getNewUserTimeline(
                        authorId, new Paging(1, 20));
                JSONArray jsonList = res.asJSONArray();
                objects2DB(jsonList, new User(TwitterApplication.getMyselfId()),
                        Status.TYPE_XXSTATUSES);
            } catch (HttpException e) {
                e.printStackTrace();
            } catch (JSONException e) {
            } catch (ParseException e) {
            }
        }
        return false;
    }
    
    public List<Status> getMoreUserTimeline(String authorId, Paging paging) throws HttpException {
        List<Status> statusList = new ArrayList<Status>();
        //先判断数据库有没有连续的20-40条，有就取出来返回
        //如果没有，就去用max_id和since_id，Paging去API取page=1数据，入库（判断重复和连续性）
        //数据库取20-40条数据，返回
        return statusList;
    }

    public boolean objects2DB(JSONArray jsonList, User owner, int type)
            throws JSONException, ParseException {
        for (int i = 0; i < jsonList.length(); i++) {
            JSONObject jsonObject = jsonList.getJSONObject(i);
            Status status = json2Object(jsonObject);
            status.setOwner(owner);
            status.setType(type);
            
        }
        return false;
    }

    public Status json2Object(JSONObject jsonObject) throws JSONException,
            ParseException {
        Status status = new Status();
        status.setStatusId(jsonObject.getString("id"));
        status.setAuthor(new UserUtils(context).json2Object(jsonObject
                .getJSONObject("user")));
        status.setText(jsonObject.getString("text"));
        status.setSource(jsonObject.getString("source"));
        status.setCreatedAt(DataUtils.parseDate(
                jsonObject.getString("created_at"),
                "EEE MMM dd HH:mm:ss z yyyy"));
        status.setTruncated(DataUtils.getBoolean("truncated", jsonObject));
        status.setFavorited(DataUtils.getBoolean("favorited", jsonObject));
        if (!jsonObject.isNull("photo")) {
            Photo photo = new PhotoUtils(context).json2Object(jsonObject
                    .getJSONObject("photo"));
            status.setPhoto(photo);
        }
        status.setInReplyToStatusId(DataUtils.getString(
                "in_reply_to_status_id", jsonObject));
        status.setInReplyToUserId(DataUtils.getString("in_reply_to_user_id",
                jsonObject));
        status.setInReplyToScreenName(DataUtils.getString(
                "in_reply_to_screen_name", jsonObject));
        return status;
    }
}
