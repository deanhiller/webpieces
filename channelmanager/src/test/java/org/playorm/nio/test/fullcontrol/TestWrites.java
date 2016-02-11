package org.playorm.nio.test.fullcontrol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ChannelManager;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.handlers.OperationCallback;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.testutil.CloneByteBuffer;
import org.playorm.nio.api.testutil.HandlerForTests;
import org.playorm.nio.api.testutil.chanapi.ChannelsFactory;
import org.playorm.nio.api.testutil.chanapi.SocketChannel;
import org.playorm.nio.api.testutil.nioapi.ChannelRegistrationListener;
import org.playorm.nio.api.testutil.nioapi.Select;
import org.playorm.nio.api.testutil.nioapi.SelectorListener;
import org.playorm.nio.api.testutil.nioapi.SelectorProviderFactory;

import biz.xsoftware.mock.CalledMethod;
import biz.xsoftware.mock.CloningBehavior;
import biz.xsoftware.mock.MockObject;
import biz.xsoftware.mock.MockObjectFactory;

public class TestWrites extends TestCase {

    //private static final Logger log = Logger.getLogger(TestWrites.class.getName());
    private static final BufferHelper HELPER = ChannelServiceFactory.bufferHelper(null);    
    
	private BufferFactory bufFactory;
	private ChannelService chanMgr;


	private MockObject mockHandler;
	//private MockObject mockConnect;
	private TCPChannel client1;
	
    private ChannelServiceFactory factory;
    private MockObject mockSunsChannel;
    private MockObject mockSelect;

    private MockObject mockRegListener;

    private MockObject mockWriteHandler;

	private SelectorListener listener;
    
	public TestWrites(String name) {
        super(name);
	}
    
	protected void setUp() throws Exception {
		HandlerForTests.setupLogging();
        
        if(bufFactory == null) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(FactoryCreator.KEY_IS_DIRECT, false);
            FactoryCreator creator = FactoryCreator.createFactory(null);
            bufFactory = creator.createBufferFactory(map);          
        }
        mockSunsChannel = MockObjectFactory.createMock(SocketChannel.class);
        mockSunsChannel.addIgnore("isBlocking");
        mockSunsChannel.addIgnore("getSelectableChannel");
        mockSunsChannel.setDefaultReturnValue("isBlocking", false);
        mockSunsChannel.addReturnValue("connect", true);

        mockSelect = MockObjectFactory.createMock(Select.class);
        mockSelect.addIgnore("isRunning");
        mockSelect.addIgnore("getThread");
        mockSelect.addIgnore("setRunning");
        mockSelect.setDefaultReturnValue("isRunning", true);
        mockSelect.setDefaultReturnValue("isWantShutdown", false);

        mockWriteHandler = MockObjectFactory.createMock(OperationCallback.class);
        mockRegListener = MockObjectFactory.createMock(ChannelRegistrationListener.class);
        MockObject mockChannels = MockObjectFactory.createMock(ChannelsFactory.class);
        MockObject mockSelectorProv = MockObjectFactory.createMock(SelectorProviderFactory.class);

        ChannelServiceFactory basic = ChannelServiceFactory.createFactory(null);

