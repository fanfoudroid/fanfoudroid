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
     * @since Weibo4J 1.1.7
     */
    public void setSearchBaseURL(String searchBaseURL) {
        this.searchBaseURL = searchBaseURL;
    }

    /**
     * Returns the search base url
     * @return search base url
     * @since Weibo4J 1.1.7
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
            if (-1 != paging.getMaxId()) {
                params.add(new BasicNameValuePair("max_id", String.valueOf(paging.getMaxId())));
            }
            if (-1 != paging.getSinceId()) {
                params.add(new BasicNameValuePair("since_id", String.valueOf(paging.getSinceId())));
            }
            if (-1 != paging.getPage()) {
                params.add(new BasicNameValuePair("page", String.valueOf(paging.getPage())));
            }
            if (-1 != paging.getCount()) {
                if (-1 != url.indexOf("search")) {
                    // search api takes "rpp"
                    params.add(new BasicNameValuePair("rpp", String.valueOf(paging.getCount())));
                } else {
                    params.add(new BasicNameValuePair("count", String.valueOf(paging.getCount())));
                }
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

    /**
     * Returns tweets that match a specified query.
     * <br>This method calls http://api.fanfou.com/users/search.format
     * @param query - the search condition
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.1
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">users/search </a>
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
     * @since Weibo4J 2.0.2
     */
    public Trends getTrends() throws WeiboException {
        return Trends.constructTrends(get(searchBaseURL + "trends.json", false));
    }

    /**
     * Returns the current top 10 trending topics on Weibo.  The response includes the time of the request, the name of each trending topic.
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.2
     */
    public Trends getCurrentTrends() throws WeiboException {
        return Trends.constructTrendsList(get(searchBaseURL + "trends/current.json"
                , false)).get(0);
    }

    /**
     * Returns the current top 10 trending topics on Weibo.  The response includes the time of the request, the name of each trending topic.
     * @param excludeHashTags Setting this to true will remove all hashtags from the trends list.
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.2
     */
    public Trends getCurrentTrends(boolean excludeHashTags) throws WeiboException {
        return Trends.constructTrendsList(get(searchBaseURL + "trends/current.json"
                + (excludeHashTags ? "?exclude=hashtags" : ""), false)).get(0);
    }


    /**
     * Returns the top 20 trending topics for each hour in a given day.
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.2
     */
    public List<Trends> getDailyTrends() throws WeiboException {
        return Trends.constructTrendsList(get(searchBaseURL + "trends/daily.json", false));
    }

    /**
     * Returns the top 20 trending topics for each hour in a given day.
     * @param date Permits specifying a start date for the report.
     * @param excludeHashTags Setting this to true will remove all hashtags from the trends list.
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.2
     */
    public List<Trends> getDailyTrends(Date date, boolean excludeHashTags) throws WeiboException {
        return Trends.constructTrendsList(get(searchBaseURL
                + "trends/daily.json?date=" + toDateStr(date)
                + (excludeHashTags ? "&exclude=hashtags" : ""), false));
    }

    private String toDateStr(Date date){
        if(null == date){
            date = new Date();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * Returns the top 30 trending topics for each day in a given week.
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.2
     */
    public List<Trends> getWeeklyTrends() throws WeiboException {
        return Trends.constructTrendsList(get(searchBaseURL
                + "trends/weekly.json", false));
    }

    /**
     * Returns the top 30 trending topics for each day in a given week.
     * @param date Permits specifying a start date for the report.
     * @param excludeHashTags Setting this to true will remove all hashtags from the trends list.
     * @return the result
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.2
     */
    public List<Trends> getWeeklyTrends(Date date, boolean excludeHashTags) throws WeiboException {
        return Trends.constructTrendsList(get(searchBaseURL
                + "trends/weekly.json?date=" + toDateStr(date)
                + (excludeHashTags ? "&exclude=hashtags" : ""), false));
    }

    /* Status Methods */

    /**
     * Returns the 20 most recent statuses from non-protected users who have set a custom user icon.
     * <br>This method calls http://api.fanfou.com/statuses/public_timeline.format
     *
     * @return list of statuses of the Public Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/public_timeline </a>
     */
    public List<Status> getPublicTimeline() throws
            WeiboException {
    	/*modify by sycheng edit with json */
        return Status.constructStatuses(get(getBaseURL() +
                "statuses/public_timeline.json", true));

//      return Status.constructStatuses(get(getBaseURL() +"statuses/public_timeline.xml", true), this);
    }

    public RateLimitStatus getRateLimitStatus()throws
            WeiboException {
    	/*modify by sycheng edit with json */
        return new RateLimitStatus(get(getBaseURL() +
                "account/rate_limit_status.json", true),this);
    }

    /**
     * Returns only public statuses with an ID greater than (that is, more recent than) the specified ID.
     * <br>This method calls http://api.fanfou.com/statuses/public_timeline.format
     *
     * @param sinceID returns only public statuses with an ID greater than (that is, more recent than) the specified ID
     * @return the 20 most recent statuses
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/public_timeline </a>
     * @deprecated use getPublicTimeline(long sinceID) instead
     */
    public List<Status> getPublicTimeline(int sinceID) throws
            WeiboException {
        return getPublicTimeline((long)sinceID);
    }
    /**
     * Returns only public statuses with an ID greater than (that is, more recent than) the specified ID.
     * <br>This method calls http://api.fanfou.com/statuses/public_timeline.format
     *
     * @param sinceID returns only public statuses with an ID greater than (that is, more recent than) the specified ID
     * @return the 20 most recent statuses
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/public_timeline </a>
     */
    public List<Status> getPublicTimeline(long sinceID) throws
            WeiboException {
      /*  return Status.constructStatuses(get(getBaseURL() +
                "statuses/public_timeline.xml", null, new Paging((long) sinceID)
                , false), this);*/
    	return Status.constructStatuses(get(getBaseURL() +
                "statuses/public_timeline.json", null, new Paging((long) sinceID)
                , false));
    }

    /**
     * Returns the 20 most recent statuses, including retweets, posted by the authenticating user and that user's friends. This is the equivalent of /timeline/home on the Web.
     * <br>This method calls http://api.fanfou.com/statuses/home_timeline.format
     *
     * @return list of the home Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"> statuses/friends_timeline </a>
     * @since Weibo4J 2.0.10
     */
    public List<Status> getHomeTimeline() throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/home_timeline.json", true));
//        return Status.constructStatuses(get(getBaseURL() + "statuses/home_timeline.xml", true), this);
    }


    /**
     * Returns the 20 most recent statuses, including retweets, posted by the authenticating user and that user's friends. This is the equivalent of /timeline/home on the Web.
     * <br>This method calls  http://api.fanfou.com/statuses/home_timeline.format
     *
     * @param paging controls pagination
     * @return list of the home Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"> statuses/friends_timeline </a>
     * @since Weibo4J 2.0.10
     */
    public List<Status> getHomeTimeline(Paging paging) throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/home_timeline.json", null, paging, true));
//        return Status.constructStatuses(get(getBaseURL() + "statuses/home_timeline.xml", null, paging, true), this);
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating1 user and that user's friends.
     * It's also possible to request another user's friends_timeline via the id parameter below.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"> statuses/friends_timeline </a>
     */
    public List<Status> getFriendsTimeline() throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.json", true));
//        return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.xml", true), this);
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @param page the number of page
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use getFriendsTimeline(Paging paging) instead
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"> statuses/friends_timeline </a>
     */
    public List<Status> getFriendsTimelineByPage(int page) throws
            WeiboException {
        return getFriendsTimeline(new Paging(page));
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @param page the number of page
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1.8
     * @deprecated Use getFriendsTimeline(Paging paging) instead
     * @see <a href="http:///wiki/index.php/Statuses/friends_timeline"> statuses/friends_timeline </a>
     */
    public List<Status> getFriendsTimeline(int page) throws
            WeiboException {
        return getFriendsTimeline(new Paging(page));
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param page    the number of page
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1.8
     * @deprecated Use getFriendsTimeline(Paging paging) instead
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"> statuses/friends_timeline </a>
     */
    public List<Status> getFriendsTimeline(long sinceId, int page) throws
            WeiboException {
        return getFriendsTimeline(new Paging(page).sinceId(sinceId));
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @param id specifies the ID or screen name of the user for whom to return the friends_timeline
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"> statuses/friends_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public List<Status> getFriendsTimeline(String id) throws
            WeiboException {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @param id   specifies the ID or screen name of the user for whom to return the friends_timeline
     * @param page the number of page
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"> statuses/friends_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public List<Status> getFriendsTimelineByPage(String id, int page) throws
            WeiboException {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @param id   specifies the ID or screen name of the user for whom to return the friends_timeline
     * @param page the number of page
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1.8
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"> statuses/friends_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public List<Status> getFriendsTimeline(String id, int page) throws
            WeiboException {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param id   specifies the ID or screen name of the user for whom to return the friends_timeline
     * @param page the number of page
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1.8
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"> statuses/friends_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public List<Status> getFriendsTimeline(long sinceId, String id, int page) throws
            WeiboException {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @param paging controls pagination
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.1
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"> statuses/friends_timeline </a>
     */
    public List<Status> getFriendsTimeline(Paging paging) throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.json",null, paging, true));
//        return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.xml",null, paging, true), this);
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @param id   specifies the ID or screen name of the user for whom to return the friends_timeline
     * @param paging controls pagination
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.1
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"> statuses/friends_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public List<Status> getFriendsTimeline(String id, Paging paging) throws
            WeiboException {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }


    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @param since narrows the returned results to just those statuses created after the specified HTTP-formatted date
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use getFriendsTimeline(Paging paging) instead
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"> statuses/friends_timeline </a>
     */
    public List<Status> getFriendsTimeline(Date since) throws
            WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.xml",
                "since", format.format(since), true), this);
    }

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1.8
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"> statuses/friends_timeline </a>
     * @deprecated Use getFriendsTimeline(Paging paging) instead
     */
    public List<Status> getFriendsTimeline(long sinceId) throws
            WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.xml",
                "since_id", String.valueOf(sinceId), true), this);
    }	
    
    public List<Status> getFriendsTimeline(int page, int count) throws
    		WeiboException {
    	Paging paging = new Paging(page, count);
    	return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.json",null, paging, true));
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @param id    specifies the ID or screen name of the user for whom to return the friends_timeline
     * @param since narrows the returned results to just those statuses created after the specified HTTP-formatted date
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"> statuses/friends_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public List<Status> getFriendsTimeline(String id,
                                                        Date since) throws WeiboException {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/friends_timeline.format
     *
     * @param id    specifies the ID or screen name of the user for whom to return the friends_timeline
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @return list of the Friends Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1.8
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"> statuses/friends_timeline </a>
     * @deprecated The Weibo API does not support this method anymore.
     */
    public List<Status> getFriendsTimeline(String id, long sinceId) throws WeiboException {
        throw new IllegalStateException("The Weibo API is not supporting this method anymore");
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/user_timeline.format
     *
     * @param id    specifies the ID or screen name of the user for whom to return the user_timeline
     * @param count specifies the number of statuses to retrieve.  May not be greater than 200 for performance purposes
     * @param since narrows the returned results to just those statuses created after the specified HTTP-formatted date
     * @return list of the user Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/user_timeline</a>
     * @deprecated using long sinceId is suggested.
     */
    public List<Status> getUserTimeline(String id, int count
            , Date since) throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + id + ".xml",
                "since", format.format(since), "count", String.valueOf(count), http.isAuthenticationEnabled()), this);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/user_timeline.format
     *
     * @param id    specifies the ID or screen name of the user for whom to return the user_timeline
     * @param count specifies the number of statuses to retrieve.  May not be greater than 200 for performance purposes
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @return list of the user Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1.8
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/user_timeline</a>
     * @deprecated Use getUserTimeline(String id, Paging paging) instead
     */
    public List<Status> getUserTimeline(String id, int count,
                                                     long sinceId) throws WeiboException {
        return getUserTimeline(id, new Paging(sinceId).count(count));
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/user_timeline.format
     *
     * @param id    specifies the ID or screen name of the user for whom to return the user_timeline
     * @param paging controls pagenation
     * @return list of the user Timeline
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.1
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/user_timeline</a>
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
     * @param id    specifies the ID or screen name of the user for whom to return the user_timeline
     * @param since narrows the returned results to just those statuses created after the specified HTTP-formatted date
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/user_timeline</a>
     * @deprecated Use getUserTimeline(String id, Paging paging) instead
     */
    public List<Status> getUserTimeline(String id, Date since) throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + id + ".xml",
                "since", format.format(since), http.isAuthenticationEnabled()), this);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/user_timeline.format
     *
     * @param id    specifies the ID or screen name of the user for whom to return the user_timeline
     * @param count specifies the number of statuses to retrieve.  May not be greater than 200 for performance purposes
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/user_timeline</a>
     * @deprecated Use getUserTimeline(String id, Paging paging) instead
     */
    public List<Status> getUserTimeline(String id, int count) throws
            WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + id + ".xml",
                "count", String.valueOf(count), http.isAuthenticationEnabled()), this);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.fanfou.com/statuses/user_timeline.format
     *
     * @param count specifies the number of statuses to retrieve.  May not be greater than 200 for performance purposes
     * @param since narrows the returned results to just those statuses created after the specified HTTP-formatted date
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/user_timeline</a>
     * @deprecated using long sinceId is suggested.
     */
    public List<Status> getUserTimeline(int count, Date since) throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.xml",
                "since", format.format(since), "count", String.valueOf(count), true), this);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.fanfou.com/statuses/user_timeline.format
     *
     * @param count specifies the number of statuses to retrieve.  May not be greater than 200 for performance purposes
     * @param sinceId returns only statuses with an ID greater than (that is, more recent than) the specified ID.
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/user_timeline</a>
     * @since Weibo4J 2.0.0
     * @deprecated Use getUserTimeline(String id, Paging paging) instead
     */
    public List<Status> getUserTimeline(int count, long sinceId) throws WeiboException {
        return getUserTimeline(new Paging(sinceId).count(count));
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/user_timeline.format
     *
     * @param id specifies the ID or screen name of the user for whom to return the user_timeline
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/user_timeline</a>
     */
    public List<Status> getUserTimeline(String id) throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + id + ".json", http.isAuthenticationEnabled()));
