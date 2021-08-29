package org.webpieces.util.context;

import java.util.Map;

public class Contexts {
    private final Map<String, String> loggingCtxMap;
    private final Map<String, Object> webserverContext;

    public Contexts(Map<String, String> loggingCtxMap, Map<String, Object> webserverContext) {
        this.loggingCtxMap = loggingCtxMap;
        this.webserverContext = webserverContext;
    }

    public Map<String, String> getLoggingCtxMap() {
        return loggingCtxMap;
    }

    public Map<String, Object> getWebserverContext() {
        return webserverContext;
    }
}
