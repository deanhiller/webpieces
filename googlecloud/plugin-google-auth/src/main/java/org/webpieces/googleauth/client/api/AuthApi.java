package org.webpieces.googleauth.client.api;

import org.webpieces.microsvc.api.NotEvolutionProof;
import org.webpieces.util.futures.XFuture;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@NotEvolutionProof
public interface AuthApi {

    //https://cloud.google.com/iam/docs/reference/sts/rest/v1/TopLevel/token ->
    //https://sts.googleapis.com/v1/token
    //https://accounts.google.com/.well-known/openid-configuration ->
    //https://oauth2.googleapis.com/token
    @POST
    @Path("/token")
    public XFuture<FetchTokenResponse> fetchToken(FetchTokenRequest request);

    /**
     * NOT FOR Production USE.  google throttles this REST api
     * See documentation here https://developers.google.com/identity/sign-in/web/backend-auth
     * @param idToken
     * @return
     */
    @GET
    @Path("/tokeninfo")
    public XFuture<FetchProfileResponse> fetchProfile(@QueryParam("id_token") String idToken);

}
