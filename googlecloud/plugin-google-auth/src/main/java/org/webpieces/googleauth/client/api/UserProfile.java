package org.webpieces.googleauth.client.api;

public class UserProfile {
    //        String userId = payload.getSubject();
    //        System.out.println("User ID: " + userId);
    //        FetchProfileResponse response = new FetchProfileResponse();
    //
    //        // Get profile information from payload
    //        String email = payload.getEmail();
    //        boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
    //        String name = (String) payload.get("name");
    //        String pictureUrl = (String) payload.get("picture");
    //        String locale = (String) payload.get("locale");
    //        String familyName = (String) payload.get("family_name");
    //        String givenName = (String) payload.get("given_name");
    private String userId;
    private String email;
    private Boolean emailVerified;
    private String name;
    private String pictureUrl;
    private String locale;
    private String familyName;
    private String givenName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean isEmailVerified() {
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

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
}
