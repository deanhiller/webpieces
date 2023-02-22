package org.webpieces.googlecloud.cloudtasks;


import org.webpieces.util.futures.XFuture;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * This API class for API hosted at POST https://reqres.in/api/users
 */
@Path("/api")
public interface UserApi {
    @POST
    @Path("/users")
    XFuture<Void> create(CreateRequest request);
}
