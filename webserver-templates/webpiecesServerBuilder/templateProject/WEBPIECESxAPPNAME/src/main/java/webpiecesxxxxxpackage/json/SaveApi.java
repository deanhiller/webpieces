package webpiecesxxxxxpackage.json;

import org.webpieces.microsvc.api.Path;

import java.util.concurrent.CompletableFuture;

public interface SaveApi {

    @Path("/search/item")
    public CompletableFuture<SearchResponse> search(SearchRequest request);

}
