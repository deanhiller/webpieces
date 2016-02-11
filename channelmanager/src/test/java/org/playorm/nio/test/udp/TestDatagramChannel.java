package org.playorm.nio.test.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.DatagramChannel;
import org.playorm.nio.api.deprecated.ChannelManager;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.handlers.DatagramListener;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.testutil.CloneByteBuffer;

import biz.xsoftware.mock.CalledMethod;
import biz.xsoftware.mock.MockObject;
import biz.xsoftware.mock.testcase.MockTestCase;

/**
 */
public class TestDatagramChannel extends MockTestCase
{
    private static final BufferHelper HELPER = ChannelServiceFactory.bufferHelper(null);
    private static final Logger log = Logger.getLogger(TestDatagramChannel.class.getName());
    
    private ChannelService svc;
    private DatagramChannel client;
    private DatagramChannel server;
    private MockObject clientHandler;
    private MockObject svrHandler;

    private BufferFactory bufFactory;
    private DatagramChannel client2;

    public TestDatagramChannel(String name) {
        super(name);
    }
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUpImpl() throws Exception
    {
        if(bufFactory == null) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(FactoryCreator.KEY_IS_DIRECT, false);
            FactoryCreator creator = FactoryCreator.createFactory(null);
            bufFactory = creator.createBufferFactory(map);          
        }        
        ChannelServiceFactory basic = ChannelServiceFactory.createFactory(null);
        
        Map<String, Object> props2 = new HashMap<String, Object>();
        props2.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_EXCEPTION_CHANNEL_MGR);
        props2.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, basic);
        ChannelServiceFactory factory = ChannelServiceFactory.createFactory(props2);        
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ChannelManager.KEY_BUFFER_FACTORY, bufFactory);
        map.put(ChannelManager.KEY_ID, "server+client");
        svc = factory.createChannelManager(map);
        
        svc.start();
        
        client = svc.createDatagramChannel("client", 100);
        client2 = svc.createDatagramChannel("client2", 100);
        server = svc.createDatagramChannel("server", 100);
        
        clientHandler = createMock(DatagramListener.class);
        svrHandler = createMock(DatagramListener.class);
        clientHandler.setDefaultBehavior("incomingData", new CloneByteBuffer());
        svrHandler.setDefaultBehavior("incomingData", new CloneByteBuffer());
    }


    /*
     * @see TestCase#tearDown()
     */
    protected void tearDownImpl() throws Exception
    {
        svc.stop();
        
        client.close();
        client2.close();
        server.close();
        
        clientHandler.expect(MockObject.NONE);
        svrHandler.expect(MockObject.NONE);
    }
    
    public void testBasic() throws IOException, InterruptedException {
        client.registerForReads((DatagramListener)clientHandler);
        server.registerForReads((DatagramListener)svrHandler);
        
        InetSocketAddress anyPort = new InetSocketAddress(0);
        client.bind(anyPort);
        client2.bind(anyPort);
        server.bind(anyPort);
        
        InetSocketAddress clientAddr = client.getLocalAddress();
        InetSocketAddress client2Addr = client2.getLocalAddress();        
        InetSocketAddress svrAddr = server.getLocalAddress();
        
        log.info("client="+clientAddr);
        log.info("client2="+client2Addr);
        log.info("server="+svrAddr);
        
        InetAddress localHost = InetAddress.getLocalHost();
        InetSocketAddress serverAddr = new InetSocketAddress(localHost, svrAddr.getPort());
        
        String payload = "payload";
        ByteBuffer b = ByteBuffer.allocate(100);
        HELPER.putString(b, payload);
        HELPER.doneFillingBuffer(b);
        client.oldWrite(serverAddr, b);
        
        verifyPacket(clientAddr, localHost, payload);
          
        b.rewind();
        client2.oldWrite(serverAddr, b);
        
        verifyPacket(client2Addr, localHost, payload);
    }

    /**
     * @param clientAddr
     * @param localHost
     * @param payload
     */
    private void verifyPacket(InetSocketAddress clientAddr, InetAddress localHost, String payload)
    {
        CalledMethod m = svrHandler.expect("incomingData");
        
        InetSocketAddress remoteAddr = (InetSocketAddress)m.getAllParams()[1];
        ByteBuffer buf1 = (ByteBuffer)m.getAllParams()[2];
        
        assertEquals(localHost, remoteAddr.getAddress());
        assertEquals(clientAddr.getPort(), remoteAddr.getPort());
        
        String actual = HELPER.readString(buf1, buf1.remaining());
        //log.info("payload='"+payload+"' act='"+actual+"'");
        assertEquals(payload, actual);
    }
    
    
}
