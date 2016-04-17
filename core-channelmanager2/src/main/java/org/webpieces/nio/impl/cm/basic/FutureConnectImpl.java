package org.webpieces.nio.impl.cm.basic;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.exceptions.FailureInfo;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.util.futures.Promise;


public class FutureConnectImpl implements ConnectionListener {

	private Promise<Channel, FailureInfo> promise;

	public FutureConnectImpl(Promise<Channel, FailureInfo> promise) {
		this.promise = promise;
	}
	
	@Override
	public void connected(Channel channel) {
		promise.setResult(channel);
	}

	@Override
	public void failed(RegisterableChannel channel, Throwable e) {
		promise.setFailure(new FailureInfo(channel, e));
	}

//	@Override
//	public synchronized void waitForOperation(long timeoutInMillis) {
//		if(channel != null) {
//			if(e != null)
//				throw new RuntimeException(e);
//			return;
//		}
//		
//		try {
//			if(timeoutInMillis > 0) {
//				this.wait(timeoutInMillis);
//			} else
//				this.wait();
//		} catch(InterruptedException e) {
//			throw new RuntimeInterruptedException(e);
//		}
//	}
//
//	@Override
//	public synchronized void waitForOperation() {
//		waitForOperation(0);
//	}
//
//	@Override
//	public synchronized void setListener(OperationCallback cb) {
//		if(channel != null) {
//			if(e != null) {
//				cb.failed(channel, e);
//			} else
//				fireFinished(cb);
//			return;
//		}
//		operationCallback = cb;
//	}
//
//	private void fireFinished(OperationCallback cb) {
//		cb.finished((Channel) channel);
//	}

}
