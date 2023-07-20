package org.webpieces.googlecloud.cloudtasks.impl;

import org.webpieces.nio.api.channels.HostWithPort;

import java.net.InetSocketAddress;

public class Endpoint {
    private final HostWithPort serverAddress;
    private final String httpMethod;
    private final String urlPath;

    public Endpoint(HostWithPort serverAddress, String httpMethod, String urlPath) {
        this.serverAddress = serverAddress;
        this.httpMethod = httpMethod;
        this.urlPath = urlPath;
    }

    public HostWithPort getServerAddress() {
        return serverAddress;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getUrlPath() {
        return urlPath;
    }
}
