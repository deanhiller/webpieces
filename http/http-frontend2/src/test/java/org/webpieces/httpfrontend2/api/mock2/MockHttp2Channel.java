package org.webpieces.httpfrontend2.api.mock2;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.nio.api.handlers.DataListener;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.MarshalState;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

/**
 * Write out to the channel like it really happens in production, and read what the server wrote back 
 * from this class
 * 
 * @author dhiller
 *
 */
public class MockHttp2Channel {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private DataListener listener;
	private Http2ChannelCache mockHttp2Channel;
	private HpackParser parser;
	private MarshalState marshalState;
	private Http2Parser frameParser;

	public MockHttp2Channel(Http2ChannelCache mockHttp2Channel) {
		this.mockHttp2Channel = mockHttp2Channel;
		BufferPool bufferPool = new BufferCreationPool();
		parser = HpackParserFactory.createParser(bufferPool, false);
		marshalState = parser.prepareToMarshal(4096, 4096);
		frameParser = Http2ParserFactory.createParser(bufferPool);
	}

	public void setDataListener(DataListener dataListener) {
		this.listener = dataListener;
	}

	public void send(ByteBuffer buffer) {
		listener.incomingData(mockHttp2Channel, buffer);
	}
	
	public void sendPreface() {
		String preface = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";
		byte[] bytes = preface.getBytes(StandardCharsets.UTF_8);
		ByteBuffer wrap = ByteBuffer.wrap(bytes);
		listener.incomingData(mockHttp2Channel, wrap);
	}
	
	public void sendPrefaceAndSettings(SettingsFrame settings) {
		String preface = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";
		byte[] bytes = preface.getBytes(StandardCharsets.UTF_8);
		DataWrapper data = parser.marshal(marshalState, settings);
		DataWrapper prefaceWrapper = dataGen.wrapByteArray(bytes);
		DataWrapper all = dataGen.chainDataWrappers(prefaceWrapper, data);
		ByteBuffer buf = ByteBuffer.wrap(all.createByteArray());
		listener.incomingData(mockHttp2Channel, buf);
	}
	
	public void sendHexBack(String hex) {
		byte[] bytes = DatatypeConverter.parseHexBinary(hex.replaceAll("\\s+",""));
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		listener.incomingData(mockHttp2Channel, buf);		
	}
	
	public void send(Http2Msg msg) {
		if(listener == null)
			throw new IllegalStateException("Not connected so we cannot write back");
		DataWrapper data = parser.marshal(marshalState, msg);
		byte[] bytes = data.createByteArray();
		if(bytes.length == 0)
			throw new IllegalArgumentException("how do you marshal to 0 bytes...WTF");
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		listener.incomingData(mockHttp2Channel, buf);
	}
	
	public void sendFrame(Http2Frame frame) {
		DataWrapper data = frameParser.marshal(frame);
		byte[] bytes = data.createByteArray();
		if(bytes.length == 0)
			throw new IllegalArgumentException("how do you marshal to 0 bytes...WTF");
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		listener.incomingData(mockHttp2Channel, buf);
	}

	public List<Http2Msg> getFramesAndClear() {
		return mockHttp2Channel.getFramesAndClear();
	}

	public Http2Msg getFrameAndClear() {
		return mockHttp2Channel.getFrameAndClear();
	}

	public void close() {
		listener.farEndClosed(mockHttp2Channel);
	}

	public boolean isClosed() {
		return mockHttp2Channel.isClosed();
	}

}
