/*
Copyright (c) 2007-2009, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package tk.sandin.android.fanfoudroid.weibo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import tk.sandin.android.fanfoudroid.http.HttpClient;
import tk.sandin.android.fanfoudroid.http.Response;

import com.ch_linghu.android.fanfoudroid.R;

public class Weibo extends WeiboSupport implements java.io.Serializable {
	public static final String CONSUMER_KEY = Configuration.getSource();
	public static final String CONSUMER_SECRET = "";
	
	private String baseURL = Configuration.getScheme() + "api.fanfou.com/";
	private String searchBaseURL = Configuration.getScheme() + "api.fanfou.com/";
    private static final long serialVersionUID = -1486360080128882436L;

    public Weibo(String userId, String password) {
        super(userId, password);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public Weibo(String userId, String password, String baseURL) {
        this(userId, password);
        this.baseURL = baseURL;
    }

    /**
     * Sets the base URL
     *
     * @param baseURL String the base URL
     */
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    /**
     * Returns the base URL
     *
     * @return the base URL
     */
    public String getBaseURL() {
        return this.baseURL;
    }

    /**
     * Sets the search base URL
     *
     * @param searchBaseURL the search base URL
     * @since fanfoudroid 0.5.0
     */
    public void setSearchBaseURL(String searchBaseURL) {
        this.searchBaseURL = searchBaseURL;
    }

    /**
     * Returns the search base url
     * @return search base url
     * @since fanfoudroid 0.5.0
     */
    public String getSearchBaseURL(){
        return this.searchBaseURL;
    }
    
    /**
     * Returns authenticating userid
     *
     * @return userid
     */
    public String getUserId() {
        return http.getUserId();
    }
    
    /**
     * Returns authenticating password
     *
     * @return password
     */
    public String getPassword() {
        return http.getPassword();
    }
    
    /**
     * Issues an HTTP GET request.
     *
     * @param url          the request url
     * @param authenticate if true, the request will be sent with BASIC authentication header
     * @return the response
     * @throws WeiboException when Weibo service or network is unavailable
     */

    protected Response get(String url, boolean authenticate) throws WeiboException {
        return get(url, null, authenticate);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url          the request url
     * @param authenticate if true, the request will be sent with BASIC authentication header
     * @param name1        the name of the first parameter
     * @param value1       the value of the first parameter
     * @return the response
     * @throws WeiboException when Weibo service or network is unavailable
     */

    protected Response get(String url, String name1, String value1, boolean authenticate) throws WeiboException {
    	ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
    	params.add( new BasicNameValuePair(name1, HttpClient.encode(value1) ) );
        return get(url, params, authenticate);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url          the request url
     * @param name1        the name of the first parameter
     * @param value1       the value of the first parameter
     * @param name2        the name of the second parameter
     * @param value2       the value of the second parameter
     * @param authenticate if true, the request will be sent with BASIC authentication header
     * @return the response
     * @throws WeiboException when Weibo service or network is unavailable
     */

    protected Response get(String url, String name1, String value1, String name2, String value2, boolean authenticate) throws WeiboException {
    	ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
    	params.add(new BasicNameValuePair(name1, HttpClient.encode(value1)));
    	params.add(new BasicNameValuePair(name2, HttpClient.encode(value2)));
        return get(url, params, authenticate);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url          the request url
     * @param params       the request parameters
     * @param authenticate if true, the request will be sent with BASIC authentication header
     * @return the response
     * @throws WeiboException when Weibo service or network is unavailable
     */
    protected Response get(String url, ArrayList<BasicNameValuePair> params, boolean authenticated) throws WeiboException {
		if (url.indexOf("?") == -1) {
			url += "?source=" + CONSUMER_KEY;
		} else if (url.indexOf("source") == -1) {
			url += "&source=" + CONSUMER_KEY;
		}
    	if (null != params && params.size() > 0) {
			url += "&" + HttpClient.encodeParameters(params);
		}
        return http.get(url, authenticated);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url          the request url
     * @param params       the request parameters
     * @param paging controls pagination
     * @param authenticate if true, the request will be sent with BASIC authentication header
     * @return the response
     * @throws WeiboException when Weibo service or network is unavailable
     */
    protected Response get(String url, ArrayList<BasicNameValuePair> params, Paging paging, boolean authenticate) throws WeiboException {
    	if (null == params) {
    		params = new ArrayList<BasicNameValuePair>();
    	}
    	
        if (null != paging) {
            if ("" != paging.getMaxId()) {
                params.add(new BasicNameValuePair("max_id", String.valueOf(paging.getMaxId())));
            }
            if ("" != paging.getSinceId()) {
                params.add(new BasicNameValuePair("since_id", String.valueOf(paging.getSinceId())));
            }
            if (-1 != paging.getPage()) {
                params.add(new BasicNameValuePair("page", String.valueOf(paging.getPage())));
            }
            if (-1 != paging.getCount()) {
                params.add(new BasicNameValuePair("count", String.valueOf(paging.getCount())));
            }
            
            return get(url, params, authenticate);
        } else {
            return get(url, params, authenticate);
        }
    }
    
    /**
     * 生成POST Parameters助手
     * @param nameValuePair 参数(一个或多个)
     * @return post parameters
     */
    public ArrayList<BasicNameValuePair> createParams(BasicNameValuePair... nameValuePair ) {
    	ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
    	for (BasicNameValuePair param : nameValuePair) {
    		params.add(param);
    	}
    	return params;
    }
    
    /***************** API METHOD START *********************/
    
    /* 搜索相关的方法 */

    /**
     * Returns tweets that match a specified query.
     * <br>This method calls http://api.fanfou.com/users/search.format
     * @param query - the search condition
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public QueryResult search(Query query) throws WeiboException {
        try{
        	return new QueryResult(get(searchBaseURL + "search/public_timeline.json", query.asPostParameters(), false), this);
        }catch(WeiboException te){
            if(404 == te.getStatusCode()){
                return new QueryResult(query);
            }else{
                throw te;
            }
        }
    }

    /**
     * Returns the top ten topics that are currently trending on Weibo.  The response includes the time of the request, the name of each trend.
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     */
    public Trends getTrends() throws WeiboException {
        return Trends.constructTrends(get(searchBaseURL + "trends.json", false));
    }

    private String toDateStr(Date date){
        if(null == date){
            date = new Date();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }


    /* 消息相关的方法 */

    /**
     * Returns the 20 most recent statuses from non-protected users who have set a custom user icon.
     * <br>This method calls http://api.fanfou.com/statuses/public_timeline.format
     *
     * @return list of statuses of the Public Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<Status> getPublicTimeline() throws
            WeiboException {
        return Status.constructStatuses(get(getBaseURL() +
                "statuses/public_timeline.json", true));
    }

    public RateLimitStatus getRateLimitStatus()throws
            WeiboException {
        return new RateLimitStatus(get(getBaseURL() +
                "account/rate_limit_status.json", true),this);
    }

    /**
     * Returns the 20 most recent statuses, including retweets, posted by the authenticating user and that user's friends. This is the equivalent of /timeline/home on the Web.
     * <br>This method calls http://api.fanfou.com/statuses/home_timeline.format
     *
     * @return list of the home Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     * @since fanfoudroid 0.5.0
     */
    public List<Status> getHomeTimeline() throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/home_timeline.json", true));
    }


    /**
     * Returns the 20 most recent statuses, including retweets, posted by the authenticating user and that user's friends. This is the equivalent of /timeline/home on the Web.
     * <br>This method calls  http://api.fanfou.com/statuses/home_timeline.format
     *
     * @param paging controls pagination
     * @return list of the home Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     * @since fanfoudroid 0.5.0
     */
    public List<Status> getHomeTimeline(Paging paging) throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/home_timeline.json", null, paging, true));
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating1 user and that user's friends.
     * It's also possible to request another user's friends_timeline via the id parameter below.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<Status> getFriendsTimeline() throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.json", true));
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @param paging controls pagination
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<Status> getFriendsTimeline(Paging paging) throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.json",null, paging, true));
    }

    
    /**
     * Returns friend time line by page and count.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     * @param page
     * @param count
     * @return
     * @throws WeiboException
     */
    public List<Status> getFriendsTimeline(int page, int count) throws
    		WeiboException {
    	Paging paging = new Paging(page, count);
    	return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.json",null, paging, true));
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/user_timeline.format
     *
     * @param id    specifies the ID or screen name of the user for whom to return the user_timeline
     * @param paging controls pagenation
     * @return list of the user Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<Status> getUserTimeline(String id, Paging paging)
            throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + id + ".xml",
                null, paging, http.isAuthenticationEnabled()), this);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/user_timeline.format
     *
     * @param id specifies the ID or screen name of the user for whom to return the user_timeline
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<Status> getUserTimeline(String id) throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + id + ".json", http.isAuthenticationEnabled()));
    }


    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.fanfou.com/statuses/user_timeline.format
     *
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<Status> getUserTimeline() throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.json"
                , true));
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.fanfou.com/statuses/user_timeline.format
     *
     * @param paging controls pagination
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     * @since fanfoudroid 0.5.0
     */
    public List<Status> getUserTimeline(Paging paging) throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.json"
                , null, paging, true));
    }

    public List<Status> getUserTimeline(int page, int count) throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.json"
                , null, new Paging(page, count), true));
    }

    /**
     * Returns the 20 most recent mentions (status containing @username) for the authenticating user.
     * <br>This method calls http://api.fanfou.com/statuses/mentions.format
     *
     * @return the 20 most recent replies
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<Status> getMentions() throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/mentions.json",
                null, true));
    }
    
    // by since_id
    public List<Status> getMentions(String since_id) throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/mentions.json",
                "since_id", String.valueOf(since_id), true));
    }

    /**
     * Returns the 20 most recent mentions (status containing @username) for the authenticating user.
     * <br>This method calls http://api.fanfou.com/statuses/mentions.format
     *
     * @param paging controls pagination
     * @return the 20 most recent replies
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<Status> getMentions(Paging paging) throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/mentions.json",
                null, paging, true));
    }


    /**
     * Returns a single status, specified by the id parameter. The status's author will be returned inline.
     * <br>This method calls http://api.fanfou.com/statuses/show/id.format
     *
     * @param id the numerical ID of the status you're trying to retrieve
     * @return a single status
     * @throws WeiboException when Weibo service or network is unavailable. 可能因为“你没有通过这个用户的验证“,返回403
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public Status showStatus(String id) throws WeiboException {
    	return new Status(get(getBaseURL() + "statuses/show/" + id + ".json", true));
    }

    /**
     * Updates the user's status.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * <br>This method calls http://api.fanfou.com/statuses/update.format
     *
     * @param status the text of your status update
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public Status updateStatus(String status) throws WeiboException{
    	return new Status(http.post(getBaseURL() + "statuses/update.json",
    			createParams(new BasicNameValuePair("status", status))));
    }
    
    /**
     * Updates the user's status.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * <br>发布消息  http://api.fanfou.com/statuses/update.[json|xml] 
     *
     * @param status    the text of your status update
     * @param latitude  The location's latitude that this tweet refers to.
     * @param longitude The location's longitude that this tweet refers to.
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     * @since fanfoudroid 0.5.0
     */
    public Status updateStatus(String status, double latitude, double longitude) throws WeiboException, JSONException {
        return new Status(http.post(getBaseURL() + "statuses/update.json",
        		createParams(new BasicNameValuePair("status", status),
        					 new BasicNameValuePair("location", latitude + "," + longitude))));
    }

    /**
     * Updates the user's status.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * <br>This method calls http://api.fanfou.com/statuses/update.format
     *
     * @param status            the text of your status update
     * @param inReplyToStatusId The ID of an existing status that the status to be posted is in reply to.  This implicitly sets the in_reply_to_user_id attribute of the resulting status to the user ID of the message being replied to.  Invalid/missing status IDs will be ignored.
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     * @deprecated Use updateStatus(String status, long inReplyToStatusId) instead
     */
    public Status update(String status, String inReplyToStatusId) throws WeiboException {
        return updateStatus(status, inReplyToStatusId);
    }

    /**
     * Updates the user's status.
     * 如果要使用inreplyToStatusId参数, 那么该status就必须得是@别人的.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * <br>发布消息  http://api.fanfou.com/statuses/update.[json|xml] 
     *
     * @param status            the text of your status update
     * @param inReplyToStatusId The ID of an existing status that the status to be posted is in reply to.  This implicitly sets the in_reply_to_user_id attribute of the resulting status to the user ID of the message being replied to.  Invalid/missing status IDs will be ignored.
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     */
    public Status updateStatus(String status, String inReplyToStatusId) throws WeiboException {
         return new Status(http.post(getBaseURL() + "statuses/update.json",
        		 createParams(new BasicNameValuePair("status", status),
        			new BasicNameValuePair("in_reply_to_status_id", inReplyToStatusId))));
    }

    /**
     * Updates the user's status.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * <br>发布消息  http://api.fanfou.com/statuses/update.[json|xml] 
     *
     * @param status            the text of your status update
     * @param inReplyToStatusId The ID of an existing status that the status to be posted is in reply to.  This implicitly sets the in_reply_to_user_id attribute of the resulting status to the user ID of the message being replied to.  Invalid/missing status IDs will be ignored.
     * @param latitude          The location's latitude that this tweet refers to.
     * @param longitude         The location's longitude that this tweet refers to.
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     */
    public Status updateStatus(String status, String inReplyToStatusId
            , double latitude, double longitude) throws WeiboException {
        return new Status(http.post(getBaseURL() + "statuses/update.json",
        		createParams(new BasicNameValuePair("status", status),
   					new BasicNameValuePair("location", latitude + "," + longitude),
   					new BasicNameValuePair("in_reply_to_status_id", inReplyToStatusId))));
    }
    
    /**
     * upload the photo.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * The image suport.
     * <br>上传照片  http://api.fanfou.com/photos/upload.[json|xml]
     *
     * @param status the text of your status update
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     */
    public Status uploadPhoto(String status, File file) throws WeiboException {
        return new Status(http.httpRequest(getBaseURL() + "photos/upload.json",
        		createParams(new BasicNameValuePair("status", status)), file, true, http.METHOD_POST));
    }

    public Status uploadStatus(String status, File file) throws WeiboException {
    	return uploadPhoto(status, file);
    }

    /**
     * Destroys the status specified by the required ID parameter.  The authenticating user must be the author of the specified status.
     * <br>删除消息  http://api.fanfou.com/statuses/destroy.[json|xml] 
     *
     * @param statusId The ID of the status to destroy.
     * @return the deleted status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since 1.0.5
     */
    public Status destroyStatus(String statusId) throws WeiboException {
        return new Status(http.post(getBaseURL() + "statuses/destroy/" + statusId + ".json",
                createParams(), true));
    }

    /**
     * Returns extended information of a given user, specified by ID or screen name as per the required id parameter below.  This information includes design settings, so third party developers can theme their widgets according to a given user's preferences.
     * <br>This method calls http://api.fanfou.com/users/show.format
     *
     * @param id (cann't be screenName the ID of the user for whom to request the detail
     * @return User
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     * @since fanfoudroid 0.5.0
     */
    public User showUser(String id) throws WeiboException {
         return new User(get(getBaseURL() + "users/show.json",
        	createParams(new BasicNameValuePair("id", id)), true));
    }
    
    /**
     * Return a status of repost
     * @param to_user_name repost status's user name
     * @param repost_status_id repost status id 
     * @param repost_status_text repost status text 
     * @param new_status the new status text 
     * @return a single status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     */
    public Status repost(String to_user_name, String repost_status_id, 
                String repost_status_text, String new_status) throws WeiboException {
        StringBuilder sb = new StringBuilder();
        sb.append(new_status);
        sb.append(" ");
        sb.append(R.string.retweet + "：@");
        sb.append(to_user_name);
        sb.append(" ");
        sb.append(repost_status_text);
        sb.append(" ");
        String message = sb.toString();
        return new Status(http.post(getBaseURL() + "statuses/update.json",
        		createParams(new BasicNameValuePair("status", message),
        				new BasicNameValuePair("repost_status_id", repost_status_id)), true));
    }
    
    /**
     * Return a status of repost
     * @param repost_status_id repost status id 
     * @param repost_status_text repost status text 
     * @return a single status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     */
    public Status repost(String repost_status_id, String new_status) throws WeiboException {
        Status repost_to = showStatus(repost_status_id);
        String to_user_name = repost_to.getUser().getName();
        String repost_status_text = repost_to.getText();
        
        return repost(to_user_name, repost_status_id, repost_status_text, new_status);
    }
    
    /* User Methods */
    
    /**
     * Returns the specified user's friends, each with current status inline.
     * <br>This method calls http://api.fanfou.com/statuses/friends.format
     *
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     * @since fanfoudroid 0.5.0
     */
    public List<User> getFriendsStatuses() throws WeiboException {
        return User.constructResult(get(getBaseURL() + "users/friends.json", true));
    }
    
    /**
     * Returns the specified user's friends, each with current status inline.
     * <br>This method calls http://api.fanfou.com/statuses/friends.format
     *
     * @param paging controls pagination
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<User> getFriendsStatuses(Paging paging) throws WeiboException {
        return User.constructUsers(get(getBaseURL() + "users/friends.json", null,
                paging, true));
    }
    
    /**
     * Returns the user's friends, each with current status inline.
     * <br>This method calls http://api.fanfou.com/statuses/friends.format
     *
     * @param id the ID or screen name of the user for whom to request a list of friends
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     * @since fanfoudroid 0.5.0
     */
    public List<User> getFriendsStatuses(String id) throws WeiboException {
        return User.constructUsers(get(getBaseURL() + "users/friends.json", 
        		createParams(new BasicNameValuePair("id", id)), false));
    }
    
    /**
     * Returns the user's friends, each with current status inline.
     * <br>This method calls http://api.fanfou.com/statuses/friends.format
     *
     * @param id the ID or screen name of the user for whom to request a list of friends
     * @param paging controls pagination (饭否API 默认返回 100 条/页)
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<User> getFriendsStatuses(String id, Paging paging) throws WeiboException {
        return User.constructUsers(get(getBaseURL() + "users/friends.json", 
        		createParams(new BasicNameValuePair("id", id)), paging, false));
    }
    
    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.fanfou.com/statuses/followers.format
     *
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     * @since fanfoudroid 0.5.0
     */
    public List<User> getFollowersStatuses() throws WeiboException {
        return User.constructResult(get(getBaseURL() + "statuses/followers.json", true));
    }


    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.fanfou.com/statuses/followers.format
     *
     * @param paging controls pagination
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<User> getFollowersStatuses(Paging paging) throws WeiboException {
        return User.constructUsers(get(getBaseURL() + "statuses/followers.json", null
                , paging, true));
    }
    
    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.fanfou.com/statuses/followers.format
     *
     * @param id The ID (not screen name) of the user for whom to request a list of followers.
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<User> getFollowersStatuses(String id) throws WeiboException {
         return User.constructUsers(get(getBaseURL() + "statuses/followers/" + id + ".json", true));
    }
    
    /**
     * Returns the authenticating user's followers, each with current status inline. They are ordered by the order in which they joined Weibo (this is going to be changed).
     * <br>This method calls http://api.fanfou.com/statuses/followers.format
     *
     * @param id   The ID or screen name of the user for whom to request a list of followers.
     * @param paging controls pagination
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<User> getFollowersStatuses(String id, Paging paging) throws WeiboException {
        return User.constructUsers(get(getBaseURL() + "statuses/followers/" + id +
                ".json", null, paging, true));
    }
    
    /* 私信功能 */
    
    /**
     * Sends a new direct message to the specified user from the authenticating user.  Requires both the user and text parameters below.
     * The text will be trimed if the length of the text is exceeding 140 characters.
     * <br>This method calls http://api.fanfou.com/direct_messages/new.format
     * <br>通过客户端只能给互相关注的人发私信
     * 
     * @param id   the ID of the user to whom send the direct message
     * @param text String
     * @return DirectMessage
     * @throws WeiboException when Weibo service or network is unavailable
       @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public DirectMessage sendDirectMessage(String id, String text) throws WeiboException {
        return new DirectMessage(http.post(getBaseURL() + "direct_messages/new.json",
        				createParams(new BasicNameValuePair("user", id), 
        						new BasicNameValuePair("text", text))).asJSONObject());
    }

    //TODO: need be unit tested by in_reply_to_id.
    /**
     * Sends a new direct message to the specified user from the authenticating user.  Requires both the user and text parameters below.
     * The text will be trimed if the length of the text is exceeding 140 characters.
     * <br>通过客户端只能给互相关注的人发私信
     * 
     * @param id
     * @param text
     * @param in_reply_to_id
     * @return
     * @throws WeiboException
     */
    public DirectMessage sendDirectMessage(String id, String text, String in_reply_to_id)
                                    throws WeiboException {
        return new DirectMessage(http.post(getBaseURL() + "direct_messages/new.json",
    			createParams(new BasicNameValuePair("user", id), 
    					new BasicNameValuePair("text", text),
    					new BasicNameValuePair("is_reply_to_id", in_reply_to_id))).asJSONObject());
        }

    /**
     * Destroys the direct message specified in the required ID parameter.  The authenticating user must be the recipient of the specified direct message.
     * <br>This method calls http://api.fanfou.com/direct_messages/destroy/id.format
     *
     * @param id the ID of the direct message to destroy
     * @return the deleted direct message
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     * @since fanfoudroid 0.5.0
     */
    public DirectMessage destroyDirectMessage(String id) throws
            WeiboException {
        return new DirectMessage(http.post(getBaseURL() +
                "direct_messages/destroy/" + id + ".json", true).asJSONObject());
    }
    
    /**
     * Returns a list of the direct messages sent to the authenticating user.
     * <br>This method calls http://api.fanfou.com/direct_messages.format
     *
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<DirectMessage> getDirectMessages() throws WeiboException {

        return DirectMessage.constructDirectMessages(get(getBaseURL() + "direct_messages.json", true));
    }

    /**
     * Returns a list of the direct messages sent to the authenticating user.
     * <br>This method calls http://api.fanfou.com/direct_messages.format
     *
     * @param paging controls pagination
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<DirectMessage> getDirectMessages(Paging paging) throws WeiboException {
        return DirectMessage.constructDirectMessages(get(getBaseURL()
                + "direct_messages.json", null, paging, true));
    }
    
    /**
     * Returns a list of the direct messages sent by the authenticating user.
     * <br>This method calls http://api.fanfou.com/direct_messages/sent.format
     *
     * @return List
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<DirectMessage> getSentDirectMessages() throws
            WeiboException {
         return DirectMessage.constructDirectMessages(get(getBaseURL() +
                 "direct_messages/sent.json", null, true));
    }

    /**
     * Returns a list of the direct messages sent by the authenticating user.
     * <br>This method calls http://api.fanfou.com/direct_messages/sent.format
     *
     * @param paging controls pagination
     * @return List 默认返回20条, 一次最多返回60条 
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<DirectMessage> getSentDirectMessages(Paging paging) throws
            WeiboException {
         return DirectMessage.constructDirectMessages(get(getBaseURL() +
                 "direct_messages/sent.json", null, paging, true));
    }
    
    /* 收藏功能 */
    
    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     * @return List<Status>
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     * @since fanfoudroid 0.5.0
     */
    public List<Status> getFavorites() throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "favorites.json", createParams(), true));
    }
    
    public List<Status> getFavorites(Paging paging) throws WeiboException {
    return Status.constructStatuses(get(getBaseURL() + "favorites.json", createParams(), paging, true));
  }
    
    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     *
     * @param page the number of page
     * @return List<Status>
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     * @since fanfoudroid 0.5.0
     */
    public List<Status> getFavorites(int page) throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "favorites.json", "page", String.valueOf(page), true));
    }
    
    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     *
     * @param id the ID or screen name of the user for whom to request a list of favorite statuses
     * @return List<Status>
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     * @since fanfoudroid 0.5.0
     */
    public List<Status> getFavorites(String id) throws WeiboException {
         return Status.constructStatuses(get(getBaseURL() + "favorites/" + id + ".json", createParams(), true));
    }

    /**
     * Returns the 20 most recent favorite statuses for the authenticating user or user specified by the ID parameter in the requested format.
     *
     * @param id   the ID or screen name of the user for whom to request a list of favorite statuses
     * @param page the number of page
     * @return List<Status>
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public List<Status> getFavorites(String id, int page) throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "favorites/" + id + ".json", "page", String.valueOf(page), true));
    }
    
    public List<Status> getFavorites(String id, Paging paging) throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "favorites/" + id + ".json", null, paging, true));
    }

    /**
     * Favorites the status specified in the ID parameter as the authenticating user.  Returns the favorite status when successful.
     *
     * @param id the ID of the status to favorite
     * @return Status
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public Status createFavorite(String id) throws WeiboException {
        return new Status(http.post(getBaseURL() + "favorites/create/" + id + ".json", true));
    }

    /**
     * Un-favorites the status specified in the ID parameter as the authenticating user.  Returns the un-favorited status in the requested format when successful.
     *
     * @param id the ID of the status to un-favorite
     * @return Status
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public Status destroyFavorite(String id) throws WeiboException {
        return new Status(http.post(getBaseURL() + "favorites/destroy/" + id + ".json", true));
    }
    
    /**
     * Enables notifications for updates from the specified user to the authenticating user.  Returns the specified user when successful.
     * @param id String
     * @return User
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @deprecated 饭否该功能暂时关闭, 等待该功能开放.
     */
    public User enableNotification(String id) throws WeiboException {
        return new User(http.post(getBaseURL() + "notifications/follow/" + id + ".json", true).asJSONObject());
    }

    /**
     * Disables notifications for updates from the specified user to the authenticating user.  Returns the specified user when successful.
     * @param id String
     * @return User
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated 饭否该功能暂时关闭, 等待该功能开放.
     * @since fanfoudroid 0.5.0
     */
    public User disableNotification(String id) throws WeiboException {
        return new User(http.post(getBaseURL() + "notifications/leave/" + id + ".json", true).asJSONObject());
    }
   
    
    /* 黑名单 */

    /**
     * Blocks the user specified in the ID parameter as the authenticating user.  Returns the blocked user in the requested format when successful.
     * @param id the ID or screen_name of the user to block
     * @return the blocked user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     */
    public User createBlock(String id) throws WeiboException {
        return new User(http.post(getBaseURL() + "blocks/create/" + id + ".json", true).asJSONObject());
    }
    
    /**
     * Un-blocks the user specified in the ID parameter as the authenticating user.  Returns the un-blocked user in the requested format when successful.
     * @param id the ID or screen_name of the user to block
     * @return the unblocked user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     */
    public User destroyBlock(String id) throws WeiboException {
        return new User(http.post(getBaseURL() + "blocks/destroy/" + id + ".json", true).asJSONObject());
    }
    
    /**
     * Tests if a friendship exists between two users.
     * @param id The ID or screen_name of the potentially blocked user.
     * @return  if the authenticating user is blocking a target user
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated 饭否暂无此功能, 期待此功能
     * @since fanfoudroid 0.5.0
     */
    public boolean existsBlock(String id) throws WeiboException {
        try{
            return -1 == get(getBaseURL() + "blocks/exists/" + id + ".json", true).
                    asString().indexOf("<error>You are not blocking this user.</error>");
        }catch(WeiboException te){
            if(te.getStatusCode() == 404){
                return false;
            }
            throw te;
        }
    }

    /**
     * Returns a list of user objects that the authenticating user is blocking.
     * @return a list of user objects that the authenticating user
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated 饭否暂无此功能, 期待此功能
     * @since fanfoudroid 0.5.0
     */
    public List<User> getBlockingUsers() throws
            WeiboException {
        return User.constructUsers(get(getBaseURL() +
                "blocks/blocking.json", true));
    }

    /**
     * Returns a list of user objects that the authenticating user is blocking.
     * @param page the number of page
     * @return a list of user objects that the authenticating user
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated 饭否暂无此功能, 期待此功能
     * @since fanfoudroid 0.5.0
     */
    public List<User> getBlockingUsers(int page) throws
            WeiboException {
        return User.constructUsers(get(getBaseURL() +
                "blocks/blocking.json?page=" + page, true));
    }

    /**
     * Returns an array of numeric user ids the authenticating user is blocking.
     * @return Returns an array of numeric user ids the authenticating user is blocking.
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated 饭否暂无此功能, 期待此功能
     * @since fanfoudroid 0.5.0
     */
    public IDs getBlockingUsersIDs() throws WeiboException {
        return new IDs(get(getBaseURL() + "blocks/blocking/ids.json", true),this);
    }
    
    /* 好友关系方法 */
    
    /**
     * Tests if a friendship exists between two users.
     *
     * @param userA The ID or screen_name of the first user to test friendship for.
     * @param userB The ID or screen_name of the second user to test friendship for.
     * @return if a friendship exists between two users.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public boolean existsFriendship(String userA, String userB) throws WeiboException {
        return -1 != get(getBaseURL() + "friendships/exists.json", "user_a", userA, "user_b", userB, true).
                asString().indexOf("true");
    }
    
    /**
     * Discontinues friendship with the user specified in the ID parameter as the authenticating user.  Returns the un-friended user in the requested format when successful.  Returns a string describing the failure condition when unsuccessful.
     *
     * @param id the ID or screen name of the user for whom to request a list of friends
     * @return User
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public User destroyFriendship(String id) throws WeiboException {
         return new User(http.post(getBaseURL() + "friendships/destroy/" + id + ".json", createParams(), true).asJSONObject());
    }
    
    /**
     * Befriends the user specified in the ID parameter as the authenticating user.  Returns the befriended user in the requested format when successful.  Returns a string describing the failure condition when unsuccessful.
     *
     * @param id the ID or screen name of the user to be befriended
     * @return the befriended user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public User createFriendship(String id) throws WeiboException {
         return new User(http.post(getBaseURL() + "friendships/create/" + id + ".json", createParams(), true).asJSONObject());
    }

    /**
     * Returns an array of numeric IDs for every user the authenticating user is following.
     * @return an array of numeric IDs for every user the authenticating user is following
     * @throws WeiboException when Weibo service or network is unavailable
     * @since androidroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public IDs getFriendsIDs() throws WeiboException {
        return getFriendsIDs(-1l);
    }

    /**
     * Returns an array of numeric IDs for every user the authenticating user is following.
     * <br/>饭否无cursor参数
     * 
     * @param cursor  Specifies the page number of the results beginning at 1. A single page contains 5000 ids. This is recommended for users with large ID lists. If not provided all ids are returned.
     * @return an array of numeric IDs for every user the authenticating user is following
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public IDs getFriendsIDs(long cursor) throws WeiboException {
        return new IDs(get(getBaseURL() + "friends/ids.xml?cursor=" + cursor, true));
    }
   
    
    /* 账户方法 */
    
    /**
     * Returns an HTTP 200 OK response code and a representation of the requesting user if authentication was successful; returns a 401 status code and an error message if not.  Use this method to test if supplied user credentials are valid.
     * 注意： 如果使用 错误的用户名/密码 多次登录后，饭否会锁住帐号，必须去fandou.com登录（输入验证码）
     * 
     * 登录成功返回 200 code
     * 登录失败返回 401 code
     * 使用WeiboException的getStatusCode取得code
     *
     * @return user
     * @since androidroid 0.5.0
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
     */
    public User verifyCredentials() throws WeiboException {
        return new User(get(getBaseURL() + "account/verify_credentials.json"
                , true).asJSONObject());
    }
    
    /* Saved Searches Methods */
    /**
     * Returns the authenticated user's saved search queries.
     * @return Returns an array of numeric user ids the authenticating user is blocking.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     */
    public List<SavedSearch> getSavedSearches() throws WeiboException {
        return SavedSearch.constructSavedSearches(get(getBaseURL() + "saved_searches.json", true));
    }

    /**
     * Retrieve the data for a saved search owned by the authenticating user specified by the given id.
     * @param id The id of the saved search to be retrieved.
     * @return the data for a saved search
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     */
    public SavedSearch showSavedSearch(int id) throws WeiboException {
        return new SavedSearch(get(getBaseURL() + "saved_searches/show/" + id
                + ".json", true));
    }

    /**
     * Retrieve the data for a saved search owned by the authenticating user specified by the given id.
     * @return the data for a created saved search
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     */
    public SavedSearch createSavedSearch(String query) throws WeiboException {
        return new SavedSearch(http.post(getBaseURL() + "saved_searches/create.json",
        		createParams(new BasicNameValuePair("query", query)), true));
    }

    /**
     * Destroys a saved search for the authenticated user. The search specified by id must be owned by the authenticating user.
     * @param id The id of the saved search to be deleted.
     * @return the data for a destroyed saved search
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     */
    public SavedSearch destroySavedSearch(int id) throws WeiboException {
        return new SavedSearch(http.post(getBaseURL() + "saved_searches/destroy/" + id
                + ".json", true));
    }
    
  
    /* Help Methods */
    /**
     * Returns the string "ok" in the requested format with a 200 OK HTTP status code.
     * @return true if the API is working
     * @throws WeiboException when Weibo service or network is unavailable
     * @since fanfoudroid 0.5.0
     */
    public boolean test() throws WeiboException {
        return -1 != get(getBaseURL() + "help/test.json", false).
                asString().indexOf("ok");
    }

    /***************** API METHOD END *********************/


    private SimpleDateFormat format = new SimpleDateFormat(
            "EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Weibo weibo = (Weibo) o;

        if (!baseURL.equals(weibo.baseURL)) return false;
        if (!format.equals(weibo.format)) return false;
        if (!http.equals(weibo.http)) return false;
        if (!searchBaseURL.equals(weibo.searchBaseURL)) return false;
        if (!source.equals(weibo.source)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = http.hashCode();
        result = 31 * result + baseURL.hashCode();
        result = 31 * result + searchBaseURL.hashCode();
        result = 31 * result + source.hashCode();
        result = 31 * result + format.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Weibo{" +
                "http=" + http +
                ", baseURL='" + baseURL + '\'' +
                ", searchBaseURL='" + searchBaseURL + '\'' +
                ", source='" + source + '\'' +
                ", format=" + format +
                '}';
    }
    

}
