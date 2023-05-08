package webpiecesxxxxxpackage.deleteme.remoteapi;

import org.webpieces.util.futures.XFuture;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

public interface RemoteApi {

	@POST
	@Path("/fetch/value")
	public XFuture<FetchValueResponse> fetchValue(FetchValueRequest request);

}
