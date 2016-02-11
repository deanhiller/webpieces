package org.playorm.nio.test.suns;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

/**
 * Not a bug, just demonstrating behavior now.
 * 
 * Tests that two connects results in one key, not two.  This is fine.  The
 * selector will refire immediately on next call.  Also once the two connects
 * are accepted(using accept() method on serversocket), the selector blocks
 * until the next connect request.  See the test below.
 * 
 * The return value of selector.select is the number of keys whose ready
 * operations where updated whereas the number of keys in the selectedKey
 * set may be larger because the ready state did not change...they were
 * ready previously and are still ready and have not added new ready
 * operations.
 */
public class TestXDemoMultipleNioConnects extends TestCase {

	private static final Logger log = Logger.getLogger(TestXDemoMultipleNioConnects.class
			.getName());
	private SelectorProvider provider;
	private AbstractSelector selector;
	private ServerSocketChannel serverChannel;
	private SocketChannel client;
	private SocketChannel client2;
	private PollingThread server;
	
	protected void setUp() throws Exception {
		provider = SelectorProvider.provider();
		selector = provider.openSelector();
		serverChannel = provider.openServerSocketChannel();
		client = provider.openSocketChannel();			
		client2 = provider.openSocketChannel();		
		server = new PollingThread();
	}
	
	protected void tearDown() throws Exception {
		server.shutdown();
	}
	
	public void testConnectsInNIO() throws Throwable {
		InetAddress loopBack = InetAddress.getByName("127.0.0.1");
		InetSocketAddress serverAddr = new InetSocketAddress(loopBack, 0);
		InetSocketAddress client1Addr = new InetSocketAddress(loopBack, 0);		
		InetSocketAddress client2Addr = new InetSocketAddress(loopBack, 0);
		
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(serverAddr);
		client.socket().bind(client1Addr);
		client2.socket().bind(client2Addr);

		SocketAddress addr = serverChannel.socket().getLocalSocketAddress();
		log.info("server="+addr);
		
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);		
	
		//start the thread that sleeps, and then blocks on select
		//after the below two socket connects happen
		server.start();

		client.connect(addr);
		client2.connect(addr);
		
		log.info("connected both client sockets");
		
		server.waitForBothSocketAccepts();
	
		log.info("done with test");
	}
	
//	private boolean serverChannelCreated = false;
	private class PollingThread extends Thread {
		private Throwable t = null;
		private SocketChannel[] clientChannels = new SocketChannel[2];
		private boolean acceptedBothSockets = false;
		private boolean shutdown = false;
		
		public void run() {
			try {
				//sleep so both connect requests have already went through.
				Thread.sleep(2000);
				log.info("STARTING RUN LOOP");
				for(int i = 0; true; i++) {
					runLoop(i);
					if(shutdown)
						break;
				}
			} catch (Exception e) {
				log.log(Level.WARNING, "test failure", e);
				t = e;
			}
		}

		/**
		 * 
		 */
		public synchronized void shutdown() {
			log.info("shutting down server");
			shutdown = true;
			this.interrupt();
		}

		/**
		 * @throws Throwable
		 * 
		 */
		public synchronized void waitForBothSocketAccepts() throws Throwable {
			if(!acceptedBothSockets)
				this.wait();
			
			//sleep further to make sure the select doesn't keep going off as
			//we have processed everything.
			Thread.sleep(5000);
			
			if(t != null)
				throw t; //to fail the test case if exception happens on polling thread.
			assertNotNull("clientChannel 1 should be non null", clientChannels[0]);
			assertNotNull("clientChannel 2 should be non null", clientChannels[1]);			
			assertTrue("clientChannel 1 and 2 should be different", clientChannels[0] != clientChannels[1]);
		}

		protected void runLoop(int i) throws Exception{
			log.info("going into selector");
			int	numKeys = selector.select();
			if(i > 1 && numKeys > 0)
				throw new RuntimeException("Failure as we should not pop out of " +
						"the selector when i > 1 except on shutdown which should have 0 keys");
			if(shutdown)
				return;
			
			log.info("coming out with keys="+numKeys);
			Set<SelectionKey> keySet = selector.selectedKeys();
			log.info("keySet size="+keySet.size());
			
			Iterator<SelectionKey> iter = keySet.iterator();	
			SelectionKey theKey = iter.next();
			log.info(i+": in loop iter.next="+theKey+" isVal="+theKey.isValid()+
					" acc="+theKey.isAcceptable()+" read="+theKey.isReadable());
			if(theKey.isAcceptable()) {
				clientChannels[i] = serverChannel.accept();
				synchronized(this) {
					if(i >= 1) {
						acceptedBothSockets = true;
						this.notifyAll();
					}
				}
			}
			if(iter.hasNext())
				throw new RuntimeException("Should only have one key");
			
			log.info(i+":first channel="+clientChannels[i]);		
		}
	}
}
