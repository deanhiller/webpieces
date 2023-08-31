package org.webpieces.auth0.client.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthRequest {

    //grant_type=authorization_code&client_id={yourClientId}&client_secret=%7ByourClientSecret%7D
    // &code=yourAuthorizationCode%7D&redirect_uri={https://yourApp/callback}")
    //
    @JsonProperty("grant_type")
    private String grantType = "authorization_code";
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("client_secret")
    private String clientSecret;
    private String code;
    @JsonProperty("redirect_uri")
    private String callbackUrl;

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
}
