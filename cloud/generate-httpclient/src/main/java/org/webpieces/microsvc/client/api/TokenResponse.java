package org.webpieces.microsvc.client.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenResponse {
    /*
    {
      "access_token":"ya29.AHES6ZRN3-HlhAPya30GnW_bHSb_QtAi85nHq39HE3C2LTrCARA",
      "expires_in":3599,
      "token_type":"Bearer"
 }
     */

    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private Long expiresIn;
    @JsonProperty("token_type")
    private String tokenType;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