        Map<String, Object> props2 = new HashMap<String, Object>();
        props2.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_EXCEPTION_CHANNEL_MGR);
        props2.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, basic);
        factory = ChannelServiceFactory.createFactory(props2);   
        
		Map<String, Object> p = new HashMap<String, Object>();
        p.put(ChannelManager.KEY_ID, "[client]");
        p.put(ChannelManager.KEY_BUFFER_FACTORY, bufFactory);
        p.put("mock.channelsFactory", mockChannels);
        p.put("mock.selectorProvider", mockSelectorProv);            
        chanMgr = factory.createChannelManager(p);
        
        mockSelectorProv.addReturnValue("provider", mockSelect);
		chanMgr.start();
		CalledMethod m = mockSelect.expect("startPollingThread");
		listener = (SelectorListener)m.getAllParams()[0];
		
		mockHandler = MockObjectFactory.createMock(DataListener.class);
		mockHandler.setDefaultBehavior("incomingData", new CloneByteBuffer());
		//mockConnect = MockObjectFactory.createMock(ConnectCallback.class);
        
        mockChannels.addReturnValue("open", mockSunsChannel);
		client1 = chanMgr.createTCPChannel("ClientChannel", null);	
        mockSunsChannel.expect("configureBlocking");
        
	}
	
	protected void tearDown() throws Exception {


        mockSunsChannel.expect(MockObject.NONE);
        //mockSelect.expect(MockObject.NONE);
        HandlerForTests.checkForWarnings();
	} 
    
    public void testSomething() {
        
    }
    
    public void xxtestBasicWrite() throws Exception {       
        client1.oldConnect(null);
        mockSunsChannel.expect("connect");
        
        runBasic();
    }

    private void runBasic() throws IOException
    {
        mockSelect.setDefaultReturnValue("getThread", null);
        mockSunsChannel.setDefaultBehavior("write", new CloneByteBuffer());
        ByteBuffer b = ByteBuffer.allocate(1000);        
        String expected = "abc";
        HELPER.putString(b, expected);
        HELPER.doneFillingBuffer(b);
        
//        mockSunsChannel.addReturnValue("write", b.remaining());
        client1.oldWrite(b);
        CalledMethod m = mockSunsChannel.expect("write");
        ByteBuffer actual = (ByteBuffer)m.getAllParams()[0];
        String msg = HELPER.readString(actual, actual.remaining());
        assertEquals(expected, msg);
    }

    public void xxtestAsynchWrite() throws Exception {
        mockSelect.setDefaultReturnValue("getThread", Thread.currentThread());
        MySelectableChannel channel = new MySelectableChannel((SocketChannel)mockSunsChannel);
        MyKey key = new MyKey(channel);
        mockSunsChannel.setDefaultReturnValue("getSelectableChannel", channel);
        MockObject mockKey = key.getMock();
        mockKey.setDefaultReturnValue("channel", mockSunsChannel);
        mockKey.setDefaultReturnValue("readyOps", SelectionKey.OP_WRITE);
        
        mockSunsChannel.setDefaultBehavior("write", new NoReadByteBuffer2(0));
        
        client1.oldConnect(null);
        mockSunsChannel.expect("connect");
        
        ByteBuffer b = ByteBuffer.allocate(1000);        
        String expected = "abc";
        HELPER.putString(b, expected);
        HELPER.doneFillingBuffer(b);

        mockSelect.addReturnValue("createRegistrationListener", mockRegListener);
        mockSunsChannel.addReturnValue("write", 0);        
        client1.oldWrite(b, (OperationCallback)mockWriteHandler);
        mockSunsChannel.expect("write");
        
        mockSelect.setDefaultReturnValue("getKeyFromChannel", key);
        String[] methodNames = new String[] { "getKeyFromChannel", "register" };
        CalledMethod[] methods = mockSelect.expect(methodNames);
        Object attachment = methods[1].getAllParams()[2];
        key.attach(attachment);

        Set<SelectionKey> set = new HashSet<SelectionKey>();
        set.add(key);
        mockSelect.addReturnValue("select", 1);
        mockSelect.addReturnValue("selectedKeys", set);
        
        mockKey.addReturnValue("interestOps", SelectionKey.OP_WRITE);
        mockSunsChannel.addReturnValue("write", b.remaining());
        //now, simlute the jdk selector going off....
        listener.selectorFired();
        
        CalledMethod m = mockSunsChannel.expect("write");
        ByteBuffer actual = (ByteBuffer)m.getAllParams()[0];
        String msg = HELPER.readString(actual, actual.remaining());
        assertEquals(expected, msg);
    }

    /**
     * This tests the situation where client writes something which can't be written and
     * so that gets queued, and then client writes another thing which gets queued.  finally
     * the selector is fired, and this tests that both buffers were written.
     * 
     * @throws Exception
     */
    public void xxtestQueuedAsynchWrite() throws Exception {
        mockSelect.setDefaultReturnValue("getThread", Thread.currentThread());
        MySelectableChannel channel = new MySelectableChannel((SocketChannel)mockSunsChannel);
        MyKey key = new MyKey(channel);
        mockSunsChannel.setDefaultReturnValue("getSelectableChannel", channel);
        MockObject mockKey = key.getMock();
        mockKey.addIgnore("readyOps");
        mockKey.setDefaultReturnValue("channel", mockSunsChannel);
        mockKey.setDefaultReturnValue("readyOps", SelectionKey.OP_WRITE);    
        //mockSunsChannel.addBehavior("write", new NoReadByteBuffer2());
        //mockSunsChannel.addBehavior("write", new CloneByteBuffer());
        
        String expected = "abc";
        String expected2 = "def";
        mockKey.addReturnValue("interestOps", SelectionKey.OP_WRITE);
        fireSelector(key, expected, expected2, false);

        String[] methodNames = new String[] { "write", "write" };
        CalledMethod[] methods = mockSunsChannel.expect(methodNames);
                
        ByteBuffer actual = (ByteBuffer)methods[0].getAllParams()[0];
        String msg = HELPER.readString(actual, actual.remaining());
        assertEquals(expected, msg);

        ByteBuffer actual2 = (ByteBuffer)methods[1].getAllParams()[0];
        msg = HELPER.readString(actual2, actual2.remaining());
        assertEquals(expected2, msg);
        
        //expect that an unregistration happened
        methodNames = new String[] { "interestOps" , "interestOps"};
        methods = mockKey.expect(methodNames);
        
        //expect that this channel is no longer interested in anything
        int ops = (Integer)methods[1].getAllParams()[0];
        assertEquals(0, ops);
        
        
        //make sure data can flow through as usual...
        runBasic();
    }

    public int fireSelector(MyKey key, String expected, String expected2, boolean isSpecial) throws Exception {
        client1.oldConnect(null);
        mockSunsChannel.expect("connect");
        
        ByteBuffer b = ByteBuffer.allocate(1000);        
        HELPER.putString(b, expected);
        HELPER.doneFillingBuffer(b);
        int remain1 = b.remaining();
        
        mockSelect.addReturnValue("createRegistrationListener", mockRegListener);
        mockSunsChannel.addBehavior("write", new NoReadByteBuffer2(0));
        client1.oldWrite(b, (OperationCallback)mockWriteHandler);
        mockSunsChannel.expect("write");
        
        b = ByteBuffer.allocate(50);
        int remain2 = b.remaining();
        HELPER.putString(b, expected2);
        HELPER.doneFillingBuffer(b);
        client1.oldWrite(b, (OperationCallback)mockWriteHandler);
        
        mockSunsChannel.expect(MockObject.NONE);
        
        mockSelect.setDefaultReturnValue("getKeyFromChannel", key);
        
        String[] methodNames = new String[] { "getKeyFromChannel", "register" };
        CalledMethod[] methods = mockSelect.expect(methodNames);
        Object attachment = methods[1].getAllParams()[2];
        key.attach(attachment);

        Set<SelectionKey> set = new HashSet<SelectionKey>();
        set.add(key);
        mockSelect.addReturnValue("select", 1);
        mockSelect.addReturnValue("selectedKeys", set);
        
        mockSunsChannel.addBehavior("write", new NoReadByteBuffer2(remain1));
        if(isSpecial)
        	mockSunsChannel.addBehavior("write", new NoReadByteBuffer2(1));
        else
        	mockSunsChannel.addBehavior("write", new NoReadByteBuffer2(remain2));
        //now, simlute the jdk selector going off....
        listener.selectorFired();     
        
        return remain2;
    }
    
    /**
     * Have client do 3 writes, have the jdk not write at first causing writes to be 
     * queued.  Then have 1.5 writes happen causing the channel to stay registered
     * for writes.  fire the selector and write the rest out.
     */
    public void xxtestDelayedAsynchWrite() throws Exception {
        mockSelect.setDefaultReturnValue("getThread", Thread.currentThread());
        MySelectableChannel channel = new MySelectableChannel((SocketChannel)mockSunsChannel);
        MyKey key = new MyKey(channel);
        mockSunsChannel.setDefaultReturnValue("getSelectableChannel", channel);
        MockObject mockKey = key.getMock();
        mockKey.addIgnore("readyOps");
        mockKey.setDefaultReturnValue("channel", mockSunsChannel);
        mockKey.setDefaultReturnValue("readyOps", SelectionKey.OP_WRITE);
        
        //mockSunsChannel.setDefaultBehavior("write", new CloneForDelayedWrite());

        String expected = "abc";
        String expected2 = "def";
        int remain2 = fireSelector(key, expected, expected2, true);
        
        String[] methodsNames = new String[] { "write", "write" };
        CalledMethod[] methods = mockSunsChannel.expect(methodsNames);
        
        ByteBuffer actual = (ByteBuffer)methods[0].getAllParams()[0];        
        String msg = HELPER.readString(actual, actual.remaining());
        assertEquals(expected, msg);
        
        //because cache1 was a snapshot of what was passed in, we need to modify it...
        ByteBuffer cache1 = (ByteBuffer)methods[1].getAllParams()[0];
        cache1.limit(1);
        
        ByteBuffer b3 = ByteBuffer.allocate(50);
        int remain3 = b3.remaining();
        String expected3 = "ghi";
        HELPER.putString(b3, expected3);
        HELPER.doneFillingBuffer(b3);
        client1.oldWrite(b3, (OperationCallback)mockWriteHandler);        

        Set<SelectionKey> set = new HashSet<SelectionKey>();
        set.add(key);
        mockSelect.addReturnValue("select", 1);
        mockSelect.addReturnValue("selectedKeys", set);
        //mockKey.addReturnValue("interestOps", SelectionKey.OP_WRITE);        
        
        mockSunsChannel.addBehavior("write", new NoReadByteBuffer2(remain2));
        mockSunsChannel.addBehavior("write", new NoReadByteBuffer2(remain3));
        mockKey.addReturnValue("interestOps", SelectionKey.OP_WRITE);
        //fire the selector again....
        listener.selectorFired();
        
        methods = mockSunsChannel.expect(methodsNames);
        
        ByteBuffer cache2 = (ByteBuffer)methods[0].getAllParams()[0];
        ByteBuffer actual2 = ByteBuffer.allocate(20);
        actual2.put(cache1);
        actual2.put(cache2);
        HELPER.doneFillingBuffer(actual2);
        String msg2 = HELPER.readString(actual2, actual2.remaining());
        assertEquals(expected2, msg2);
        
        ByteBuffer actual3 = (ByteBuffer)methods[1].getAllParams()[0];
        String msg3 = HELPER.readString(actual3, actual3.remaining());
        assertEquals(expected3, msg3);
        
        //expect that an unregistration happened
        String[] methodNames = new String[] { "interestOps", "interestOps" };
        methods = mockKey.expect(methodNames);
        
        //expect that this channel is no longer interested in anything
        int ops = (Integer)methods[1].getAllParams()[0];
        assertEquals(0, ops);
        
        //make sure data can flow through as usual...
        runBasic();
    }
    
    /**
     * Test exceptions on all tests above this one and make sure a failure
     * event is fired to the client.
     */
    public void testExcepitons() {
        
    }
    
    private static final class NoReadByteBuffer2 implements CloningBehavior {
    	private int numReadBytes;
		private NoReadByteBuffer2(int numReadBytes) {
    		this.numReadBytes = numReadBytes;
    	}
    	public Object[] writeCloner(ByteBuffer b) {
    		return new Object[] { CloneByteBuffer.cloneWithoutModify(b) };
    	}
    	public int write(ByteBuffer b) {
    		if(numReadBytes == 0)
    			return 0;
    		
    		if(b.remaining() < numReadBytes) {
    			numReadBytes = b.remaining();
    		}
    		byte[] data = new byte[numReadBytes];
    		b.get(data);
    		
    		return numReadBytes;
    	}    	 	
    }
    
}
