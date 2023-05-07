package webpiecesxxxxxpackage.json;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.webpieces.util.futures.XFuture;

public interface SearchApi {

    @POST
    @Path("/search/item")
    public XFuture<SearchResponse> search(SearchRequest request);


}
