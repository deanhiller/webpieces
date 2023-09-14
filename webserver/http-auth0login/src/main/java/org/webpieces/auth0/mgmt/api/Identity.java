package org.webpieces.auth0.mgmt.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Identity {

    private String connection;
    @JsonProperty("user_id")
    private String userId;
    private String provider;
    private Boolean isSocial;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("access_token_secret")
    private String accessTokenSecret;
    @JsonProperty("refresh_token")
    private String refreshToken;
    private String profileData;

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Boolean getSocial() {
        return isSocial;
    }

    public void setSocial(Boolean social) {
        isSocial = social;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getProfileData() {
        return profileData;
    }

    public void setProfileData(String profileData) {
        this.profileData = profileData;
    }
}
