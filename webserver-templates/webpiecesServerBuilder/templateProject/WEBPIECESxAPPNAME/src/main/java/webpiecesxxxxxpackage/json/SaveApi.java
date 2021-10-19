package webpiecesxxxxxpackage.json;

import java.util.concurrent.CompletableFuture;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

public interface SaveApi {

    @POST
    @Path("/search/item")
    public CompletableFuture<SearchResponse> search(SearchRequest request);

}
