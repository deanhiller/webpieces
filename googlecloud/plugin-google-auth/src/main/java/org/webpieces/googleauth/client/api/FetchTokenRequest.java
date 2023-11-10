package org.webpieces.googleauth.client.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FetchTokenRequest {


    //https://www.daimto.com/how-to-get-a-google-access-token-with-curl/
    //
    //curl -s \
    //--request POST \
    //--data "
    // code=4/1AY0e-g7BhBt0QU9f5HTgNDGNR1GYtH12q4xvgL_D2Q34A
    // &client_id=XXXX.apps.googleusercontent.com
    // &client_secret=zYAoXDam3mqsdwabh3dQ3NTh
    // &redirect_uri=urn:ietf:wg:oauth:2.0:oob
    // &grant_type=authorization_code" \
    //https://accounts.google.com/o/oauth2/token
    //
    //https://accounts.google.com/.well-known/openid-configuration -> endpoints to use?
    @JsonProperty("grant_type")
    private String grantType = "authorization_code";
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("client_secret")
    private String clientSecret;
    private String code;
    @JsonProperty("redirect_uri")
    private String callbackUrl;

    private String audience;

    private String scope;

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

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}