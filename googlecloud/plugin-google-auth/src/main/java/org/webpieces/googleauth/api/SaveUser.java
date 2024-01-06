package org.webpieces.googleauth.api;

import org.webpieces.googleauth.client.api.FetchTokenResponse;
import org.webpieces.googleauth.client.api.ProfileAndTokens;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.util.futures.XFuture;

public interface SaveUser {
    XFuture<Void> saveUserIfNotExist(ProfileAndTokens profile);

    Redirect returnRedirectIfScopesInvalid(FetchTokenResponse resp);

}
