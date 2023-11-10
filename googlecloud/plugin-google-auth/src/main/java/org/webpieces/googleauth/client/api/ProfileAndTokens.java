package org.webpieces.googleauth.client.api;

public class ProfileAndTokens {
    private final FetchTokenResponse tokens;
    private final UserProfile profile;

    public ProfileAndTokens(FetchTokenResponse tokens, UserProfile profile) {
        this.tokens = tokens;
        this.profile = profile;
    }

    public FetchTokenResponse getTokens() {
        return tokens;
    }

    public UserProfile getProfile() {
        return profile;
    }
}
