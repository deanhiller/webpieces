package org.webpieces.googleauth.client.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FetchProfileResponse {

    //From https://developers.google.com/identity/sign-in/web/backend-auth
    //
    // // These six fields are included in all Google ID Tokens.
    // "iss": "https://accounts.google.com",
    // "sub": "110169484474386276334",
    // "azp": "1008719970978-hb24n2dstb40o45d4feuo2ukqmcc6381.apps.googleusercontent.com",
    // "aud": "1008719970978-hb24n2dstb40o45d4feuo2ukqmcc6381.apps.googleusercontent.com",
    // "iat": "1433978353",
    // "exp": "1433981953",
    //
    // // These seven fields are only included when the user has granted the "profile" and
    // // "email" OAuth scopes to the application.
    // "email": "testuser@gmail.com",
    // "email_verified": "true",
    // "name" : "Test User",
    // "picture": "https://lh4.googleusercontent.com/-kYgzyAWpZzJ/ABCDEFGHI/AAAJKLMNOP/tIXL9Ir44LE/s99-c/photo.jpg",
    // "given_name": "Test",
    // "family_name": "User",
    // "locale": "en"

    private String iss;
    private String sub;
    private String azp;
    private String aud;
    private String iat;
    private String exp;

    private String email;
    @JsonProperty("email_verified")
    private Boolean emailVerified;
    private String name;
    private String picture;
    @JsonProperty("given_name")
    private String givenName;
    @JsonProperty("family_name")
    private String familyName;

    private String locale;

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getAzp() {
        return azp;
    }

    public void setAzp(String azp) {
        this.azp = azp;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public String getIat() {
        return iat;
    }

    public void setIat(String iat) {
        this.iat = iat;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
