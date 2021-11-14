package webpiecesxxxxxpackage.service;

import org.webpieces.util.futures.XFuture;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

public interface RemoteService {

	@POST
	@Path("/search/item")
	public XFuture<Integer> fetchRemoteValue(String s, int i);
	
	public void sendData(int num);
}
