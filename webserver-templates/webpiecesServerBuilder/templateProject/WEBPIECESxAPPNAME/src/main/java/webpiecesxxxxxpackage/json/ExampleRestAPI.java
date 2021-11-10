package webpiecesxxxxxpackage.json;

import org.webpieces.microsvc.api.NotEvolutionProof;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.webpieces.util.futures.XFuture;

@NotEvolutionProof
public interface ExampleRestAPI {

    @GET
    @Path("/users/{id}/account/{number}")
    public XFuture<MethodResponse> method(String id, int number);

    @POST
    @Path("/users/{id}/account/{number}")
    public XFuture<PostTestResponse> postTest(String id, int number, PostTestRequest request);

}
