package org.webpieces.googlecloud.cloudtasks;


import org.webpieces.util.futures.XFuture;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/api")
public interface DeansApi {
    @POST
    @Path("/create")
    XFuture<Void> create(CreateRequest request);
}
