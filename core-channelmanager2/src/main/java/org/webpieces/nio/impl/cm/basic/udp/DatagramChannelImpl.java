package org.webpieces.nio.impl.cm.basic.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.exceptions.NioException;
import org.webpieces.nio.api.handlers.DatagramListener;
import org.webpieces.nio.impl.util.ChannelSessionImpl;


/**
 */
public class DatagramChannelImpl implements DatagramChannel
{
    private static final Logger log = LoggerFactory.getLogger(DatagramChannelImpl.class);
    
    private ChannelSession session = new ChannelSessionImpl();
    private DatagramSocket socket;
    private String id;
    private final DatagramListener listener;
    private ByteBuffer buffer;
    private ReaderThread readerThread;
    private boolean shutDownThread = false;
    private String name;

    public DatagramChannelImpl(String id, int bufferSize, DatagramListener dataListener) {
        this.id = "["+id+"] ";
        buffer = ByteBuffer.allocate(bufferSize);
        this.listener = dataListener;
    }

    /**
     * @see org.webpieces.nio.api.channels.Channel#registerForReads(org.webpieces.nio.api.handlers.DataListener)
     */
    public void registerForReads() {
    }

    /**
     * @see org.webpieces.nio.api.channels.Channel#unregisterForReads()
     */
    public void unregisterForReads() {
    }


    /**
     * @see org.webpieces.nio.api.channels.Channel#getSession()
     */
    public ChannelSession getSession() {
        return session;
    }

    /**
     * @see org.webpieces.nio.api.channels.RegisterableChannel#setReuseAddress(boolean)
     */
    public void setReuseAddress(boolean b) {
        if(socket == null)
            throw new IllegalStateException(id+"Must bind socket before any operations can be called");
        try {
			socket.setReuseAddress(b);
		} catch (SocketException e) {
			throw new NioException(e);
		}
    }

    /**
     * @see org.webpieces.nio.api.channels.RegisterableChannel#bind(java.net.SocketAddress)
     */
    public void bind(SocketAddress addr) {
    	try {
    		socket = new DatagramSocket(addr);
    		readerThread = new ReaderThread();
    		readerThread.start();
    	} catch(IOException e) {
    		throw new NioException(e);
    	}
    }

    public void setId(Object id) {
    }

    public Object getId() {
        return id;
    }

    /**
     * @see org.webpieces.nio.api.channels.RegisterableChannel#isBlocking()
     */
    public boolean isBlocking() {
        return true;
    }

    /**
     * @see org.webpieces.nio.api.channels.RegisterableChannel#close()
     */
    public void close() {
        if(socket == null)
            return;

        //stop the thread first!!!!   
        if(Thread.currentThread().equals(readerThread)) {
            //since we are on the reader thread, we are not stuck on a DatagramPacket.receive call, just 
            //close and return
            shutDownThread = true;
            socket.close();
            return;
        }
        
        //otherwise, we must interrupt the call to receive
        shutDownThread = true;
        
        //useless on a DatagramSocket for some reason......
        //readerThread.interrupt();        
        
        socket.close();
    }
    
    /**
     * @see org.webpieces.nio.api.channels.RegisterableChannel#isClosed()
     */
    public boolean isClosed() {
        if(socket == null)
            throw new IllegalStateException(id+"Must bind socket before any operations can be called");
        return socket.isClosed();
    }

    /**
     * @see org.webpieces.nio.api.channels.RegisterableChannel#isBound()
     */
    public boolean isBound() {
        if(socket == null)
            return false;
        return socket.isBound();
    }

    /**
     * @see org.webpieces.nio.api.channels.RegisterableChannel#getLocalAddress()
     */
    public InetSocketAddress getLocalAddress() {
        if(!socket.isBound())
            throw new IllegalStateException(id+"Must bind socket before any operations can be called");
        log.trace("get local="+socket.getLocalPort());
        return new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort());
    }

    public void write(SocketAddress addr, ByteBuffer b) {
    	try {
			writeImpl(addr,b);
		} catch (IOException e) {
			throw new NioException(e);
		}
    }
    /**
     * @throws IOException 
     * @see org.webpieces.nio.api.channels.DatagramChannel#oldWrite(java.net.SocketAddress, java.nio.ByteBuffer)
     */
    private void writeImpl(SocketAddress addr, ByteBuffer b) throws IOException {
        if(socket == null)
            throw new IllegalStateException(id+"Must bind socket before any operations can be called");
        DatagramPacket packet = new DatagramPacket(b.array(), b.position(), b.limit()-b.position(), addr);
        
        if(log.isTraceEnabled())
            log.trace("size="+(b.limit()-b.position())+" addr="+addr);
        
        
        socket.send(packet);
    }

    private void doThreadWork() {
        while(!shutDownThread) {
            readPackets();
        }
        if(log.isTraceEnabled())
            log.trace(id+"reader thread ending");
    }
    
    private void readPackets() {
        InetSocketAddress fromAddr = null;
        try {
            buffer.clear();
            DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.remaining());
            socket.receive(packet);
            fromAddr = (InetSocketAddress)packet.getSocketAddress();
            int offset = packet.getOffset();
            int len = packet.getLength();

            buffer.position(offset);
            buffer.limit(offset+len);
            
            fireToListener(this, fromAddr, buffer);
        } catch(Throwable e) {
            //ignore an SocketException when shutDownThread.
            if(e instanceof SocketException && shutDownThread)
                return;
            
            log.warn(id+"Exception processing packet", e);
            fireFailure(fromAddr, buffer, e);
        }
    }
    
    /**
     * @param fromAddr
     */
    private void fireToListener(DatagramChannel c, InetSocketAddress fromAddr, ByteBuffer b) {
        try {
            listener.incomingData(c, fromAddr, b);
            
            if(b.remaining() > 0) {
                log.warn(id+"Client="+listener+" did not read all the data from the buffer");
            }
        } catch(Throwable e) {
            log.warn(id+"Exception in client's listener", e);
        }
    }

    /**
     * @param fromAddr 
     * @param e
     */
    private void fireFailure(InetSocketAddress fromAddr, ByteBuffer data, Throwable e) {
        try {
            listener.failure(this, fromAddr, data, e);
        } catch(Throwable ee) {
            log.warn(id+"Exception notifying client of exception", ee);
        }
    }

    private class ReaderThread extends Thread {

        /**
         * @see java.lang.Thread#run()
         */
        @Override
        public void run()
        {
            doThreadWork();
        }

    }

    /**
     * @see org.webpieces.nio.api.channels.RegisterableChannel#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @see org.webpieces.nio.api.channels.RegisterableChannel#getName()
     */
    public String getName()
    {
        return name;
    }
}
