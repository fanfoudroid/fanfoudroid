package com.ch_linghu.fanfoudroid.http;

import com.ch_linghu.fanfoudroid.weibo.RefuseError;


/**
 * HTTP StatusCode is not 200
 */
public class HttpAuthException extends HttpRefusedException {

    private static final long serialVersionUID = 4206324519931063593L;

    public HttpAuthException(RefuseError error) {
        super(error);
    }

}
