package org.webpieces.googlecloud.cloudtasks;


import org.webpieces.util.futures.XFuture;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/publishsvc")
public interface DeansApi {
    @POST
    @Path("/some")
    XFuture<Void> some(SomeRequest request);
}
