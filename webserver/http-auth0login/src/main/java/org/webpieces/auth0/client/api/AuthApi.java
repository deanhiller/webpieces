package org.webpieces.auth0.client.api;

import org.webpieces.microsvc.api.NotEvolutionProof;
import org.webpieces.util.futures.XFuture;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@NotEvolutionProof
public interface AuthApi {

    @POST
    @Path("/oauth/token")
    public XFuture<FetchTokenResponse> fetchToken(FetchTokenRequest request);

    @GET
    @Path("/userinfo")
    public XFuture<FetchProfileResponse> fetchProfile();

}
