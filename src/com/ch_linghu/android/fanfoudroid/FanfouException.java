package com.ch_linghu.android.fanfoudroid;

/**
 * An exception class that will be thrown when WeiboAPI calls are failed.<br>
 * In case the Fanfou server returned HTTP error code, you can get the HTTP status code using getStatusCode() method.
 */
public class FanfouException extends Exception {
    private int statusCode = -1;
    private static final long serialVersionUID = -2623309261327598087L;

    public FanfouException(String msg) {
        super(msg);
    }

    public FanfouException(Exception cause) {
        super(cause);
    }

    public FanfouException(String msg, int statusCode) {
        super(msg);
        this.statusCode = statusCode;

    }

    public FanfouException(String msg, Exception cause) {
        super(msg, cause);
    }

    public FanfouException(String msg, Exception cause, int statusCode) {
        super(msg, cause);
        this.statusCode = statusCode;

    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
