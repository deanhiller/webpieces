package webpiecesxxxxxpackage.service;

import org.webpieces.util.futures.XFuture;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

public interface RemoteService {

	@POST
	@Path("/fetch/value")
	public XFuture<FetchValueResponse> fetchValue(FetchValueRequest request);

	@POST
	@Path("/send/data")
	public XFuture<SendDataResponse> sendData(SendDataRequest num);

}
