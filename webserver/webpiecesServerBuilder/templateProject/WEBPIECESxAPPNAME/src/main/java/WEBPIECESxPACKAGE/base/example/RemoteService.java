package WEBPIECESxPACKAGE.base.example;

import java.util.concurrent.CompletableFuture;

public class RemoteService {

	public CompletableFuture<Integer> fetchRemoteValue() {
		//Here a remote service usually returns an uncompleted future and completes it when the remote
		//service returns it's value unblocking this thread for others to use.
		
		//In this case we just return a completely future though.
		return CompletableFuture.completedFuture(33);
	}
}
