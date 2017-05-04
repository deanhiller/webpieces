package org.webpieces.util.binding;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

/**
 * This is a cheat as nothing sucks more than running a long build and get to the end only to find out
 * you are running a dev server on port 8080 already so it fails to start the server.  instead check early on
 * in the process so we can kill the server
 * 
 */
public class TestBind {

	@Test
	public void testFilters() throws InterruptedException, ExecutionException, IOException {
		ServerSocket s = new ServerSocket();
		s.setReuseAddress(true);
		s.bind(new InetSocketAddress(8080));
		
		s.close();

	}
}
