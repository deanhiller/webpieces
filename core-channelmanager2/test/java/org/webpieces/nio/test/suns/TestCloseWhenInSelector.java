package org.webpieces.nio.test.suns;

import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import org.slf4j.Logger;

import junit.framework.TestCase;

/**
 * Bug internal review id: 494251
 * 
 * setReuseAddress(true) appears not to work when the close is done 
 * while the selector thread is inside the selector making close not
 * thread safe with the selector.
 */
public class TestCloseWhenInSelector extends TestCase {

	private static final Logger log = Logger
			.getLogger(TestCloseWhenInSelector.class);
	private static final int CLIENT_PORT = 8023;
//	private static final int CLIENT_PORT2 = 8002;	
	private static final int SERVER_PORT = 8020;	
	private SelectorProvider provider;
	private AbstractSelector selector;
	private ServerSocketChannel serverSocket;
//	private SocketChannel serverChannel;
	private SocketChannel client;
//	private SocketChannel client2;
	
	private ByteBuffer buf = ByteBuffer.allocate(10);	
	/**
	 * @param arg0
	 */
	public TestCloseWhenInSelector(String arg0) {
		super(arg0);
	}
	
	protected void setUp() throws Exception {
		provider = SelectorProvider.provider();
		selector = provider.openSelector();
		serverSocket = provider.openServerSocketChannel();
		serverSocket.socket().setReuseAddress(true);
		client = provider.openSocketChannel();			
		client.socket().setReuseAddress(true);
	}
	protected void tearDown() throws Exception {
	}
	
	/**
	 * This is testing a bug in the jdk where setReuseAddress to true is not
	 * working and I cannot rebind to the same address again.
	 * 
	 * @throws Throwable
	 */
	public void testCloseWhenInSelector2() throws Throwable {

		//the try catch is big because it can happen on the first connect or the last one
		//depending on if you have run this test already!!!!
		try {
			InetAddress loopBack = InetAddress.getByName("127.0.0.1");
			InetSocketAddress clientAddr = new InetSocketAddress(loopBack, CLIENT_PORT);		
			InetSocketAddress serverAddr = new InetSocketAddress(loopBack, SERVER_PORT);
			
			//serverSocket.configureBlocking(false);
			serverSocket.socket().bind(serverAddr);
			client.socket().bind(clientAddr);
	
			client.connect(serverAddr);
			client.configureBlocking(false);
			log.info("connecting client socket");
	
			Thread.sleep(1000);
			
			serverSocket.configureBlocking(true);
			log.info("about to accept");
			SocketChannel serverChannel = serverSocket.accept();
			log.info("accepted");
			serverChannel.configureBlocking(false);
			log.info("client socket connected");
			
			serverChannel.register(selector, SelectionKey.OP_READ);
			PollingThread2 server = new PollingThread2();
			server.start();
			
			client.finishConnect();
			
			log.info("write data to server");
			ByteBuffer b = ByteBuffer.allocate(10);
			b.putChar('d');
			b.putChar('e');
			b.flip();
			log.info("write bytes");
			int i = client.write(b);
			log.info("wrote bytes="+i);
			
			//wait for other thread to get into the selector...
			Thread.sleep(1000);
			log.info("1. closing client channel");
			client.close();
			log.info("1. closed client channel");	
	
			server.waitForCompletion();
			
			client = provider.openSocketChannel();			
			client.socket().setReuseAddress(true);
			client.socket().bind(clientAddr);
	
			client.connect(serverAddr);
			client.configureBlocking(false);		
			
			fail("setReuseAddress typically works but does not in this specific instance");
		} catch(BindException e) {
			
		}
		
	}
	
	private class PollingThread2 extends Thread {
		private Throwable t = null;
		private boolean socketClosed = false;

		public void run() {
			try {
				log.info("STARTING RUN LOOP");
			
				while(true) {
					runLoop();
				}
			} catch (Exception e) {
				t = e;
				log.warn("Test failure", e);
			}			
		}
		/**
		 * 
		 */
		public synchronized void waitForCompletion() throws Throwable {
			if(!socketClosed)
				this.wait();
			
			if(t != null)
				throw t;
		}
		
		protected void runLoop() throws Exception {
			log.info("going into selector");
			int numKeys = selector.select();
			log.info("coming out with new keys="+numKeys);
			Set<SelectionKey> keySet = selector.selectedKeys();
			log.info("keySet size="+keySet.size());
			
			Iterator<SelectionKey> iter = keySet.iterator();
			SelectionKey theKey = iter.next();
			log.info("in loop iter.next="+theKey+" isVal="+theKey.isValid()+" acc="+theKey.isAcceptable()+" read="+theKey.isReadable());
			if(theKey.isReadable()) {
				SocketChannel channel = (SocketChannel)theKey.channel();
				log.info("reading bytes");
				int b = 5;
				while(b > 0) {
					b = channel.read(buf);
					if(b < 0) {
						channel.close();
						synchronized(this) {
							socketClosed = true;
							this.notifyAll();
						}
					}
					log.info("bytes read="+b);
				}
			}
			if(iter.hasNext())
				throw new RuntimeException("Fail test, iterator should only have one key");
		}	
	}	
}
