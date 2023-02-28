package org.webpieces.googlecloud.storage.api;

import org.webpieces.util.futures.XFuture;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/something")
public interface DeansCoolApi {

    @POST
    @Path("/test")
    XFuture<DeanResponse> dean(DeanRequest request);
}
