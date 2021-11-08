package webpiecesxxxxxpackage.json;

import org.webpieces.microsvc.api.NotEvolutionProof;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.concurrent.CompletableFuture;

@NotEvolutionProof
public interface ExampleRestAPI {

    @GET
    @Path("/users/{id}/account/{number}")
    public CompletableFuture<MethodResponse> method(String id, int number);

    @POST
    @Path("/users/{id}/account/{number}")
    public CompletableFuture<PostTestResponse> postTest(String id, int number, PostTestRequest request);

}
