package org.webpieces.microsvc.server.api;


public class ServerMetrics {

    public static final String SERVER_REQUEST_COUNT = "server.request";
    public static final String SERVER_REQUEST_SUCCESS = "server.request.success";
    public static final String SERVER_REQUEST_FAILURE = "server.request.failure";
    public static final String SERVER_REQUEST_TIME = "server.request.duration";

    public static class ServerMetricTags {

        public static final String CONTROLLER = "controller";
        public static final String METHOD = "method";
        public static final String HTTP_METHOD = "httpMethod";

    }

    public static class ServerExceptionMetricTags {

        public static final String CONTROLLER = ServerMetricTags.CONTROLLER;
        public static final String METHOD = ServerMetricTags.METHOD;
        public static final String HTTP_METHOD = ServerMetricTags.HTTP_METHOD;
        public static final String EXCEPTION = "exception";

    }

}
