package org.playorm.nio.test.suns;

import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import junit.framework.TestCase;

/**
 * Need to run on linux to see if fixed again so can file bug report.
 * 
 * This test proves that on linux, you can bind to an invalid ip.  You can bind to
 * an address that neither nic has, and then when you connect you get a BindException.
 * On windows, you correctly get the BindException when trying to bind to an 
 * invalid ip.
 * 
 * TODO: need to write test to get binding to port 0 and see on linux if
 * it returns the port instead of 0 with getLocalPort.
 */
public class TestBindToWrongIp extends TestCase {

	private static final Logger log = Logger.getLogger(TestBindToWrongIp.class.getName());
	private boolean isWindows;
	private boolean isLinux;
	
	
	/**
	 * @param name
	 */
	public TestBindToWrongIp(String name) {
		super(name);	
	}

	public void setUp() {
		String os = System.getProperty("os.name");
		//String osArch = System.getProperty("os.arch");
		log.info("os="+os);			
		if(os.matches(".*Windows.*")) {
			log.info("RUNNING WINDOWS TESTS ONLY");
			isWindows = true;
		} else {
			log.info("RUNNING LINUX TESTS ONLY");
			isLinux = true;
		}

	}
	
	/**
	 * This tests BindException thrown on connect when socket is bound
	 * to a wrong but existing ip address.
	 * 
	 * I believe this happens on linux.  On Windows, it happens on the bind.
	 * @throws Exception
	 */
	public void testBindExceptionOnConnect2() throws Exception {
		InetAddress svrLoopBack = InetAddress.getByName("127.0.0.1");
		InetSocketAddress svrAddr = new InetSocketAddress(svrLoopBack, 5000);
		if(isLinux) {
			SocketChannel channel1 = SocketChannel.open();
			channel1.socket().setReuseAddress(true);
			
			InetAddress loopBack = InetAddress.getByName("24.8.32.1");
			InetSocketAddress addr1 = new InetSocketAddress(loopBack, 0);			
	
			try {
				channel1.socket().bind(addr1);	
				
				//on linux, this used to throw the bind exception instead of the bind...
				//channel1.connect(svrAddr); //results in BindException		
				fail("Should have thrown a BindException");
			} catch(BindException e) {
				//gulp
			}
		} else if(isWindows) {
			SocketChannel channel1 = SocketChannel.open();
			channel1.socket().setReuseAddress(true);
			
			InetAddress loopBack = InetAddress.getByName("24.8.32.1");
			InetSocketAddress addr1 = new InetSocketAddress(loopBack, 0);		
			try {
				channel1.socket().bind(addr1);
				fail("Should have thrown a BindException since we are binding to an invalid address");
			} catch(BindException e) {
				//gulp
			}
		}
	}	
}
