package org.webpieces.googlecloud.cloudtasks.localimpl;

import java.net.InetSocketAddress;

public class Endpoint {
    private final InetSocketAddress serverAddress;
    private final String httpMethod;
    private final String urlPath;

    public Endpoint(InetSocketAddress serverAddress, String httpMethod, String urlPath) {
        this.serverAddress = serverAddress;
        this.httpMethod = httpMethod;
        this.urlPath = urlPath;
    }

    public InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getUrlPath() {
        return urlPath;
    }
}
