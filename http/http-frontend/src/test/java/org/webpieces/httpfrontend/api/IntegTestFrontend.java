package org.webpieces.httpfrontend.api;


public class IntegTestFrontend {
	public static void main(String[] args) throws InterruptedException {
		ServerFactory.createTestServer(8083, false);
		synchronized (IntegTestFrontend.class) {
			IntegTestFrontend.class.wait();
		}
	}
}
