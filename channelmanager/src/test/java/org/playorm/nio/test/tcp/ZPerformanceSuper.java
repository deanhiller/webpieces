package org.playorm.nio.test.tcp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.deprecated.ConnectionCallback;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.handlers.FutureOperation;
import org.playorm.nio.api.handlers.OperationCallback;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.testutil.CloneByteBuffer;
import org.playorm.nio.api.testutil.HandlerForTests;
import org.playorm.nio.test.EchoServer;
import org.playorm.nio.test.PerfTimer;

import biz.xsoftware.mock.CalledMethod;
import biz.xsoftware.mock.MockObject;
import biz.xsoftware.mock.MockObjectFactory;

public abstract class ZPerformanceSuper extends TestCase {

	private static final Logger log = Logger.getLogger(ZPerformanceSuper.class.getName());
	
	private BufferFactory bufFactory;
	
	private InetSocketAddress svrAddr;
	private ChannelService chanMgr;
	//private InetAddress loopBack;
	//private InetSocketAddress loopBackAnyPort;
	private BufferHelper helper = ChannelServiceFactory.bufferHelper(null);

	private MockObject mockHandler;
	private MockObject mockConnect;
	private EchoServer echoServer;

	private MockObject mockConnectOp;
	
	protected abstract ChannelService getClientChanMgr();
	protected abstract ChannelService getServerChanMgr();
	protected abstract Settings getServerFactoryHolder();
	protected abstract Settings getClientFactoryHolder();		
	protected abstract String getChannelImplName();
	
	public ZPerformanceSuper(String arg0) {
		super(arg0);
		if(bufFactory == null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(FactoryCreator.KEY_IS_DIRECT, false);
			FactoryCreator creator = FactoryCreator.createFactory(null);
			bufFactory = creator.createBufferFactory(map);			
		}		
	}
	
	protected void setUp() throws Exception {
		HandlerForTests.setupLogging();
		Logger.getLogger("").setLevel(Level.INFO);
		//here I keep using the same channel manager on purpose, just
		//so we get testing between tests that the channel manager shutdown
		//and started back up cleanly.....
		if(chanMgr == null) {
			chanMgr = getClientChanMgr();
		}
		if(echoServer == null) {
			ChannelService svrChanMgr = getServerChanMgr();
			echoServer = new EchoServer(svrChanMgr, getServerFactoryHolder());
		}
		chanMgr.start();		
		svrAddr = echoServer.start();
//this is to test EchoServer on a different machine...
//		InetAddress tmp = InetAddress.getByName("192.168.1.102");
//		svrAddr = new InetSocketAddress(tmp, 501);
		
		log.fine("server port ="+svrAddr);
		
		//loopBack = InetAddress.getByName("127.0.0.1");	
		//loopBackAnyPort = new InetSocketAddress(loopBack, 0);
		
		mockHandler = MockObjectFactory.createMock(DataListener.class);
		mockHandler.setDefaultBehavior("incomingData", new CloneByteBuffer());
		mockConnect = MockObjectFactory.createMock(ConnectionCallback.class);
		mockConnectOp = MockObjectFactory.createMock(OperationCallback.class);
	}
	
	protected void tearDown() throws Exception {
		chanMgr.stop();
		chanMgr = null;
		echoServer.stop();
		
		HandlerForTests.checkForWarnings();
        Thread.sleep(1000);
		Logger.getLogger("").setLevel(Level.FINEST);
	}

	protected abstract int getBasicConnectTimeLimit();
	protected abstract int getSmallReadWriteTimeLimit();
	protected abstract int getLargerReadWriteTimeLimit();
	
