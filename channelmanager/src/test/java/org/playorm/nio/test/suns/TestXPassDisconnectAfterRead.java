package org.playorm.nio.test.suns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
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
 * Previously internal review id: 498799
 * 
 * Weird....For a while, the selector keeps returning a keyset even after 
 * key is cancelled, in 5.0_04, but not 5.0_03!!!
 * I switched to 5.0_03 and it started working. Switched back to 5.0_04 and it
 * started to work in that version.  I will leave this code around for future use.
 * 
 * @author Dean Hiller
 */
public class TestXPassDisconnectAfterRead extends TestCase {

	private static final Logger log = Logger.getLogger(TestXPassDisconnectAfterRead.class.getName());
	private SelectorProvider provider;
	private AbstractSelector selector;
	private ServerSocketChannel server;
	private SocketChannel serverChannel;
	private SocketChannel client;
//	private String lastLock = "xyz";
	
	private ByteBuffer buf = ByteBuffer.allocate(10);
	/**
	 * @param name
	 */
	public TestXPassDisconnectAfterRead(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		provider = SelectorProvider.provider();
		selector = provider.openSelector();
		server = provider.openServerSocketChannel();
		client = provider.openSocketChannel();			
	}
	
	private boolean receivedWrite = false;
	private boolean closeHappened = false;
	public void testDisconnectAfterReadResultsInNewKey() throws Exception {
		InetAddress loopBack = InetAddress.getByName("127.0.0.1");
		InetSocketAddress clientAddr = new InetSocketAddress(loopBack, 0);		
		InetSocketAddress serverAddr = new InetSocketAddress(loopBack, 0);
		
		server.configureBlocking(false);
		server.socket().bind(serverAddr);
		client.socket().bind(clientAddr);
		
		server.register(selector, SelectionKey.OP_ACCEPT);
		
		new PollingThread2().start();
		
		SocketAddress addr = server.socket().getLocalSocketAddress();
		client.connect(addr);
		
		log.info("configure client non-blocking");
		client.configureBlocking(false);
		//client.register(selector, SelectionKey.OP_READ);
		
//		Thread.sleep(2000);
		log.info("write data to server");
		ByteBuffer b = ByteBuffer.allocate(10);
		b.putChar('d');
		b.putChar('e');
		b.flip();
		log.info("write bytes");
		int i = client.write(b);
		log.info("wrote bytes="+i);
		
		synchronized(client) {
			log.info("waiting for server to tell us received data from read");
			if(!receivedWrite)
				client.wait();
		}
		
		client.close();
		
		synchronized(client) {
			log.info("close done, notify server");			
			closeHappened = true;
			client.notify();
		}

		log.info("sleeping");
		//will block forever when bug is fixed.....
//		synchronized(lastLock) {
//			if(!isThreadDone)
//				lastLock.wait();
//		}
		log.info("result="+result);

		Thread.sleep(5000);	
		log.info("test done");
	}


	
	//private boolean isThreadDone = false;
	private int result;
	
	private class PollingThread2 extends Thread {
		public void run() {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			log.info("STARTING RUN LOOP");
			int i = 0;
			while(true) {
				i++;
				runLoop2();
				if(i > 10)
					break;
			}
//			synchronized (lastLock) {
//				result = i;
//				isThreadDone = true;
//				lastLock.notifyAll();
//			}
		}
	}
	protected void runLoop2() {
		int numKeys = 0;
		try {
			log.info("going into selector");
			numKeys = selector.select();
			log.info("coming out with keys="+numKeys);
		} catch (IOException e) {
			log.log(Level.WARNING, "Having trouble with a channel", e);
		}
		Set<SelectionKey> keySet = selector.selectedKeys();
		Iterator<SelectionKey> iter = keySet.iterator();	
		while (iter.hasNext()) {
			try {
				SelectionKey theKey = iter.next();
				log.info("in loop iter.next="+theKey+" isVal="+theKey.isValid()+" acc="
						+theKey.isAcceptable()+" read="+theKey.isReadable());
				if(theKey.isAcceptable()) {
					SocketChannel temp = server.accept();
					if(temp != null) {
						serverChannel = temp;
						serverChannel.configureBlocking(false);
						log.info("register serverChannel");
						serverChannel.register(selector, SelectionKey.OP_READ);
					}
				} else if(theKey.isReadable()) {
					log.info("reading bytes");
					buf.clear();
					int bytes = serverChannel.read(buf);
					log.info("read bytes="+bytes);
					iter.remove();
					if(bytes < 0)
						theKey.cancel();
					//keySet.remove(theKey);					
					synchronized(client) {
						log.info("waiting for write");
						receivedWrite = true;
						client.notify();
					}
					synchronized(client) {
						log.info("received write but not close yet, wait for client to call close");
						if(!closeHappened)
							client.wait();
					}
					log.info("done waiting for close, it happened");
				}
			} catch(Throwable e) {
				log.log(Level.WARNING, "Processing of key failed, but continuing channel manager loop", e);
			}
		}
		keySet.clear();
	}		
}
