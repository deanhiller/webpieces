package org.webpieces.microsvc.server.api;

public class CorsConfig {
    private String[] domains;
    private String[] allowedRequestHeaders = {"content-type", "content-length","user-agent"};
    private String[] exposedResponseHeaders = {};
    private boolean allowCredentials = false;
    private int expiredTimeSeconds = 86400;

    public CorsConfig() {}

    public CorsConfig(String[] domains) {
        this.domains = domains;
    }

    public CorsConfig(String[] domains, boolean allowCredentials) {
        this.domains = domains;
        this.allowCredentials = allowCredentials;
    }

    public String[] getDomains() {
        return domains;
    }

    public void setDomains(String[] domains) {
        this.domains = domains;
    }

    public String[] getAllowedRequestHeaders() {
        return allowedRequestHeaders;
    }

    public void setAllowedRequestHeaders(String[] allowedRequestHeaders) {
        this.allowedRequestHeaders = allowedRequestHeaders;
    }

    public String[] getExposedResponseHeaders() {
        return exposedResponseHeaders;
    }

    public void setExposedResponseHeaders(String[] exposedResponseHeaders) {
        this.exposedResponseHeaders = exposedResponseHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public int getExpiredTimeSeconds() {
        return expiredTimeSeconds;
    }

    public void setExpiredTimeSeconds(int expiredTimeSeconds) {
        this.expiredTimeSeconds = expiredTimeSeconds;
    }
}
