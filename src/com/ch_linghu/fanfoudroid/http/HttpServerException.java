package com.ch_linghu.fanfoudroid.http;


/**
 * HTTP StatusCode is not 200
 *  INTERNAL_SERVER_ERROR: 500
 *  BAD_GATEWAY: 502
 *  SERVICE_UNAVAILABLE: 503
 */
public class HttpServerException extends HttpException {

    public HttpServerException(Exception cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public HttpServerException(String msg, Exception cause, int statusCode) {
        super(msg, cause, statusCode);
        // TODO Auto-generated constructor stub
    }

    public HttpServerException(String msg, Exception cause) {
        super(msg, cause);
        // TODO Auto-generated constructor stub
    }

    public HttpServerException(String msg, int statusCode) {
        super(msg, statusCode);
        // TODO Auto-generated constructor stub
    }

    public HttpServerException(String msg) {
        super(msg);
        // TODO Auto-generated constructor stub
    }

}
