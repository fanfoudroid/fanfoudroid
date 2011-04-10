package com.ch_linghu.fanfoudroid.http;


/**
 * HTTP StatusCode is not 200
 */
public class HttpException extends Exception {
    private int statusCode = -1;
    
    public HttpException(String msg) {
        super(msg);
    }

    public HttpException(Exception cause) {
        super(cause);
    }

    public HttpException(String msg, int statusCode) {
        super(msg);
        this.statusCode = statusCode;
    }

    public HttpException(String msg, Exception cause) {
        super(msg, cause);
    }

    public HttpException(String msg, Exception cause, int statusCode) {
        super(msg, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
        

}
