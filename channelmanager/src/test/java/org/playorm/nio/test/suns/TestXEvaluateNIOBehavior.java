package org.playorm.nio.test.suns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.playorm.nio.api.deprecated.ChannelManager;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.testutil.MockNIOServer;

import biz.xsoftware.mock.ExpectFailedException;

/**
 * 
 */
public class TestXEvaluateNIOBehavior extends TestCase {

	private static final Logger log = Logger.getLogger(TestXEvaluateNIOBehavior.class.getName());
	
	private MockNIOServer mockServer;
	private InetSocketAddress svrAddr;

	private BufferFactory bufFactory;
//	private boolean isSolaris = false;
//	private boolean isWindows;
	private boolean isLinux;
	
	
	/**
	 * @param name
	 */
	public TestXEvaluateNIOBehavior(String name) {
		super(name);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(FactoryCreator.KEY_IS_DIRECT, false);
		FactoryCreator creator = FactoryCreator.createFactory(null);
		bufFactory = creator.createBufferFactory(map);			
	}

	public void setUp() {
		String os = System.getProperty("os.name");
		//String osArch = System.getProperty("os.arch");
		log.info("os="+os);			
		if(os.equals("linux")) {
			log.info("RUNNING LINUX TESTS ONLY");
			isLinux = true;
		}		
		try {
			if(mockServer == null) {
				ChannelServiceFactory factory = ChannelServiceFactory.createFactory(null);
				Map<String, Object> p = new HashMap<String, Object>();
				p.put(ChannelManager.KEY_ID, "[server]");
				p.put(ChannelManager.KEY_BUFFER_FACTORY, bufFactory);
				ChannelService chanMgr = factory.createChannelManager(p);				
				mockServer = new MockNIOServer(chanMgr, null);
			}
			svrAddr = mockServer.start();
			log.info("svrPort="+svrAddr);

		} catch(IOException e) {
			throw new RuntimeException("test failed", e);
		} catch (InterruptedException e) {
			throw new RuntimeException("test failed2", e);
		}
	}
	
	public void tearDown() {
		try {
			mockServer.stop();
		} catch(IOException e) {
			throw new RuntimeException("test failed", e);
		} catch (InterruptedException e) {
			throw new RuntimeException("test failed", e);
		}
	}
	
	/**
	 * This test is interesting.  If a Socket is registered with a Selector, and
	 * you close the socket, the socket cannot be closed until the select statement.
	 * 
	 * NOTE: Appears to be fixed in 1.5.0_06 on windows, so we only run this on
	 * linux now!!!!!!!!!
	 * 
	 * @throws Exception
	 */
	public void testNormalNio() throws Exception {	
		SocketChannel channel1 = SocketChannel.open();
		channel1.configureBlocking(false);
		channel1.socket().setReuseAddress(true);
		
		InetAddress loopBack = InetAddress.getByName("127.0.0.1");
		InetSocketAddress addr1 = new InetSocketAddress(loopBack, 0);		
		channel1.socket().bind(addr1);

		channel1.connect(svrAddr);
		channel1.finishConnect();

		mockServer.expect(MockNIOServer.CONNECTED);
		
		SelectorProvider prov = SelectorProvider.provider();
		AbstractSelector sel = prov.openSelector();
		log.info("about to register client");
		channel1.register(sel, SelectionKey.OP_READ);
		log.info("registered channel");
		
		log.info("about to close------------------");
		channel1.close();
		log.info("channel is now closed-----------");

		mockServer.expect(MockNIOServer.FAR_END_CLOSED);
	}	
}
