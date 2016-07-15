package PACKAGE;

import java.util.concurrent.CompletableFuture;

import PACKAGE.example.RemoteService;

public class MockRemoteSystem extends RemoteService {

	private CompletableFuture<Integer> futureToReturn;

	public void addValueToReturn(CompletableFuture<Integer> future) {
		this.futureToReturn = future;
	}

	@Override
	public CompletableFuture<Integer> fetchRemoteValue() {
		return futureToReturn;
	}
	
}
