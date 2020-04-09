package webpiecesxxxxxpackage.service;

import java.util.concurrent.CompletableFuture;

public class RemoteServiceImpl implements RemoteService {

	@Override
	public CompletableFuture<Integer> fetchRemoteValue(String s, int i)  {
		//Here a remote service usually returns an uncompleted future and completes it when the remote
		//service returns it's value unblocking this thread for others to use.
		
		//In this case we just return a completely future though.
		return CompletableFuture.completedFuture(33);
	}

	@Override
	public void sendData(int num) {
	}

}
