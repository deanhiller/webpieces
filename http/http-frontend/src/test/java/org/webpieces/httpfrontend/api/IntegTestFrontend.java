package org.webpieces.httpfrontend.api;


public class IntegTestFrontend {
	public static void main(String[] args) throws InterruptedException {
		// Set to true to run h2spec
		ServerFactory.createTestServer(8083, true);
		synchronized (IntegTestFrontend.class) {
			IntegTestFrontend.class.wait();
		}
	}
}
