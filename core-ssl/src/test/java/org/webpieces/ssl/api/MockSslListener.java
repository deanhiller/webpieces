package org.webpieces.ssl.api;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ssl.api.SslListener;

public class MockSslListener implements SslListener {

	public boolean connected;
	public boolean closed;
	public boolean clientInitiated;
	public Runnable runnable;
	public List<ByteBuffer> encrypted = new ArrayList<>();
	public List<ByteBuffer> unEncrypted = new ArrayList<>();
	private List<CompletableFuture<Void>> futures = new ArrayList<>();
	
	@Override
	public void encryptedLinkEstablished() {
		if(connected)
			throw new RuntimeException("bug in implementation.  we should not fire connected twice");
		connected = true;
	}

	@Override
	public CompletableFuture<Void> packetEncrypted(ByteBuffer engineToSocketData) {
		encrypted.add(engineToSocketData);
		CompletableFuture<Void> future = new CompletableFuture<Void>();
		futures.add(future);
		return future;
	}

	@Override
	public void sendEncryptedHandshakeData(ByteBuffer engineToSocketData) {
		encrypted.add(engineToSocketData);		
	}
	
	@Override
	public void packetUnencrypted(ByteBuffer out) {
		unEncrypted.add(out);
	}

	@Override
	public void runTask(Runnable r) {
		this.runnable = r;
	}

	@Override
	public void closed(boolean clientInitiated) {
		if(closed)
			throw new RuntimeException("bug in implementation.  we should not fire closed twice");
		closed = true;
		this.clientInitiated = clientInitiated;
	}

	public List<ByteBuffer> getToSendToSocket() {
		List<ByteBuffer> temp = encrypted;
		encrypted = new ArrayList<>();
		return temp;
	}

	public Runnable getRunnable() {
		Runnable temp = runnable;
		runnable = null;
		return temp;
	}

	public List<ByteBuffer> getToSendToClient() {
		List<ByteBuffer> temp = unEncrypted;
		unEncrypted = new ArrayList<>();
		return temp;
	}

	public List<CompletableFuture<Void>> getFutures() {
		return futures;
	}

}
