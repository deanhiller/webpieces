package org.webpieces.webserver.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.http2parser.api.dto.DataFrame;

public class ChunkedStream extends OutputStream {

	private static final Logger log = LoggerFactory.getLogger(ChunkedStream.class);
	private static final DataWrapperGenerator wrapperFactory = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private ByteArrayOutputStream str = new ByteArrayOutputStream();

	private int size;
	private String type;
	private List<DataFrame> cache = new ArrayList<>();
	private boolean isClosed;

	public ChunkedStream(int size, boolean compressed) {
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
		isClosed = true;
	}
	
	private void writeDataOut() {
		byte[] data = str.toByteArray();
		str = new ByteArrayOutputStream();
		DataWrapper body = wrapperFactory.wrapByteArray(data);
		log.info("caching "+type+" data="+body.getReadableSize());

		DataFrame frame = new DataFrame();
		frame.setEndOfStream(false);
		frame.setData(body);
		cache.add(frame);
	}

	public boolean isClosed() {
		return isClosed;
	}

	public List<DataFrame> getFrames() {
		return cache;
	}

}
