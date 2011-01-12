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

import java.io.IOException;

import com.ch_linghu.fanfoudroid.http.HttpClient;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
/*protected*/ class WeiboSupport {
    protected HttpClient http = null;
    protected String source = Configuration.getSource();
    protected final boolean USE_SSL;

    /*package*/ WeiboSupport(){
        this(null, null);
    }
    /*package*/ WeiboSupport(String userId, String password){
        USE_SSL = Configuration.useSSL();
        http = new HttpClient(userId, password);
        
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
    
    //Added for compatibility
    public boolean isLoggedIn(){
    	return http.isLoggedIn();
    }

    public void logout(){
    	http.logout();
    }
    
    public void login(String username, String password) throws IOException, WeiboException{
    	http.login(username, password);
    }
}
