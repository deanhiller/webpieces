package webpiecesxxxxxpackage.service;

import org.webpieces.util.futures.XFuture;

public interface RemoteService {

	public XFuture<Integer> fetchRemoteValue(String s, int i);
	
	public void sendData(int num);
}
