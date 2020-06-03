package org.webpieces.http2client.mock;

import java.nio.ByteBuffer;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2MsgType;

public class Preface implements Http2Msg {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private ByteBuffer buffer;

	public Preface(ByteBuffer prefaceBuffer) {
		this.buffer = prefaceBuffer;
	}

	@Override
	public int getStreamId() {
		return 0;
	}

	@Override
	public Http2MsgType getMessageType() {
		return null;
	}

	public void verify() {
		buffer.flip();
		DataWrapper data = dataGen.wrapByteBuffer(buffer);
		String content = data.createStringFromUtf8(0, data.getReadableSize());
		if(!"PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".equals(content))
			throw new IllegalStateException("preface incorrect="+content);
	}

	
}
