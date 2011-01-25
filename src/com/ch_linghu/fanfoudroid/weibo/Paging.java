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

/**
 * Controlls pagination
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class Paging implements java.io.Serializable {
    private int page = -1;
    private int count = -1;
    private String sinceId = "";
    private String maxId = "";
    private static final long serialVersionUID = -3285857427993796670L;

    public Paging() {
    }

    public Paging(int page) {
        setPage(page);
    }

    public Paging(String sinceId) {
        setSinceId(sinceId);
    }

    public Paging(int page, int count) {
        this(page);
        setCount(count);
    }
    public Paging(int page, String sinceId) {
        this(page);
        setSinceId(sinceId);
    }

    public Paging(int page, int count, String sinceId) {
        this(page, count);
        setSinceId(sinceId);
    }

    public Paging(int page, int count, String sinceId, String maxId) {
        this(page, count, sinceId);
        setMaxId(maxId);
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        if (page < 1) {
            throw new IllegalArgumentException("page should be positive integer. passed:" + page);
        }
        this.page = page;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("count should be positive integer. passed:" + count);
        }
        this.count = count;
    }

    public Paging count(int count) {
        setCount(count);
        return this;
    }

    public String getSinceId() {
        return sinceId;
    }

    public void setSinceId(String sinceId) {
        if (sinceId.length() > 0) {
        	this.sinceId = sinceId;
        } else {
            throw new IllegalArgumentException("since_id is null. passed:" + sinceId);
        }
    }


    public Paging sinceId(String sinceId) {
        setSinceId(sinceId);
        return this;
    }

    public String getMaxId() {
        return maxId;
    }

    public void setMaxId(String maxId) {
        if (maxId.length() == 0) {
            throw new IllegalArgumentException("max_id is null. passed:" + maxId);
        }
        this.maxId = maxId;
    }

    public Paging maxId(String maxId) {
        setMaxId(maxId);
        return this;
    }
}
