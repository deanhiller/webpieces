package org.webpieces.httpfrontend.api;


public class IntegTestFrontend {
	public static void main(String[] args) throws InterruptedException {
		// Set to true to run h2spec
		int port = ServerFactory.createTestServer(true);
		System.out.println("Server running on port: " + port);
		synchronized (IntegTestFrontend.class) {
			IntegTestFrontend.class.wait();
		}
	}
}
