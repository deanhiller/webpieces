package webpiecesxxxxxpackage.json;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.concurrent.CompletableFuture;

public interface SaveApi {

    @POST
    @Path("/search/item")
    public CompletableFuture<SearchResponse> search(SearchRequest request);


}
