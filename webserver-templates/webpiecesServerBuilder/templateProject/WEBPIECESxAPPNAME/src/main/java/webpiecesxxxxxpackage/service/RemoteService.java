package webpiecesxxxxxpackage.service;

import java.util.concurrent.CompletableFuture;

public interface RemoteService {

	public CompletableFuture<Integer> fetchRemoteValue(String s, int i);
	
	public void sendData(int num);
}
