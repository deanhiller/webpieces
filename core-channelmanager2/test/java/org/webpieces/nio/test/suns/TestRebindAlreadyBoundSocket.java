package org.webpieces.nio.test.suns;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

import junit.framework.TestCase;

/**
 * Bug ID: 498397
 *
 * This test proves the jdk throws an Error when it should be throwing a 
 * normal Exception due to rebinding an already bound socket.
 * 
 * TODO: need to write test to get binding to port 0 and see on linux if
 * it returns the port instead of 0 with getLocalPort.
 */
public class TestRebindAlreadyBoundSocket extends TestCase {

	//private static final Logger log = LoggerFactory.getLogger(TestBindError.class);
	
	
	/**
	 * @param name
	 */
	public TestRebindAlreadyBoundSocket(String name) {
		super(name);		
	}

	public void setUp() {

	}
	
	public void tearDown() {

	}
	
	public void testBindError() throws Exception {
		InetAddress loopBack = InetAddress.getByName("127.0.0.1");
		SocketChannel chan1 = SocketChannel.open();
		chan1.configureBlocking(false);
		
		InetSocketAddress addr1 = new InetSocketAddress(loopBack, 10001);
		InetSocketAddress addr2 = new InetSocketAddress(loopBack, 9999);
		chan1.socket().bind(addr1);	
//NOTE: At home I keep the try catch so my test passes until the bug is fixed.  This
//way, when I upgrade to a new jdk and some tests fail, I get happy :) and get rid of
//these try catchs that exist at home.
		try {
			chan1.socket().bind(addr2);
			fail("Previously has thrown untranslated exception Error");
		} catch(SocketException e) {
			e.printStackTrace();
			//gulp
		}
		
		chan1.close();
	}	
}
