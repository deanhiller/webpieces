package org.webpieces.auth0.mgmt.api;

import org.webpieces.microsvc.api.NotEvolutionProof;
import org.webpieces.util.futures.XFuture;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@NotEvolutionProof
public interface AuthManagementApi {

//
//    @Wrap(List<User>) // need a way to return ListUserResponse which has an array but body is an array.
//    @GET
//    @Path("/api/v2/users-by-email")
//    public XFuture<List<FetchUserResponse>> listUser(String email, @QueryParam("include_fields") Boolean includeFields, String fields);

    @GET
    @Path("/api/v2/users/{id}")
    public XFuture<FetchUserResponse> fetchUser(
            String id,
            @QueryParam("fields") String fields,
            @QueryParam("include_fields") Boolean includeFields
    );

}
