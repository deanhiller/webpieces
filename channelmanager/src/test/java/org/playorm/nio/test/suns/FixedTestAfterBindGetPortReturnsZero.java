package org.playorm.nio.test.suns;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import junit.framework.TestCase;


/**
 * This is fixed in jdk1.5.0_08.  We have not changed channelmanager to start using it again
 * yet though!!!
 * 
 * External Bug ID: 4739238
 * 
 * This test proves getting the port after a SocketChannel is bound return 0 instead
 * of the port it was bound too and proves DatagramChannel returns the port that
 * it was actually bound to.
 * 
 * TODO: need to write test to get binding to port 0 and see on linux if
 * it returns the port instead of 0 with getLocalPort.
 */
public class FixedTestAfterBindGetPortReturnsZero extends TestCase {

	private static final Logger log = Logger.getLogger(FixedTestAfterBindGetPortReturnsZero.class.getName());
	
	
	/**
	 * @param name
	 */
	public FixedTestAfterBindGetPortReturnsZero(String name) {
		super(name);	
	}

	
	/**
	 * External Bug ID: 4739238
	 * This tests that after the TCP bind on port 0(which results in grabbing
	 * any available port) that getLocalPort returns 0 instead of the port
	 * we bound to like ServerSocket does.  This test also proves 
	 * DatagramChannel does this correctly and returns the port that 
	 * the jdk bound to.
	 */
	public void testAfterBindGetPortReturnsZero() throws Exception {
		String fixVersion = "1.6.0_02";
		String jdkVersion = System.getProperty("java.vm.version");
		
		if (jdkVersion.compareTo(fixVersion) >= 0)
			return;
		
		SocketChannel chan1 = SocketChannel.open();
		DatagramChannel chan2 = DatagramChannel.open();
		chan1.socket().setReuseAddress(true);
		chan2.socket().setReuseAddress(true);
		chan1.configureBlocking(false);
		chan2.configureBlocking(false);
		
		InetAddress loopBack = InetAddress.getByName("127.0.0.1");
		InetSocketAddress addr = new InetSocketAddress(loopBack, 0);
		chan1.socket().bind(addr);
		chan2.socket().bind(addr);
		
		//port doesn't return the port we bound too for tcp, but
		//does for udp
		int port1 = chan1.socket().getLocalPort();
		int port2 = chan2.socket().getLocalPort();
		assertTrue("TCP should not return 0 but does", port1 == 0);
		assertTrue("Udp should not return 0 and doesn't(good)", port2 != 0);

		SocketAddress tcpAddr = chan1.socket().getLocalSocketAddress();
		log.info("addr="+tcpAddr);
		InetSocketAddress expected = new InetSocketAddress(loopBack, 0);
		assertEquals("local addr port is returning 0, this is bad", expected, tcpAddr);
	}
	
}
