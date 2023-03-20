package org.webpieces.microsvc.server.api;

public class TokenConfig {
    private String token;

    public TokenConfig(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
