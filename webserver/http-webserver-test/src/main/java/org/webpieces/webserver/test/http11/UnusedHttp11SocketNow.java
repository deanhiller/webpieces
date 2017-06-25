package org.webpieces.webserver.test.http11;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpResponseListener;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.MarshalState;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.webserver.test.FullResponse;
import org.webpieces.webserver.test.IncomingDataListener;
import org.webpieces.webserver.test.MockTcpChannel;

public class UnusedHttp11SocketNow implements HttpSocket {
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private static final Logger log = LoggerFactory.getLogger(UnusedHttp11SocketNow.class);

	private ConnectionListener connListener;
	//This dataListener is the production listener that listens to the socket...
	private DataListener dataListener;
	//This is where the response is written to
	private MockTcpChannel channel;
	private HttpParser parser;
	private MarshalState state;
	private Memento memento;

	public UnusedHttp11SocketNow(ConnectionListener connListener, MockTcpChannel channel, HttpParser parser) {
		this.connListener = connListener;
		this.channel = channel;
		this.parser = parser;
		this.state = parser.prepareToMarshal();
		
		memento = parser.prepareToParse();
	}
	
	@Override
	public CompletableFuture<Void> connect(InetSocketAddress addr) {
		CompletableFuture<DataListener> connected = connListener.connected(channel, true);
		return connected.thenApply(d -> {
			dataListener = d;
			return null;
		});
	}

	@Override
	public CompletableFuture<HttpFullResponse> send(HttpFullRequest request) {
		
		throw new UnsupportedOperationException("not yet");
//		ByteBuffer buf = parser.marshalToByteBuffer(state, payload);
//		dataListener.incomingData(channel, buf);
//		return null;
	}

	@Override
	public CompletableFuture<HttpDataWriter> send(HttpRequest request, HttpResponseListener l) {
		ByteBuffer buf = parser.marshalToByteBuffer(state, request);
		return dataListener.incomingData(channel, buf).thenApply(v -> new MyHttpDataWriter());
	}

	private class MyHttpDataWriter implements HttpDataWriter {
		@Override
		public CompletableFuture<Void> send(HttpData chunk) {
			ByteBuffer b = parser.marshalToByteBuffer(state, chunk);
			return dataListener.incomingData(channel, b);
		}
	}
	
	@Override
	public CompletableFuture<Void> close() {
		dataListener.farEndClosed(channel);
		return CompletableFuture.completedFuture(null);
	}

	public CompletableFuture<Void> sendBytes(DataWrapper dataWrapper) {
		byte[] bytes = dataWrapper.createByteArray();
		ByteBuffer wrap = ByteBuffer.wrap(bytes);
		return dataListener.incomingData(channel, wrap);		
	}
	
	
	private class IncomingDataListenerImpl implements IncomingDataListener {

		private List<FullResponse> payloads = new ArrayList<>();
		private FullResponse chunkedResponse;
		
		@Override
		public CompletableFuture<Void> write(ByteBuffer b) {
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
			
			return CompletableFuture.completedFuture(null);
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
		public CompletableFuture<Void> close() {
			throw new UnsupportedOperationException("need to mark closed");
		}		
		
		public List<FullResponse> getResponses() {
			return payloads;
		}

		public void clear() {
			this.payloads.clear();
		}
	}
	
}
