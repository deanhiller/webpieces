package org.webpieces.ssl.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.SSLEngine;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.ssl.api.AsyncSSLEngine;
import org.webpieces.ssl.api.SSLParser;
import org.webpieces.ssl.api.SslListener;
import org.webpieces.ssl.api.SslResult;
import org.webpieces.ssl.api.SslState;

public class SSLParserImpl implements SSLParser {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private AsyncSSLEngine engine;
	private Runnable lastRunnable;
	private DataWrapper encryptedData;
	private DataWrapper decryptedData;
	private boolean encryptedLinkEsstablished;
	private boolean isClosed;
	private boolean isClientInitiatedClosed;
	
	public SSLParserImpl(String logId, SSLEngine sslEngine, BufferPool pool) {
		engine = new AsyncSSLEngine2Impl(logId, sslEngine, pool, new OurListener());
	}

	private class OurListener implements SslListener {

		@Override
		public void encryptedLinkEstablished() {
			encryptedLinkEsstablished = true;
		}

		@Override
		public CompletableFuture<Void> packetEncrypted(ByteBuffer engineToSocketData) {
			encryptedData = dataGen.wrapByteBuffer(engineToSocketData);
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void sendEncryptedHandshakeData(ByteBuffer engineToSocketData) {
			encryptedData = dataGen.wrapByteBuffer(engineToSocketData);
		}

		@Override
		public CompletableFuture<Void> packetUnencrypted(ByteBuffer out) {
			decryptedData = dataGen.wrapByteBuffer(out);
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void runTask(Runnable r) {
			lastRunnable = r;
		}

		@Override
		public void closed(boolean clientInitiated) {
			isClosed = true;
			isClientInitiatedClosed = clientInitiated;
		}
	}

	@Override
	public CompletableFuture<SslResult> parseIncoming(DataWrapper dataWrapper) {
		byte[] bytes = dataWrapper.createByteArray();
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		do {
			if(lastRunnable != null) {
				Runnable toRun = lastRunnable;
				lastRunnable = null;//need to clear runnable in case another one is scheduled
				toRun.run();
			} else
				engine.feedEncryptedPacket(buffer);

		} while(lastRunnable != null);

		SslState state;
		if(encryptedData != null)
			state = SslState.SEND_TO_SOCKET;
		else if(decryptedData != null)
			state = SslState.SEND_TO_APP;
		else
			throw new IllegalStateException("state not expected.  all is null?");
			
		SslResult result = new SslResultImpl(
				state,
				encryptedData,
				decryptedData,
				isClientInitiatedClosed);
		
		//get result from above
		return CompletableFuture.completedFuture(result);
	}
}
