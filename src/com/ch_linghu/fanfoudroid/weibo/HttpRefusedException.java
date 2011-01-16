package com.ch_linghu.fanfoudroid.weibo;


/**
 * HTTP StatusCode is 403, Server refuse the request
 */
public class HttpRefusedException extends HttpException {
    /**
     * 服务器返回来的错误信息
     */
    private RefuseError mError;
    
    public HttpRefusedException() {
    }
    
    public HttpRefusedException(RefuseError error) {
        setError(error);
    }
    
    public RefuseError getError() {
        return mError;
    }
    
    public HttpRefusedException setError(RefuseError error) {
        mError = error;
        return this;
    }

}
