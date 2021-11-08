package webpiecesxxxxxpackage.json;

import org.webpieces.microsvc.api.NotEvolutionProof;

import java.util.concurrent.CompletableFuture;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

public interface SaveApi {

    @POST
    @Path("/search/item")
    public CompletableFuture<SearchResponse> search(SearchRequest request);

    @NotEvolutionProof
    @GET
    @Path("/users/{id}/account/{number}")
    public CompletableFuture<MethodResponse> method(String id, int number);

    @NotEvolutionProof
    @POST
    @Path("/users/{id}/account/{number}")
    public CompletableFuture<PostTestResponse> postTest(String id, int number, PostTestRequest request);
}
