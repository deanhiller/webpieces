package org.webpieces.auth0.api;

import org.webpieces.auth0.client.api.FetchProfileResponse;
import org.webpieces.auth0.mgmt.api.FetchUserResponse;

public class UserInfo {
    private final FetchProfileResponse profile;
    private final FetchUserResponse fetchUserResponse;

    public UserInfo(FetchProfileResponse profile, FetchUserResponse resp) {
        this.profile = profile;
        this.fetchUserResponse = resp;
    }

    public FetchProfileResponse getProfile() {
        return profile;
    }

    public FetchUserResponse getFetchUserResponse() {
        return fetchUserResponse;
    }
}
