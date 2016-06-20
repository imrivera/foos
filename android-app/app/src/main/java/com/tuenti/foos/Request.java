package com.tuenti.foos;

/**
 * Created by adelcampo on 16/06/16.
 */
public class Request {
    private String mUrl;
    private String mBody;
    private HttpMethod mMethod;
    public Request(String url, String body, HttpMethod method) {
        this.mUrl = url;
        this.mBody = body;
        this.mMethod = method;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getBody() {
        return mBody;
    }

    public HttpMethod getMethod() {
        return mMethod;
    }

    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE
    }

}
