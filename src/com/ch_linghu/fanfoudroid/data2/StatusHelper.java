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

public class StatusHelper {
    private static final String TAG = StatusHelper.class.getSimpleName();

    private Context context;
    private StatusDAO statusDAO;
    
    public final static int MAX_NUM = 20;

    public StatusHelper(Context con) {
        context = con;
        statusDAO = new StatusDAO(con);
    }

    public boolean getNewUserTimeline(String authorId) {
        String maxStatusId = statusDAO
                .getMaxStatusId(TwitterApplication.getMyselfId(), Status.TYPE_USER, authorId);
        // 数据库没有该用户消息时
        if (maxStatusId.equals("")) {
            try {
                Response res = TwitterApplication.mApi.getNewUserTimeline(
                        authorId, new Paging(1, MAX_NUM));
                JSONArray jsonList = res.asJSONArray();
                //UserTimeLine的Owner设置为user本身，便于做连续性判断。
                objects2DB(jsonList, new User(authorId), Status.TYPE_USER);
            } catch (HttpException e) {
                e.printStackTrace();
            } catch (JSONException e) {
            } catch (ParseException e) {
            }
        } else {
            //通过since_id，Paging去API取page=1数据，入库（判断连续性）
        }
        return true;
    }
    
    public List<Status> getMoreUserTimeline(String authorId, int nextPage) throws HttpException {
        List<Status> statusList = new ArrayList<Status>();
        //先判断数据库有没有连续的20-40条，有就取出来返回
        //如果没有，就去用max_id和since_id，Paging去API取page=1数据，入库（判断连续性）
        //数据库取20-40条数据，返回
    	
//      int sequenceFlag = 0;
//    	if (jsonList.length() >= MAX_NUM){
//    		//可能中间有断层
//    		sequenceFlag = getCurrentSquenceFlag(owner, type);
//    	}else{
//    		sequenceFlag = getPrevSquenceFlag(owner, type);
//    	}

    	return statusList;
    }

    public boolean objects2DB(JSONArray jsonList, User owner, int type)
            throws JSONException, ParseException {
    	int sequenceFlag = 0;
    	if (jsonList.length() >= MAX_NUM){
    		//中间有断层
    		sequenceFlag = statusDAO.getNewSequenceFlag(owner.getId(), type);
    	}else{
    		sequenceFlag = statusDAO.getCurrentSequenceFlag(owner.getId(), type);
    	}
    	
        for (int i = 0; i < jsonList.length(); i++) {
            JSONObject jsonObject = jsonList.getJSONObject(i);
            Status status = json2Object(jsonObject);
            status.setOwner(owner);
            status.setType(type);
            
            statusDAO.insertOneStatus(status, sequenceFlag);
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
