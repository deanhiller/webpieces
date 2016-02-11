package org.playorm.nio.impl.cm.basic;

import java.io.IOException;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.deprecated.ConnectionCallback;
import org.playorm.nio.api.handlers.FutureOperation;
import org.playorm.nio.api.handlers.NioInterruptException;
import org.playorm.nio.api.handlers.OperationCallback;


public class FutureConnectImpl implements FutureOperation, ConnectionCallback {

	private RegisterableChannel channel;
	private Throwable e;
	private OperationCallback operationCallback;

	@Override
	public synchronized void connected(Channel channel) throws IOException {
		this.channel = channel;
		this.notify();
		if(operationCallback != null)
			operationCallback.finished(channel);
	}

	@Override
	public synchronized void failed(RegisterableChannel channel, Throwable e) {
		this.channel = channel;
		this.e = e;
		this.notify();
		if(operationCallback != null)
			operationCallback.failed(channel, e);
	}

	@Override
	public synchronized void waitForOperation(long timeoutInMillis) {
		if(channel != null) {
			if(e != null)
				throw new RuntimeException(e);
			return;
		}
		
		try {
			if(timeoutInMillis > 0) {
				this.wait(timeoutInMillis);
			} else
				this.wait();
		} catch(InterruptedException e) {
			throw new NioInterruptException(e);
		}
	}

	@Override
	public synchronized void waitForOperation() {
		waitForOperation(0);
	}

	@Override
	public synchronized void setListener(OperationCallback cb) {
		if(channel != null) {
			if(e != null) {
				cb.failed(channel, e);
			} else
				fireFinished(cb);
			return;
		}
		operationCallback = cb;
	}

	private void fireFinished(OperationCallback cb) {
		try {
			cb.finished((Channel) channel);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
