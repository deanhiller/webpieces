package org.webpieces.nio.api.mocks;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.mock.MockSuperclass;
import org.webpieces.nio.api.jdk.JdkServerSocketChannel;
import org.webpieces.nio.api.jdk.JdkSocketChannel;

public class MockSvrChannel extends MockSuperclass implements JdkServerSocketChannel {

	private List<MockChannel> toConnect = new ArrayList<>();
	private List<MockChannel> connected2 = new ArrayList<>();

	private MockSelectionKey selectionKey;

	@Override
	public void close() throws IOException {
	}

	@Override
	public SelectionKey keyFor() {
		return selectionKey;
	}

	@Override
	public SelectionKey register(int ops, Object struct) throws ClosedChannelException {
		if(selectionKey == null)
			selectionKey = new MockSelectionKey();
		
		selectionKey.interestOps(ops);
		selectionKey.attach(struct);
		
		return selectionKey;
	}

	@Override
	public ServerSocket socket() {
		return null;
	}

	@Override
	public JdkSocketChannel accept() throws IOException {
		MockChannel channel = toConnect.remove(0);
		if(toConnect.size() == 0) {
			selectionKey = null;
		}
		
		connected2.add(channel);
		return channel;
	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SelectableChannel configureBlocking(boolean block) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServerSocketChannel getRealChannel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetRegisteredOperations(int ops) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setReuseAddress(boolean b) throws SocketException {
	}

	@Override
	public void bind(SocketAddress srvrAddr) throws IOException {
	}

	@Override
	public boolean isBound() {
		return true;
	}

	public void addNewChannel(MockChannel mockChannel) {
		if(selectionKey == null)
			selectionKey = new MockSelectionKey();
		selectionKey.setReadyToAccept();
		this.toConnect.add(mockChannel);
	}

	public List<MockChannel> getConnectedChannels() {
		return connected2;
	}

	public SelectionKey getKey() {
		return selectionKey;
	}

	@Override
	public boolean isClosed() {
		return false;
	}

}
