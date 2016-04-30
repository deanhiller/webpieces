package org.webpieces.netty.impl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.webpieces.nio.api.channels.TCPServerChannel;

public class NettyServerChannel implements TCPServerChannel {

	@Override
	public void setReuseAddress(boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setName(String string) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void bind(SocketAddress addr) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBound() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void closeServerChannel() {

	}

}
