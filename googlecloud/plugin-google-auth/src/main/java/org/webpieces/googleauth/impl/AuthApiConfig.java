package org.webpieces.googleauth.impl;

public class AuthApiConfig {
    private final String callbackUrl;
    private String clientId;
    private String clientSecret;

    public AuthApiConfig(
            String callbackUrl,
            String clientId,
            String clientSecret
    ) {
        this.callbackUrl = callbackUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

}
