package org.webpieces.googleauth.client.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FetchTokenResponse {
//    {
//        "access_token": "ya29.a0AfH6SMDypscIeiyNnPRvoizz3NvvA6SZdk9U4K8h4MyQRRm29kEc2shdrskPZp71Q1roy8RqIm_7spufW84ozUoSTk0QKkQ",
//            "expires_in": 3599,
//            "refresh_token": "1//09Y5LQ0XRxjt-CgYIARAAGAkSNwF-L9IrYzyMnbGtJHgh-FTf6z79cBhQ1hsPUAk71HFgFwqyXoiwpIa-4eA",
//            "scope": "https://www.googleapis.com/auth/userinfo.profile",
//            "token_type": "Bearer"
//    }

// Taken from response ->
//
//    {
//  "access_token":
//  "expires_in":
//  "refresh_token":
//  "scope": "https://www.googleapis.com/auth/gmail.labels https://www.googleapis.com/auth/gmail.send https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/gmail.readonly openid",
//  "token_type": "Bearer",
//  "id_token":
//}
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("id_token")
    private String idToken;
    @JsonProperty("token_type")
    private String tokenType;
    private String scope;
    @JsonProperty("expires_in")
    private Long expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