	/**
	 * Testing difference between secureChanMgr with Basic, and just Basic
	 * and whatever other combinations are in PerfTestZ<Subclassname>.  Here
	 * we are testing the performance of connect method.  As you can see the
	 * establishing of a link is very expensive. 57 times difference!
	 * 
	 * Basic ChannelMgr....
	 * time for initiating all connects           = 180 ms
	 * time for initiating/finishing all connects = 180 ms
	 * time per connection                        = 4 ms
	 * 
	 * Secure ChannelMgr....
	 * time for initiating all connects           = 8,072 ms
	 * time for initiating/finishing all connects = 9,174 ms
	 * time per connection                        = 229 ms
	 * 
	 * 
	 * If you want to test difference between asynch connect and connect, you
	 * need to simulate a network delay in EchoServer class.  
	 * I did this and simulated one second.
	 * Then changing the connect from synchronous to asynchronous results in going
	 * from 7.401 seconds to .531 seconds.  ie. the thread calling connect is free
	 * to go do more work while in the process of connecting to a server or
	 * client for that matter.
	 */
	public void testBasicConnect() throws Exception {	
		int size = 40;
		String[] methodNames = new String[size];
		for(int i = 0; i < size; i++) {
			methodNames[i] = "finished";
		}

		TCPChannel[] clients = new TCPChannel[size];
		for(int i = 0; i < size; i++) {
			clients[i] = chanMgr.createTCPChannel("Client["+i+"]", getClientFactoryHolder());
		}
		
		PerfTimer timer = new PerfTimer();
		PerfTimer timer2 = new PerfTimer();
		log.info("Starting test connecting to="+svrAddr);
		timer.start();
		timer2.start();
		for(int i = 0; i < size; i++) {
			FutureOperation future = clients[i].connect(svrAddr);
			future.setListener((OperationCallback) mockConnectOp);
		}
		long result2 = timer2.stop();
		
		mockConnectOp.expect(methodNames);
		
		long result = timer.stop();

		long timePerConnect = result/size;
		log.info("time for initiating connects          ="+result2);
		log.info("time for initiating/finishing connects="+result);
		log.info("connected per connection time         ="+timePerConnect);		
		log.info("--time to beat           ="+getBasicConnectTimeLimit());
		assertTrue(timePerConnect < getBasicConnectTimeLimit());
		
	}

        /**
         * Test used to test with netbeans profiler.  Was seeing bizarre results where profiler
         * claimed server selector thread was stuck on a monitor for 10 seconds but this is 
         * never shown in the snapshot, and is very bizarre.  The most time spent in one
         * of my methods if looked at the hotspot is from the main test thread, not the server
         * selector thread which is bizarre as that thread showed no indication of being on a
         * monitor.
         */
	public void profilerTestConnectCloseBatches() throws Exception {	
		try {
			log.info("test beginning");
			int size = 10;
			String[] methodNames = new String[size];
			for(int i = 0; i < size; i++) {
				methodNames[i] = "connected";
			}
	
			for(int j = 0; j < 10000; j++) {
				TCPChannel[] clients = new TCPChannel[size];
				for(int i = 0; i < size; i++) {
					clients[i] = chanMgr.createTCPChannel("Client["+i+"]", getClientFactoryHolder());
				}
			
				for(int i = 0; i < size; i++) {
					clients[i].oldConnect(svrAddr, (ConnectionCallback)mockConnect);
				}
			
				mockConnect.expect(methodNames);
				
				for(TCPChannel channel : clients) {
					channel.oldClose();
				}
			}
		} catch(Exception e) {
			log.log(Level.WARNING, "the exception", e);
		} finally {
			log.warning("test ending*********************");
		}
//		long timePerConnect = result/size;
//		log.info("time for initiating connects          ="+result2);
//		log.info("time for initiating/finishing connects="+result);
//		log.info("connected per connection time         ="+timePerConnect);		
//		log.info("--time to beat           ="+getBasicConnectTimeLimit());
	//	assertTrue(timePerConnect < getBasicConnectTimeLimit());
		
	}
	
	/**
	 * This is the difference in performance of writing/reading secure data vs.
	 * writing/reading non-secure data for a VERY SMALL payload.  Realize though,
	 * in this test, the data is encrypted, decrypted, encrypted again, and 
	 * decrypted again, so the server takes half this load, and the client the
	 * other half.  
	 * Basic seems to be 75% of secure's time. This is a slowdown of 133% 
	 * for echoing 'hello'.
	 * 
     * Basic....
     * total write time         =1732 ms
     * total write/response time=1802 ms
     * time per write/response  =45   ms
     * Secure....
     * total write time         =2374 ms
     * total write/response time=2424 ms
     * time per write/response  =60   ms
     * Basic with network delay of 1 seconds....
     * total write time         =1522 ms
     * total write/response time=3585 ms
     * time per write/response  =89   ms
	 * @throws Exception
	 */
	public void testVerySmallReadWrite() throws Exception {
		ByteBuffer b = ByteBuffer.allocate(4000);
		
		log.info("getting all proper connections");
		int size = 40;
		String[] methodNames = new String[size];
		for(int i = 0; i < size; i++) {
			methodNames[i] = "finished";
		}
		TCPChannel[] clients = new TCPChannel[size];	
		for(int i = 0; i < size; i++) {
			clients[i] = chanMgr.createTCPChannel("Client["+i+"]", getClientFactoryHolder());
			FutureOperation future = clients[i].connect(svrAddr);
			future.setListener((OperationCallback) mockConnectOp);
		}
		mockConnectOp.expect(methodNames);
		log.info("done getting all connections");
		
		for(TCPChannel client : clients) {
			client.registerForReads((DataListener)mockHandler);
		}
		
		int numWrites = 200;
		String payload = "hello";
		helper.putString(b, payload);
		helper.doneFillingBuffer(b);
		int numBytes = b.remaining();
		methodNames = new String[size];
		for(int i = 0; i < size; i++) {
			methodNames[i] = "incomingData";
		}		
		
		String[] finNames = new String[size];
		for(int i = 0;i < size;i++) {
			finNames[i] = "finished";
		}
		
		PerfTimer timer = new PerfTimer();
		PerfTimer timer2 = new PerfTimer();
		timer.start();
		timer2.start();

		CalledMethod[] methods = null;
		for(int i = 0; i < numWrites; i++) {
			for(TCPChannel client : clients) {
				FutureOperation write = client.write(b);
				write.setListener((OperationCallback) mockConnectOp);
				//client.oldWrite(b);
				b.rewind();
			}
			mockConnectOp.expect(finNames);
			methods = mockHandler.expect(methodNames);
		}
		long result2 = timer2.stop();
		long result = timer.stop();
		
		//pick a method and verify right data came back for performance test
		//to make sure performance test is valid....
		ByteBuffer actualBuf = (ByteBuffer)methods[5].getAllParams()[1];
		String actual = helper.readString(actualBuf, actualBuf.remaining());
		assertEquals(payload, actual);
		log.info("payload="+actual);
		
		long readWriteTime = result/size;
		long byteTime = 100*result / (numWrites*numBytes);
		log.info("total write time         ="+result2);		
		log.info("total write/read time    ="+result);
		log.info("--time per 100 bytes     ="+byteTime);
        log.info("test result info:");
        log.info("--time per write/read    ="+readWriteTime);
		log.info("  time to beat           ="+getSmallReadWriteTimeLimit());
		assertTrue(readWriteTime < getSmallReadWriteTimeLimit());
	}
	
