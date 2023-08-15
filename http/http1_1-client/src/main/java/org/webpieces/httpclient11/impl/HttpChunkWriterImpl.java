package org.webpieces.httpclient11.impl;

import java.nio.ByteBuffer;

import org.webpieces.nio.api.Throttle;
import org.webpieces.nio.impl.cm.basic.Throttler;
import org.webpieces.util.futures.XFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.MarshalState;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpData;

public class HttpChunkWriterImpl implements HttpDataWriter {

	private ChannelProxy channel;
	private HttpParser parser;
	private MarshalState state;
	private boolean isConnectMsg;
	private int maxBytesToSend;

	private int currentBytes = 0;

	public HttpChunkWriterImpl(
			ChannelProxy channel,
			HttpParser parser,
			MarshalState state,
			boolean isConnectMsg,
			int bytesTracker
	) {
		this.channel = channel;
		this.parser = parser;
		this.state = state;
		this.isConnectMsg = isConnectMsg;
		this.maxBytesToSend = bytesTracker;
	}

	@Override
	public XFuture<Void> send(HttpData chunk) {
		if(isConnectMsg) {
			//special case where we don't go through parser as SSL is being passed through now
			DataWrapper body = chunk.getBody();
			byte[] createByteArray = body.createByteArray();
			ByteBuffer buffer = ByteBuffer.wrap(createByteArray);
			return channel.write(buffer);
		}

		if(maxBytesToSend == 0) {
			//chunking nor content-length exists so no body is allowed
			XFuture<Void> failure = new XFuture<Void>();
			failure.completeExceptionally(new IllegalStateException("Header "+KnownHeaderName.TRANSFER_ENCODING.getHeaderName()+" was not set with 'chunked' " +
					"NOT did Header "+KnownHeaderName.CONTENT_LENGTH+" exist so you can't send data"));
			return failure;
		} else if(maxBytesToSend > 0) {
			currentBytes += chunk.getBody().getReadableSize();
			if(currentBytes > maxBytesToSend) {
				XFuture<Void> failure = new XFuture<Void>();
				failure.completeExceptionally(new IllegalStateException("You are sending more data than your Header "+KnownHeaderName.CONTENT_LENGTH.getHeaderName()+" allows"));
				return failure;
			}
		}

		ByteBuffer buffer = parser.marshalToByteBuffer(state, chunk);
		return channel.write(buffer);
	}

}
