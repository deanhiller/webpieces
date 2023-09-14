package org.webpieces.auth0.impl;

public class Auth0ApiConfig {
    private String auth0Domain;
    private String clientId;
    private String clientSecret;
    private String audience;

    public Auth0ApiConfig(
            String auth0Domain,
            String clientId,
            String clientSecret,
            String audience
    ) {
        this.auth0Domain = auth0Domain;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.audience = audience;
    }

    public String getAuth0Domain() {
        return auth0Domain;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getAudience() {
        return audience;
    }
}
