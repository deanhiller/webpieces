package org.webpieces.webserver.test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.impl.util.ChannelSessionImpl;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class MockTcpChannel implements TCPChannel {

	private static final Logger log = LoggerFactory.getLogger(MockTcpChannel.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	private ChannelSession session = new ChannelSessionImpl();
	private List<FullResponse> payloads = new ArrayList<>();
	private FullResponse chunkedResponse;
	private HttpParser parser;
	private Memento memento;

	public MockTcpChannel(HttpParser parser) {
		this.parser = parser;
		memento = parser.prepareToParse();
	}

	@Override
	public CompletableFuture<Channel> write(ByteBuffer b) {
		DataWrapper data = dataGen.wrapByteBuffer(b);
		memento	= parser.parse(memento, data);
		List<HttpPayload> parsedMessages = memento.getParsedMessages();
		
		for(HttpPayload payload : parsedMessages) {
			if(payload instanceof HttpResponse) {
				sendResponse((HttpResponse) payload);
			} else {
				sendData((HttpData) payload);
			}
		}
		
		return CompletableFuture.completedFuture(this);
	}

	public void sendResponse(HttpResponse response) {
		if(isParsingBody()) {
			FullResponse nextResp = new FullResponse(response);
			if(!hasValidContentLength(response) && !hasChunkedEncoding(response)) {
				payloads.add(nextResp);
			} else
				chunkedResponse = nextResp;
		}
		else {
			log.error("expecting sendData but got Response instead=" + response);
			throw new IllegalStateException("Sending the data never ended from last response and we are getting next response already?");
		}

	}
	
	private boolean hasChunkedEncoding(HttpResponse response) {
		Header transferHeader = response.getHeaderLookupStruct().getLastInstanceOfHeader(KnownHeaderName.TRANSFER_ENCODING);

		if(transferHeader != null && "chunked".equals(transferHeader.getValue())) {
			return true;
		}
		return false;
	}

	private boolean hasValidContentLength(HttpResponse response) {
		Integer contentLen = null;
		Header header = response.getHeaderLookupStruct().getHeader(KnownHeaderName.CONTENT_LENGTH);
		if(header != null) {
			contentLen = Integer.parseInt(header.getValue());
		}
		if(contentLen != null && contentLen > 0)
			return true;
		return false;
	}

	private boolean isParsingBody() {
		return chunkedResponse == null;
	}

	public CompletableFuture<Void> sendData(HttpData httpData) {
		if(isParsingBody())
			throw new IllegalStateException("We are not in a state of sending content length body nor chunked data.  there is a bug somewhere");
		
		chunkedResponse.addChunk(httpData);

		if(httpData.isEndOfData()) {
			log.info("last chunk");
			payloads.add(chunkedResponse);
			chunkedResponse = null;
		}

		return CompletableFuture.completedFuture(null);
	}
	
	@Override
	public CompletableFuture<Channel> close() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public CompletableFuture<Channel> connect(SocketAddress addr, DataListener listener) {
		throw new UnsupportedOperationException("no needed");
	}

	@Override
	public CompletableFuture<Channel> registerForReads() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Channel> unregisterForReads() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRegisteredForReads() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ChannelSession getSession() {
		return session;
	}

	@Override
	public void setMaxBytesWriteBackupSize(int maxBytesBackup) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getMaxBytesBackupSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSslChannel() {
		// TODO Auto-generated method stub
		return false;
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
	public String getChannelId() {
		// TODO Auto-generated method stub
		return null;
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
	public boolean getKeepAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setKeepAlive(boolean b) {
		// TODO Auto-generated method stub

	}

	public List<FullResponse> getResponses() {
		return payloads;
	}

	public void clear() {
		this.payloads.clear();
	}

}
