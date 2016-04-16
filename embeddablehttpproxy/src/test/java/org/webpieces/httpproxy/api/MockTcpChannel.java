package org.webpieces.httpproxy.api;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.deprecated.ConnectionCallback;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.FutureOperation;
import org.webpieces.nio.api.handlers.OperationCallback;
import org.webpieces.nio.api.libs.ChannelSession;

public class MockTcpChannel implements TCPChannel {

	private DataListener dataListener;

	@Override
	public FutureOperation connect(SocketAddress addr) {
		return null;
	}

	@Override
	public FutureOperation write(ByteBuffer b) {
		return null;
	}

	@Override
	public FutureOperation close() {
		return null;
	}

	@Override
	public void registerForReads(DataListener listener) {
		this.dataListener = listener;
	}

	@Override
	public void unregisterForReads() {
		
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return null;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public ChannelSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int oldWrite(ByteBuffer b) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void oldWrite(ByteBuffer b, OperationCallback h) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void oldConnect(SocketAddress addr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void oldClose(OperationCallback cb) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void oldClose() {
		// TODO Auto-generated method stub
		
	}

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
	public void oldConnect(SocketAddress remoteAddr, ConnectionCallback cb) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getKeepAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setKeepAlive(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FutureOperation openSSL(SSLEngine engine) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FutureOperation closeSSL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInSslMode() {
		// TODO Auto-generated method stub
		return false;
	}

	public DataListener getDataListener() {
		return dataListener;
	}

}