	/**
	 * This is the difference in performance of writing/reading secure data vs.
	 * writing/reading non-secure data for a very small payload.  
	 * Basic seems to be 75% of secure's time. This is a slowdown of 133% 
	 * for echoing 'hello'.
	 * 
     * Basic....
     * total write time         = 1402 ms
     * total write/response time= 1433 ms
     * time per write/response  = 35   ms

     * Secure....
     * total write time         = 6119 ms
     * total write/response time= 6159 ms
     * time per write/response  = 153 ms
     *
	 * @throws Exception
	 */
	public void testLargeReadWrite() throws Exception {
		ByteBuffer b = ByteBuffer.allocate(4000);
		
		log.info("getting all proper connections");
		int size = 40;
		String[] methodNames = new String[size];
		for(int i = 0; i < size; i++) {
			methodNames[i] = "connected";
		}
		TCPChannel[] clients = new TCPChannel[size];	
		for(int i = 0; i < size; i++) {
			clients[i] = chanMgr.createTCPChannel("Client["+i+"]", getClientFactoryHolder());
			clients[i].oldConnect(svrAddr, (ConnectionCallback)mockConnect);
		}
		mockConnect.expect(methodNames);
		log.info("done getting all connections");
		
		for(TCPChannel client : clients) {
			client.registerForReads((DataListener)mockHandler);
		}
		
		int numWrites = 100;
		String payload = "hello";
		for(int i = 0; i < 3000; i++) {
			payload+="i";
		}
		helper.putString(b, payload);
		helper.doneFillingBuffer(b);
		int numBytes = b.remaining();
		log.info("size="+b.remaining());
		methodNames = new String[size*numWrites];
		for(int i = 0; i < size*numWrites; i++) {
			methodNames[i] = "incomingData";
		}		
		
		PerfTimer timer = new PerfTimer();
		PerfTimer timer2 = new PerfTimer();
		timer.start();
		timer2.start();
		for(TCPChannel client : clients) {
			for(int i = 0; i < numWrites; i++) {
				FutureOperation future = client.write(b);
				future.waitForOperation(5000);
				b.rewind();
			}
		}
		long result2 = timer2.stop();
		CalledMethod[] methods = mockHandler.expect(methodNames);
		long result = timer.stop();
		
		ByteBuffer actualBuf = (ByteBuffer)methods[6].getAllParams()[1];
		String actual = helper.readString(actualBuf, actualBuf.remaining());
		assertEquals(payload, actual);
		log.info("payload="+actual);	
		
		long readWriteTime = result/size;
		long byteTime = 100*result / (numWrites*numBytes);
		log.info("total write time         ="+result2);		
		log.info("total write/read time    ="+result);
		log.info("--time per 100 bytes     ="+byteTime);
        log.info("test result info:");
        log.info("--time per write/read    ="+readWriteTime);
		log.info("--time to beat           ="+getLargerReadWriteTimeLimit());
		assertTrue(readWriteTime < getLargerReadWriteTimeLimit());		
	}
    
	public Object getBufFactory() {
		return bufFactory;
	}	
	
}
