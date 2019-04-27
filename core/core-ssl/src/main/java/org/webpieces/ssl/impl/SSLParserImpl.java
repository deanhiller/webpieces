package org.webpieces.ssl.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.SSLEngine;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.ssl.api.AsyncSSLEngine;
import org.webpieces.ssl.api.SSLParser;
import org.webpieces.ssl.api.SslListener;
import org.webpieces.ssl.api.dto.SslAction;
import org.webpieces.ssl.api.dto.SslActionEnum;

public class SSLParserImpl implements SSLParser {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private AsyncSSLEngine engine;
	private DataWrapper encryptedData;
	private DataWrapper decryptedData;
	private boolean encryptedLinkEsstablished;
	private boolean isClosed;
	private boolean isClientInitiatedClosed;
	
	public SSLParserImpl(String logId, SSLEngine sslEngine, BufferPool pool) {
		engine = new AsyncSSLEngine3Impl(logId, sslEngine, pool, new OurListener());
	}

	private class OurListener implements SslListener {

		@Override
		public void encryptedLinkEstablished() {
			encryptedLinkEsstablished = true;
		}

		@Override
		public CompletableFuture<Void> packetEncrypted(ByteBuffer engineToSocketData) {
			if(encryptedData != null) {
				DataWrapper newBuf = dataGen.wrapByteBuffer(engineToSocketData);
				encryptedData = dataGen.chainDataWrappers(encryptedData, newBuf);
				return CompletableFuture.completedFuture(null);
			}
			encryptedData = dataGen.wrapByteBuffer(engineToSocketData);
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletableFuture<Void> sendEncryptedHandshakeData(ByteBuffer engineToSocketData) {
			if(encryptedData != null) {
				DataWrapper newBuf = dataGen.wrapByteBuffer(engineToSocketData);
				encryptedData = dataGen.chainDataWrappers(encryptedData, newBuf);
				return CompletableFuture.completedFuture(null);
			}
			encryptedData = dataGen.wrapByteBuffer(engineToSocketData);
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletableFuture<Void> packetUnencrypted(ByteBuffer out) {
			decryptedData = dataGen.wrapByteBuffer(out);
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void closed(boolean clientInitiated) {
			isClosed = true;
			isClientInitiatedClosed = clientInitiated;
		}
	}

	@Override
	public CompletableFuture<List<SslAction>> parseIncoming(DataWrapper dataWrapper) {
		byte[] bytes = dataWrapper.createByteArray();
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		encryptedData = null;
		decryptedData = null;

		engine.feedEncryptedPacket(buffer);

		return CompletableFuture.completedFuture(createResult());
	}

	private List<SslAction> createResult() {
		List<SslAction> infos = new ArrayList<>();
		
		if(encryptedData != null) {
			infos.add(new SslAction(SslActionEnum.SEND_TO_SOCKET, encryptedData, null));
			encryptedData = null;
		} 
		
		if(decryptedData != null) {
			infos.add(new SslAction(SslActionEnum.SEND_TO_APP, null, decryptedData));
			decryptedData = null;
		}
		
		if(encryptedLinkEsstablished) {
			encryptedLinkEsstablished = false;
			infos.add(new SslAction(SslActionEnum.SEND_LINK_ESTABLISHED_TO_APP, null, null));
		}

		if(isClosed) {
			if(isClientInitiatedClosed)
				infos.add(new SslAction(SslActionEnum.LINK_SUCCESSFULLY_CLOSED, null, null));
			else
				infos.add(new SslAction(SslActionEnum.SEND_LINK_CLOSED_TO_APP, null, null));
		}
		
		if(infos.size() == 0)
			infos.add(new SslAction(SslActionEnum.WAIT_FOR_MORE_DATA_FROM_REMOTE_END, null, null));
		
		//get result from above
		return infos;
	}

	@Override
	public SslAction beginHandshake() {
		engine.beginHandshake();
		List<SslAction> results = createResult();
		if(results.size() != 1)
			throw new IllegalStateException("I thought begin handshake only results in ONE action.  fix this");
		return results.get(0);
	}

	@Override
	public SslAction close() {
		engine.close();
		List<SslAction> results = createResult();
		if(results.size() != 1)
			throw new IllegalStateException("I thought close engine only results in ONE action.  fix this");
		return results.get(0);
	}

	@Override
	public DataWrapper encrypt(DataWrapper data) {
		ByteBuffer b = ByteBuffer.wrap(data.createByteArray());
		engine.feedPlainPacket(b);
		DataWrapper retVal = encryptedData;
		encryptedData = null;
		return retVal;
	}
}
