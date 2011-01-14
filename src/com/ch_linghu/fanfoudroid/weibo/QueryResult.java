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
package com.ch_linghu.fanfoudroid.weibo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ch_linghu.fanfoudroid.http.Response;


/**
 * A data class representing search API response
 * @author Yusuke Yamamoto - yusuke at mac.com
 */

public class QueryResult extends WeiboResponse {

    private long sinceId;
    private long maxId;
    private String refreshUrl;
    private int resultsPerPage;
    private int total = -1;
    private String warning;
    private double completedIn;
    private int page;
    private String query;
    private List<Status> tweets;
    private static final long serialVersionUID = -9059136565234613286L;

    /*package*/ QueryResult(Response res, WeiboSupport weiboSupport) throws WeiboException {
        super(res);
        // 饭否search API直接返回 "[{JSONObejet},{JSONObejet},{JSONObejet}]"的JSONArray
        //System.out.println("TAG " + res.asString());
        JSONArray array = res.asJSONArray();
        try {
            tweets = new ArrayList<Status>(array.length());
            for (int i = 0; i < array.length(); i++) {
                JSONObject tweet = array.getJSONObject(i);
                tweets.add(new Status(tweet));
            }
        } catch (JSONException jsone) {
            throw new WeiboException(jsone.getMessage() + ":" + array.toString(), jsone);
        }
    }
    /*package*/ QueryResult(Query query) throws WeiboException {
        super();
        sinceId = query.getSinceId();
        resultsPerPage = query.getRpp();
        page = query.getPage();
        tweets = new ArrayList<Status>(0);
    }

    public long getSinceId() {
        return sinceId;
    }

    public long getMaxId() {
        return maxId;
    }

    public String getRefreshUrl() {
        return refreshUrl;
    }

    public int getResultsPerPage() {
        return resultsPerPage;
    }

    /**
     * returns the number of hits
     * @return number of hits
     * @deprecated The Weibo API doesn't return total anymore
     * @see <a href="http://yusuke.homeip.net/jira/browse/TFJ-108">TRJ-108 deprecate QueryResult#getTotal()</a>
     */
    @Deprecated
	public int getTotal() {
        return total;
    }

    public String getWarning() {
        return warning;
    }

    public double getCompletedIn() {
        return completedIn;
    }

    public int getPage() {
        return page;
    }

    public String getQuery() {
        return query;
    }

    public List<Status> getStatus() {
        return tweets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryResult that = (QueryResult) o;

        if (Double.compare(that.completedIn, completedIn) != 0) return false;
        if (maxId != that.maxId) return false;
        if (page != that.page) return false;
        if (resultsPerPage != that.resultsPerPage) return false;
        if (sinceId != that.sinceId) return false;
        if (total != that.total) return false;
        if (!query.equals(that.query)) return false;
        if (refreshUrl != null ? !refreshUrl.equals(that.refreshUrl) : that.refreshUrl != null)
            return false;
        if (tweets != null ? !tweets.equals(that.tweets) : that.tweets != null)
            return false;
        if (warning != null ? !warning.equals(that.warning) : that.warning != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (sinceId ^ (sinceId >>> 32));
        result = 31 * result + (int) (maxId ^ (maxId >>> 32));
        result = 31 * result + (refreshUrl != null ? refreshUrl.hashCode() : 0);
        result = 31 * result + resultsPerPage;
        result = 31 * result + total;
        result = 31 * result + (warning != null ? warning.hashCode() : 0);
        temp = completedIn != +0.0d ? Double.doubleToLongBits(completedIn) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + page;
        result = 31 * result + query.hashCode();
        result = 31 * result + (tweets != null ? tweets.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "QueryResult{" +
                "sinceId=" + sinceId +
                ", maxId=" + maxId +
                ", refreshUrl='" + refreshUrl + '\'' +
                ", resultsPerPage=" + resultsPerPage +
                ", total=" + total +
                ", warning='" + warning + '\'' +
                ", completedIn=" + completedIn +
                ", page=" + page +
                ", query='" + query + '\'' +
                ", tweets=" + tweets +
                '}';
    }
}
