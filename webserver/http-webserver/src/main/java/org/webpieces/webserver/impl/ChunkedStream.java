package org.webpieces.webserver.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpLastChunk;

public class ChunkedStream extends OutputStream {

	private static final Logger log = LoggerFactory.getLogger(ChunkedStream.class);
	private static final DataWrapperGenerator wrapperFactory = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private ByteArrayOutputStream str = new ByteArrayOutputStream();

	private FrontendSocket channel;
	private int size;
	private String type;

	public ChunkedStream(FrontendSocket channel, int size, boolean compressed) {
		this.channel = channel;
		this.size = size;
		this.str = new ByteArrayOutputStream(size);
		if(compressed)
			this.type = "compressed";
		else
			this.type = "not compressed";
	}

	@Override
	public void write(int b) throws IOException {
		str.write(b);
		
		if(str.size() >= size) {
			writeDataOut();
		}
	}

	@Override
	public void flush() {
		if(str.size() > 0) {
			writeDataOut();
		}
		

	}
	
	@Override
	public void close() {
		if(str.size() > 0) {
			writeDataOut();
		}
		
		//now write Last chunk
		channel.write(new HttpLastChunk());
	}
	
	private void writeDataOut() {
		byte[] data = str.toByteArray();
		str = new ByteArrayOutputStream();
		DataWrapper body = wrapperFactory.wrapByteArray(data);
		HttpChunk chunk = new HttpChunk();
		//if(log.isDebugEnabled())
			log.info("writing "+type+" data="+body.getReadableSize());
		chunk.setBody(body);
		channel.write(chunk);
	}

}
