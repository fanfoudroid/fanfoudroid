/*
Copyright (c) 2007-2009
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

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;

/**
 * A data class represents search query.
 */
public class Query {
    private String query = null;
    private String lang = null;
    private int rpp = -1;
    private int page = -1;
    private long sinceId = -1;
    private String geocode = null;
    public Query(){
    }
    public Query(String query){
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    /**
     * Sets the query string
     * @param query - the query string
     */
    public void setQuery(String query) {
        this.query = query;
    }

    public String getLang() {
        return lang;
    }

    /**
     * restricts tweets to the given language, given by an <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1 code</a>
     * @param lang an <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1 code</a>
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    public int getRpp() {
        return rpp;
    }

    /**
     * sets the number of tweets to return per page, up to a max of 100
     * @param rpp the number of tweets to return per page
     */
    public void setRpp(int rpp) {
        this.rpp = rpp;
    }

    public int getPage() {
        return page;
    }

    /**
     * sets the page number (starting at 1) to return, up to a max of roughly 1500 results
     * @param page - the page number (starting at 1) to return
     */
    public void setPage(int page) {
        this.page = page;
    }

    public long getSinceId() {
        return sinceId;
    }

    /**
     * returns tweets with status ids greater than the given id.
     * @param sinceId - returns tweets with status ids greater than the given id
     */
    public void setSinceId(long sinceId) {
        this.sinceId = sinceId;
    }

    public String getGeocode() {
        return geocode;
    }

    public static final String MILES = "mi";
    public static final String KILOMETERS = "km";

    /**
     * returns tweets by users located within a given radius of the given latitude/longitude, where the user's location is taken from their Weibo profile
     * @param latitude latitude
     * @param longtitude longtitude
     * @param radius radius
     * @param unit Query.MILES or Query.KILOMETERS
     */
    public void setGeoCode(double latitude, double longtitude, double radius
            , String unit) {
        this.geocode = latitude + "," + longtitude + "," + radius + unit;
    }
    public ArrayList<BasicNameValuePair> asPostParameters(){
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        appendParameter("q", query, params);
        appendParameter("lang", lang, params);
        appendParameter("page", page, params);
        appendParameter("since_id",sinceId , params);
        appendParameter("geocode", geocode, params);
        return params;
    }

    private void appendParameter(String name, String value, ArrayList<BasicNameValuePair> params) {
        if (null != value) {
            params.add(new BasicNameValuePair(name, value));
        }
    }

    private void appendParameter(String name, long value, ArrayList<BasicNameValuePair> params) {
        if (0 <= value) {
            params.add(new BasicNameValuePair(name, value + ""));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Query query1 = (Query) o;

        if (page != query1.page) return false;
        if (rpp != query1.rpp) return false;
        if (sinceId != query1.sinceId) return false;
        if (geocode != null ? !geocode.equals(query1.geocode) : query1.geocode != null)
            return false;
        if (lang != null ? !lang.equals(query1.lang) : query1.lang != null)
            return false;
        if (query != null ? !query.equals(query1.query) : query1.query != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = query != null ? query.hashCode() : 0;
        result = 31 * result + (lang != null ? lang.hashCode() : 0);
        result = 31 * result + rpp;
        result = 31 * result + page;
        result = 31 * result + (int) (sinceId ^ (sinceId >>> 32));
        result = 31 * result + (geocode != null ? geocode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Query{" +
                "query='" + query + '\'' +
                ", lang='" + lang + '\'' +
                ", rpp=" + rpp +
                ", page=" + page +
                ", sinceId=" + sinceId +
                ", geocode='" + geocode + '\'' +
                '}';
    }
}
