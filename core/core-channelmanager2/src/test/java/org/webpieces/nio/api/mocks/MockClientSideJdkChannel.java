package org.webpieces.nio.api.mocks;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.nio.api.jdk.JdkSocketChannel;

public class MockClientSideJdkChannel extends MockSuperclass implements JdkSocketChannel {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	enum Method implements MethodEnum {
		CONNECT,
		FAR_END_CLOSED,
		FAILURE,
		FINISH_CONNECT
	}

	private MockSelectionKey selectionKey;
	private int numBytesToConsume;
	private Queue<Byte> queue = new LinkedBlockingQueue<>();
	private Queue<byte[]> payloadQueue = new LinkedBlockingQueue<>();
	private Queue<byte[]> toRead = new LinkedBlockingQueue<>();

	public void addConnectReturnValue(boolean isConnected) {
		super.addValueToReturn(Method.CONNECT, isConnected);
	}
	@Override
	public boolean connect(SocketAddress addr) throws IOException {
		return (boolean) super.calledMethod(Method.CONNECT, addr);
	}
	
	@Override
	public void configureBlocking(boolean b) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void bind(SocketAddress addr) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isBound() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setNumBytesToConsume(int numBytes) {
		this.numBytesToConsume = numBytes;
	}
	
	@Override
	public int write(ByteBuffer buf) throws IOException {
		int min = Math.min(buf.remaining(), numBytesToConsume);
		numBytesToConsume -= min;
		byte[] data = new byte[min];
		buf.get(data); //simulate consumption
		payloadQueue.add(data);
		for(byte b : data)
			queue.add(b);
		return min;
	}

	public byte nextByte() {
		return queue.remove();
	}
	
	public DataWrapper nextPayload() {
		byte[] payload = payloadQueue.remove();
		DataWrapper dataWrapper = dataGen.wrapByteArray(payload);
		return dataWrapper;
	}
	
	@Override
	public int read(ByteBuffer b) throws IOException {
		byte[] data = toRead.poll();
		if(data == null)
			return -1;

		b.put(data);
		return data.length;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReuseAddress(boolean b) throws SocketException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InetAddress getInetAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public InetAddress getLocalAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void finishConnect() throws IOException {
		super.calledVoidMethod(Method.FINISH_CONNECT, true);
	}

	public int getNumTimesFinishConnectCalled() {
		return getCalledMethodList(Method.FINISH_CONNECT).size();
	}
	
	@Override
	public void setKeepAlive(boolean b) throws SocketException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getKeepAlive() throws SocketException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getSoTimeout() throws SocketException {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public boolean isOpen() {
		return true;
	}
	@Override
	public SelectionKey register(int allOps, Object struct) {
		if(selectionKey == null)
			selectionKey = new MockSelectionKey();
		
		selectionKey.interestOps(allOps);
		selectionKey.attach(struct);
		
		return selectionKey;
	}
	
	@Override
	public SelectionKey keyFor() {
		return selectionKey;
	}

	public void setReadyToConnect() {
		selectionKey.setReadyToConnect();
	}
	public void setReadyToWrite() {
		selectionKey.setReadyToWrite();
	}
	
	public SelectionKey getKey() {
		if(selectionKey == null)
			return null;
		
		int val = selectionKey.interestOps() & selectionKey.readyOps();
		if(val > 0)
			return selectionKey;
		return null;
	}
	public boolean isRegisteredForReads() {
		if(selectionKey == null)
			return false;
		if((selectionKey.interestOps() & SelectionKey.OP_READ) > 0)
			return true;
		
		return false;
	}
	public boolean isRegisteredForWrites() {
		if(selectionKey == null)
			return false;
		if((selectionKey.interestOps() & SelectionKey.OP_WRITE) > 0)
			return true;
		
		return false;
	}
	public int getNumBytesConsumed() {
		return queue.size();
	}

	public void forceDataRead(MockJdk mockJdk, DataWrapper wrapper) {
		forceDataRead(mockJdk, wrapper.createByteArray());
	}
	
	public void forceDataRead(MockJdk mockJdk, byte[] buffer) {
		toRead.add(buffer);
		selectionKey.setReadyToRead(); //update key state to ready to read
		mockJdk.fireSelector(); //simulate the jdk firing selector from key update
	}
	
	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		return null;
	}

}
