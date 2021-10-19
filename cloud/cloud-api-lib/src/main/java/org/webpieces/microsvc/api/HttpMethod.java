package org.webpieces.microsvc.api;

@Deprecated
public enum HttpMethod {

    DELETE("DELETE"),
    GET("GET"),
    OPTIONS("OPTIONS"),
    PATCH("PATCH"),
    POST("POST"),
    PUT("PUT");


    private final String method;

    HttpMethod(String method){
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