//        return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + id + ".xml", http.isAuthenticationEnabled()), this);
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the specified userid.
     * <br>This method calls http://api.fanfou.com/statuses/user_timeline.format
     *
     * @param id specifies the ID or screen name of the user for whom to return the user_timeline
     * @param sinceId returns only statuses with an ID greater than (that is, more recent than) the specified ID.
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/user_timeline</a>
     * @since Weibo4J 2.0.0
     * @deprecated Use getUserTimeline(String id, Paging paging) instead
     */
    public List<Status> getUserTimeline(String id, long sinceId) throws WeiboException {
        return getUserTimeline(id, new Paging(sinceId));
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.fanfou.com/statuses/user_timeline.format
     *
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/user_timeline</a>
     */
    public List<Status> getUserTimeline() throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.json"
                , true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.xml"
                , true), this);*/
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.fanfou.com/statuses/user_timeline.format
     *
     * @param paging controls pagination
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/user_timeline</a>
     * @since Weibo4J 2.0.1
     */
    public List<Status> getUserTimeline(Paging paging) throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.json"
                , null, paging, true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.xml"
                , null, paging, true), this);*/
    }

    public List<Status> getUserTimeline(int page, int count) throws
            WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.json"
                , null, new Paging(page, count), true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.xml"
                , null, paging, true), this);*/
    }

    /**
     * Returns the most recent statuses posted in the last 24 hours from the authenticating user.
     * <br>This method calls http://api.fanfou.com/statuses/user_timeline.format
     *
     * @param sinceId returns only statuses with an ID greater than (that is, more recent than) the specified ID.
     * @return the 20 most recent statuses posted in the last 24 hours from the user
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/user_timeline</a>
     * @since Weibo4J 2.0.0
     * @deprecated Use getUserTimeline(Paging paging) instead
     */
    public List<Status> getUserTimeline(long sinceId) throws
            WeiboException {
        return getUserTimeline(new Paging(sinceId));
    }

    /**
     * Returns the 20 most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.fanfou.com/statuses/reply.format
     *
     * @return the 20 most recent replies
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use getMentions() instead
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/reply </a>
     */
    public List<Status> getReplies() throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/replies.xml", true), this);
    }

    /**
     * Returns the 20 most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.fanfou.com/statuses/reply.format
     *
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @return the 20 most recent replies
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1.8
     * @deprecated Use getMentions(Paging paging) instead
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/reply </a>
     */
    public List<Status> getReplies(long sinceId) throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/replies.xml",
                "since_id", String.valueOf(sinceId), true), this);
    }

    /**
     * Returns the most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.fanfou.com/statuses/reply.format
     *
     * @param page the number of page
     * @return the 20 most recent replies
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use getMentions(Paging paging) instead
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/reply </a>
     */
    public List<Status> getRepliesByPage(int page) throws WeiboException {
        if (page < 1) {
            throw new IllegalArgumentException("page should be positive integer. passed:" + page);
        }
        return Status.constructStatuses(get(getBaseURL() + "statuses/replies.xml",
                "page", String.valueOf(page), true), this);
    }

    /**
     * Returns the most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.fanfou.com/statuses/reply.format
     *
     * @param page the number of page
     * @return the 20 most recent replies
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1.8
     * @deprecated Use getMentions(Paging paging) instead
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/reply </a>
     */
    public List<Status> getReplies(int page) throws WeiboException {
        if (page < 1) {
            throw new IllegalArgumentException("page should be positive integer. passed:" + page);
        }
        return Status.constructStatuses(get(getBaseURL() + "statuses/replies.xml",
                "page", String.valueOf(page), true), this);
    }

    /**
     * Returns the most recent replies (status updates prefixed with @username) to the authenticating user.  Replies are only available to the authenticating user; you can not request a list of replies to another user whether public or protected.
     * <br>This method calls http://api.fanfou.com/statuses/reply.format
     *
     * @param sinceId Returns only statuses with an ID greater than (that is, more recent than) the specified ID
     * @param page the number of page
     * @return the 20 most recent replies
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1.8
     * @deprecated Use getMentions(Paging paging) instead
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/reply </a>
     */
    public List<Status> getReplies(long sinceId, int page) throws WeiboException {
        if (page < 1) {
            throw new IllegalArgumentException("page should be positive integer. passed:" + page);
        }
        return Status.constructStatuses(get(getBaseURL() + "statuses/replies.xml",
                "since_id", String.valueOf(sinceId),
                "page", String.valueOf(page), true), this);
    }

    /**
     * Returns the 20 most recent mentions (status containing @username) for the authenticating user.
     * <br>This method calls http://api.fanfou.com/statuses/mentions.format
     *
     * @return the 20 most recent replies
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.1
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">Statuses/mentions </a>
     */
    public List<Status> getMentions() throws WeiboException {
        return Status.constructStatuses(get(getBaseURL() + "statuses/mentions.json",
                null, true));
    }
    
    // since_id
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
     * @since Weibo4J 2.0.1
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">Statuses/mentions </a>
     */
    public List<Status> getMentions(Paging paging) throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/mentions.json",
                null, paging, true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/mentions.xml",
                null, paging, true), this);*/
    }

    /**
     * Returns the 20 most recent retweets posted by the authenticating user.
     *
     * @return the 20 most recent retweets posted by the authenticating user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.10
     */
    public List<Status> getRetweetedByMe() throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_by_me.json",
                null, true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_by_me.xml",
                null, true), this);*/
    }

    /**
     * Returns the 20 most recent retweets posted by the authenticating user.
     * @param paging controls pagination
     * @return the 20 most recent retweets posted by the authenticating user
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.10
     */
    public List<Status> getRetweetedByMe(Paging paging) throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_by_me.json",
                null, true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_by_me.xml",
                null, paging, true), this);*/
    }

    /**
     * Returns the 20 most recent retweets posted by the authenticating user's friends.
     * @return the 20 most recent retweets posted by the authenticating user's friends.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.10
     */
    public List<Status> getRetweetedToMe() throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_to_me.json",
                null, true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_to_me.xml",
                null, true), this);*/
    }

    /**
     * Returns the 20 most recent retweets posted by the authenticating user's friends.
     * @param paging controls pagination
     * @return the 20 most recent retweets posted by the authenticating user's friends.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.10
     */
    public List<Status> getRetweetedToMe(Paging paging) throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_to_me.json",
                null, paging, true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/retweeted_to_me.xml",
                null, paging, true), this);*/
    }

    /**
     * Returns the 20 most recent tweets of the authenticated user that have been retweeted by others.
     * @return the 20 most recent tweets of the authenticated user that have been retweeted by others.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.10
     */
    public List<Status> getRetweetsOfMe() throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/retweets_of_me.json",
                null, true));
        /*return Status.constructStatuses(get(getBaseURL() + "statuses/retweets_of_me.xml",
                null, true), this);*/
    }

    /**
     * Returns the 20 most recent tweets of the authenticated user that have been retweeted by others.
     * @param paging controls pagination
     * @return the 20 most recent tweets of the authenticated user that have been retweeted by others.
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.10
     */
    public List<Status> getRetweetsOfMe(Paging paging) throws WeiboException {
    	return Status.constructStatuses(get(getBaseURL() + "statuses/retweets_of_me.json",
                null, paging, true));
    	/* return Status.constructStatuses(get(getBaseURL() + "statuses/retweets_of_me.xml",
                null, paging, true), this);*/
    }


    /**
     * Returns a single status, specified by the id parameter. The status's author will be returned inline.
     * @param id the numerical ID of the status you're trying to retrieve
     * @return a single status
     * @throws WeiboException when Weibo service or network is unavailable
     * @deprecated Use showStatus(long id) instead.
     */
    public Status show(String id) throws WeiboException {
        return showStatus((String)id);
    }

    /**
     * Returns a single status, specified by the id parameter. The status's author will be returned inline.
     * <br>This method calls http://api.fanfou.com/statuses/show/id.format
     *
     * @param id the numerical ID of the status you're trying to retrieve
     * @return a single status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1.1
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/show </a>
     * @deprecated Use showStatus(long id) instead.
     */

    public Status show(long id) throws WeiboException {
        return new Status(get(getBaseURL() + "statuses/show/" + id + ".xml", false), this);
    }

    /**
     * Returns a single status, specified by the id parameter. The status's author will be returned inline.
     * <br>This method calls http://api.fanfou.com/statuses/show/id.format
     *
     * @param id the numerical ID of the status you're trying to retrieve
     * @return a single status
     * @throws WeiboException when Weibo service or network is unavailable. 可能因为“你没有通过这个用户的验证“,返回403
     * @since Weibo4J 2.0.1
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/show </a>
     */
    public Status showStatus(String id) throws WeiboException {
//        return new Status(get(getBaseURL() + "statuses/show/" + id + ".xml", false), this);
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
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/update </a>
     * @deprecated Use updateStatus(String status) instead
     */
    public Status update(String status) throws WeiboException {
        return updateStatus(status);
    }

    /*modify by sycheng with json */
    /**
     * Updates the user's status.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * <br>This method calls http://api.fanfou.com/statuses/update.format
     *
     * @param status the text of your status update
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.1
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/update </a>
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
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/update </a>
     * @since Weibo4J 2.0.10
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
     * @see <a href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation">statuses/update </a>
     * @deprecated Use updateStatus(String status, long inReplyToStatusId) instead
     */
    public Status update(String status, String inReplyToStatusId) throws WeiboException {
        return updateStatus(status, inReplyToStatusId);
    }

    /**
     * Updates the user's status.
     * 如果要使用inreplyToStatusId, 那么该status就必须得是@别人的.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * <br>发布消息  http://api.fanfou.com/statuses/update.[json|xml] 
     *
     * @param status            the text of your status update
     * @param inReplyToStatusId The ID of an existing status that the status to be posted is in reply to.  This implicitly sets the in_reply_to_user_id attribute of the resulting status to the user ID of the message being replied to.  Invalid/missing status IDs will be ignored.
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.1
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
     * @since Weibo4J 2.0.10
     */
    public Status updateStatus(String status, String inReplyToStatusId
            , double latitude, double longitude) throws WeiboException {
        return new Status(http.post(getBaseURL() + "statuses/update.json",
        		createParams(new BasicNameValuePair("status", status),
   					new BasicNameValuePair("location", latitude + "," + longitude),
   					new BasicNameValuePair("in_reply_to_status_id", inReplyToStatusId))));
    }
    
    //TODO: upload photo
    /**
     * upload the user's status.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * The image suport.
     * <br>发布照片  http://api.fanfou.com/photos/upload.[json|xml]
     *
     *
     * @param status the text of your status update
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.1
     */
//   public Status uploadStatus(String status,ImageItem item) throws WeiboException {
//        return new Status(
//                http.multPartURL(getBaseURL() + "photos/upload.json",
//                new PostParameter[]{new PostParameter("status", status), new PostParameter("source", source)},
//                item, true)
//                );
        /*return new Status(http.multPartURL(getBaseURL() + "statuses/upload.xml",
                new PostParameter[]{new PostParameter("status", status), new PostParameter("source", source)},item, true), this);*/
//   }
    
    /**
     * upload the photo.
     * The text will be trimed if the length of the text is exceeding 160 characters.
     * The image suport.
     * <br>上传照片  http://api.fanfou.com/photos/upload.[json|xml]
     *
     * @param status the text of your status update
     * @return the latest status
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.1
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
     * <br>This method calls http://api.t.sina.com.cn/users/show.format
     *
     * @param id (cann't be screenName the ID of the user for whom to request the detail
     * @return User
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Users/show">users/show </a>
     * @since Weibo4J 2.0.9
     */
    public User showUser(String id) throws WeiboException {
       /* return new User(get(getBaseURL() + "users/show/" + id + ".xml"
                , http.isAuthenticationEnabled()), this);*/
         return new User(
        		 get(getBaseURL() + "users/show.json",
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
     * @since Weibo4J 2.0.10
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
     * @since Weibo4J 2.0.10
     */
    public Status repost(String repost_status_id, String new_status) throws WeiboException {
        Status repost_to = showStatus(repost_status_id);
        String to_user_name = repost_to.getUser().getName();
        String repost_status_text = repost_to.getText();
        
        return repost(to_user_name, repost_status_id, repost_status_text, new_status);
    }
    
    /**
     * Returns the specified user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     *
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     * @since Weibo4J 2.0.9
     */
    public List<User> getFriendsStatuses() throws WeiboException {
//        return User.constructUsers(get(getBaseURL() + "statuses/friends.xml", true), this);
        return User.constructResult(get(getBaseURL() + "users/friends.json", true));
    }
    
    /**
     * Returns the specified user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     *
     * @param paging controls pagination
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.9
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     */
    public List<User> getFriendsStatuses(Paging paging) throws WeiboException {
        /*return User.constructUsers(get(getBaseURL() + "statuses/friends.xml", null,
                paging, true), this);*/
        return User.constructUsers(get(getBaseURL() + "users/friends.json", null,
                paging, true));
    }
    
    /**
     * Returns the user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     *
     * @param id the ID or screen name of the user for whom to request a list of friends
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     * @since Weibo4J 2.0.9
     */
    public List<User> getFriendsStatuses(String id) throws WeiboException {
        /*return User.constructUsers(get(getBaseURL() + "statuses/friends/" + id + ".xml"
                , false), this);*/
        return User.constructUsers(get(getBaseURL() + "users/friends.json", 
        		createParams(new BasicNameValuePair("id", id)), false));
    }
    
    /**
     * Returns the user's friends, each with current status inline.
     * <br>This method calls http://api.t.sina.com.cn/statuses/friends.format
     *
     * @param id the ID or screen name of the user for whom to request a list of friends
     * @param paging controls pagination (饭否API 默认返回 100 条/页)
     * @return the list of friends
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 2.0.9
     * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/friends">statuses/friends </a>
     */
    public List<User> getFriendsStatuses(String id, Paging paging) throws WeiboException {
       /* return User.constructUsers(get(getBaseURL() + "statuses/friends/" + id + ".xml"
                , null, paging, false), this);*/
        return User.constructUsers(get(getBaseURL() + "users/friends.json", 
        		createParams(new BasicNameValuePair("id", id)), paging, false));
    }
   
    /*****************MARK*********************/
    
    //TODO: add all API method
    
    /*****************MARK*********************/
    
  
    /* Help Methods */
    /**
     * Returns the string "ok" in the requested format with a 200 OK HTTP status code.
     * @return true if the API is working
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.0.4
     */
    public boolean test() throws WeiboException {
        return -1 != get(getBaseURL() + "help/test.json", false).
                asString().indexOf("ok");
    }

    /**
     * Returns extended information of the authenticated user.  This information includes design settings, so third party developers can theme their widgets according to a given user's preferences.<br>
     * The call Weibo.getAuthenticatedUser() is equivalent to the call:<br>
     * weibo.getUserDetail(weibo.getUserId());
     * @return User
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.1.3
     * @deprecated Use verifyCredentials() instead
     */
    public User getAuthenticatedUser() throws WeiboException {
        return new User(get(getBaseURL() + "account/verify_credentials.xml", true),this);
    }

    /**
     * @return the schedule
     * @throws WeiboException when Weibo service or network is unavailable
     * @since Weibo4J 1.0.4
     * @deprecated this method is not supported by the Weibo API anymore
     */
    public String getDowntimeSchedule() throws WeiboException {
        throw new WeiboException(
                "this method is not supported by the Weibo API anymore"
                , new NoSuchMethodException("this method is not supported by the Weibo API anymore"));
    }


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
