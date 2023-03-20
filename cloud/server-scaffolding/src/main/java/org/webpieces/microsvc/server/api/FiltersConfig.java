package org.webpieces.microsvc.server.api;

public class FiltersConfig {
    private String token;

    public FiltersConfig(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
