package org.webpieces.auth0.api;

import org.webpieces.auth0.client.api.FetchProfileResponse;
import org.webpieces.util.futures.XFuture;

public interface SaveUser {
    XFuture<Void> saveUserIfNotExist(JwtAuth0Body profile);
}
