package org.webpieces.nio.api.mocks;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.HashSet;
import java.util.Set;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.nio.api.jdk.JdkDatagramChannel;
import org.webpieces.nio.api.jdk.JdkSelect;
import org.webpieces.nio.api.jdk.JdkServerSocketChannel;
import org.webpieces.nio.api.jdk.JdkSocketChannel;
import org.webpieces.nio.api.jdk.Keys;
import org.webpieces.nio.api.jdk.SelectorListener;

public class MockJdk extends MockSuperclass implements JdkSelect {

	enum Method implements MethodEnum {
		PROVIDER,
		OPEN,
		OPEN2, 
		WAKEUP, 
	}

	private SelectorListener cachedListener;
	private Thread currentThread;
	private MockSvrChannel mockSvrChannel;

	public MockJdk(MockSvrChannel mockSvrChannel) {
		this.mockSvrChannel = mockSvrChannel;
	}

	@Override
	public JdkSocketChannel open() throws IOException {
		return null;
	}

	@Override
	public JdkServerSocketChannel openServerSocket() throws IOException {
		return mockSvrChannel;
	}
	
	@Override
	public JdkDatagramChannel openDatagram() throws IOException {
		return null;
	}
	
	@Override
	public void startPollingThread(SelectorListener listener, String threadName) {
		cachedListener = listener;
	}

	@Override
	public void wakeup() {
		super.calledVoidMethod(Method.WAKEUP, true);
	}
	
	public int getNumTimesWokenUp() {
		return super.getCalledMethodList(Method.WAKEUP).size();
	}
	
	@Override
	public void stopPollingThread() {
	}

	@Override
	public Object getThread() {
		return currentThread;
	}

	@Override
	public Keys select() {
		Set<SelectionKey> keys = new HashSet<>(); 
		for(MockChannel c : mockSvrChannel.getConnectedChannels()) {
			SelectionKey key = c.getKey();
			if(key != null)
				keys.add(key);
		}
		
		SelectionKey key = mockSvrChannel.getKey();
		if(key != null)
			keys.add(key);
		
		return new Keys(keys.size(), keys);
	}

	@Override
	public boolean isRunning() {
		return true;
	}

	@Override
	public boolean isWantShutdown() {
		return false;
	}

	public void setThread(Thread currentThread) {
		this.currentThread = currentThread;
	}

	public void fireSelector() {
		cachedListener.selectorFired();
	}

	@Override
	public boolean isChannelOpen(SelectionKey key) {
		return true;
	}

}
