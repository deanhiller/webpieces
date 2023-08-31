package org.webpieces.auth0.client.api;

import com.google.inject.ImplementedBy;
import org.webpieces.auth0.impl.*;
import org.webpieces.util.futures.XFuture;

@ImplementedBy(AuthApiImpl.class)
public interface AuthApi {

    public XFuture<AuthResponse> codeToTokens(AuthRequest request);

    public XFuture<UserProfile> fetchProfile(FetchProfileRequest bearerToken);
}
